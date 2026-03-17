/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.bookkeeper.mledger.offload.filesystem.impl;

import static org.apache.bookkeeper.mledger.offload.OffloadUtils.buildLedgerMetadataFormat;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.MoreExecutors;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.bookkeeper.client.api.LedgerEntries;
import org.apache.bookkeeper.client.api.LedgerEntry;
import org.apache.bookkeeper.client.api.ReadHandle;
import org.apache.bookkeeper.common.util.OrderedScheduler;
import org.apache.bookkeeper.mledger.LedgerOffloader;
import org.apache.bookkeeper.mledger.LedgerOffloaderStats;
import org.apache.pulsar.common.naming.TopicName;
import org.apache.pulsar.common.policies.data.OffloadPolicies;
import org.apache.pulsar.common.policies.data.OffloadPoliciesImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSystemManagedLedgerOffloader implements LedgerOffloader {

    private static final Logger log = LoggerFactory.getLogger(FileSystemManagedLedgerOffloader.class);
    private static final String STORAGE_BASE_PATH = "storageBasePath";
    private static final String DRIVER_NAMES = "filesystem";
    private static final String MANAGED_LEDGER_NAME = "ManagedLedgerName";
    static final long METADATA_KEY_INDEX = -1;
    private final String driverName;
    private final String storageBasePath;
    private OrderedScheduler scheduler;
    private static final long ENTRIES_PER_READ = 100;
    private OrderedScheduler assignmentScheduler;
    private OffloadPolicies offloadPolicies;
    private final LedgerOffloaderStats offloaderStats;

    public static boolean driverSupported(String driver) {
        return DRIVER_NAMES.equals(driver);
    }

    @Override
    public String getOffloadDriverName() {
        return driverName;
    }

    public static FileSystemManagedLedgerOffloader create(OffloadPoliciesImpl conf,
                                                          OrderedScheduler scheduler,
                                                          LedgerOffloaderStats offloaderStats) throws IOException {
        return new FileSystemManagedLedgerOffloader(conf, scheduler, offloaderStats);
    }

    private FileSystemManagedLedgerOffloader(OffloadPoliciesImpl conf, OrderedScheduler scheduler,
                                             LedgerOffloaderStats offloaderStats) throws IOException {
        this.offloadPolicies = conf;
        this.driverName = conf.getManagedLedgerOffloadDriver();
        String uri = conf.getFileSystemURI();
        if (uri != null && uri.startsWith("file://")) {
            this.storageBasePath = uri.substring("file://".length());
        } else if (uri != null && !uri.isEmpty()) {
            this.storageBasePath = uri;
        } else {
            this.storageBasePath = "";
        }
        this.scheduler = scheduler;
        this.assignmentScheduler = OrderedScheduler.newSchedulerBuilder()
                .numThreads(conf.getManagedLedgerOffloadMaxThreads())
                .name("offload-assignment").build();
        this.offloaderStats = offloaderStats;
    }

    public FileSystemManagedLedgerOffloader(OffloadPoliciesImpl conf,
                                            OrderedScheduler scheduler,
                                            String storageBasePath,
                                            LedgerOffloaderStats offloaderStats) throws IOException {
        this.offloadPolicies = conf;
        this.driverName = conf.getManagedLedgerOffloadDriver();
        this.storageBasePath = storageBasePath;
        this.scheduler = scheduler;
        this.assignmentScheduler = OrderedScheduler.newSchedulerBuilder()
                .numThreads(conf.getManagedLedgerOffloadMaxThreads())
                .name("offload-assignment").build();
        this.offloaderStats = offloaderStats;
    }

    @Override
    public Map<String, String> getOffloadDriverMetadata() {
        String path = storageBasePath == null ? "null" : storageBasePath;
        return ImmutableMap.of(
                STORAGE_BASE_PATH, path
        );
    }

    @Override
    public CompletableFuture<Void> offload(ReadHandle readHandle, UUID uuid, Map<String, String> extraMetadata) {
        CompletableFuture<Void> promise = new CompletableFuture<>();
        scheduler.chooseThread(readHandle.getId()).execute(
                new LedgerReader(readHandle, uuid, extraMetadata, promise, storageBasePath,
                        assignmentScheduler, offloadPolicies.getManagedLedgerOffloadPrefetchRounds(),
                        this.offloaderStats));
        return promise;
    }

    private static class LedgerReader implements Runnable {

        private final ReadHandle readHandle;
        private final UUID uuid;
        private final Map<String, String> extraMetadata;
        private final CompletableFuture<Void> promise;
        private final String storageBasePath;
        volatile Exception fileSystemWriteException = null;
        private OrderedScheduler assignmentScheduler;
        private int managedLedgerOffloadPrefetchRounds = 1;
        private final LedgerOffloaderStats offloaderStats;

        private LedgerReader(ReadHandle readHandle,
                             UUID uuid,
                             Map<String, String> extraMetadata,
                             CompletableFuture<Void> promise,
                             String storageBasePath,
                             OrderedScheduler assignmentScheduler,
                             int managedLedgerOffloadPrefetchRounds,
                             LedgerOffloaderStats offloaderStats) {
            this.readHandle = readHandle;
            this.uuid = uuid;
            this.extraMetadata = extraMetadata;
            this.promise = promise;
            this.storageBasePath = storageBasePath;
            this.assignmentScheduler = assignmentScheduler;
            this.managedLedgerOffloadPrefetchRounds = managedLedgerOffloadPrefetchRounds;
            this.offloaderStats = offloaderStats;
        }

        @Override
        public void run() {
            if (!readHandle.isClosed() || readHandle.getLastAddConfirmed() < 0) {
                promise.completeExceptionally(
                        new IllegalArgumentException("An empty or open ledger should never be offloaded"));
                return;
            }
            if (readHandle.getLength() <= 0) {
                log.warn("Ledger [{}] has zero length, but it contains {} entries. "
                    + " Attempting to offload ledger since it contains entries.", readHandle.getId(),
                    readHandle.getLastAddConfirmed() + 1);
            }
            long ledgerId = readHandle.getId();
            final String managedLedgerName = extraMetadata.get(MANAGED_LEDGER_NAME);
            String storagePath = getStoragePath(storageBasePath, managedLedgerName);
            String dataFilePath = getDataFilePath(storagePath, ledgerId, uuid);
            final String topicName = TopicName.fromPersistenceNamingEncoding(managedLedgerName);
            try {
                NativeMapFile.Writer dataWriter = new NativeMapFile.Writer(Path.of(dataFilePath));
                // Store the ledgerMetadata at index -1
                byte[] ledgerMetadata = buildLedgerMetadataFormat(readHandle.getLedgerMetadata());
                dataWriter.append(METADATA_KEY_INDEX, ledgerMetadata, 0, ledgerMetadata.length);
                AtomicLong haveOffloadEntryNumber = new AtomicLong(0);
                long needToOffloadFirstEntryNumber = 0;
                CountDownLatch countDownLatch;
                // Avoid prefetch too much data into memory
                Semaphore semaphore = new Semaphore(managedLedgerOffloadPrefetchRounds);
                do {
                    long end = Math.min(needToOffloadFirstEntryNumber + ENTRIES_PER_READ - 1,
                            readHandle.getLastAddConfirmed());
                    log.debug("read ledger entries. start: {}, end: {}", needToOffloadFirstEntryNumber, end);
                    long startReadTime = System.nanoTime();
                    LedgerEntries ledgerEntriesOnce = readHandle.readAsync(needToOffloadFirstEntryNumber, end).get();
                    long cost = System.nanoTime() - startReadTime;
                    this.offloaderStats.recordReadLedgerLatency(topicName, cost, TimeUnit.NANOSECONDS);
                    semaphore.acquire();
                    countDownLatch = new CountDownLatch(1);
                    assignmentScheduler.chooseThread(ledgerId)
                            .execute(new FileSystemWriter(ledgerEntriesOnce,
                                    dataWriter, semaphore, countDownLatch, haveOffloadEntryNumber, this));
                    needToOffloadFirstEntryNumber = end + 1;
                } while (needToOffloadFirstEntryNumber - 1 != readHandle.getLastAddConfirmed()
                        && fileSystemWriteException == null);
                countDownLatch.await();
                if (fileSystemWriteException != null) {
                    throw fileSystemWriteException;
                }
                dataWriter.close();
                promise.complete(null);
            } catch (Exception e) {
                log.error("Exception when get CompletableFuture<LedgerEntries> : ManagerLedgerName: {}, "
                        + "LedgerId: {}, UUID: {} ", managedLedgerName, ledgerId, uuid, e);
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                this.offloaderStats.recordOffloadError(topicName);
                promise.completeExceptionally(e);
            }
        }
    }

    private static class FileSystemWriter implements Runnable {

        private final LedgerEntries ledgerEntriesOnce;
        private final NativeMapFile.Writer dataWriter;
        private final CountDownLatch countDownLatch;
        private final AtomicLong haveOffloadEntryNumber;
        private final LedgerReader ledgerReader;
        private final Semaphore semaphore;

        FileSystemWriter(LedgerEntries ledgerEntriesOnce,
                         NativeMapFile.Writer dataWriter,
                         Semaphore semaphore,
                         CountDownLatch countDownLatch,
                         AtomicLong haveOffloadEntryNumber,
                         LedgerReader ledgerReader) {
            this.ledgerEntriesOnce = ledgerEntriesOnce;
            this.dataWriter = dataWriter;
            this.semaphore = semaphore;
            this.countDownLatch = countDownLatch;
            this.haveOffloadEntryNumber = haveOffloadEntryNumber;
            this.ledgerReader = ledgerReader;
        }

        @Override
        public void run() {
            String managedLedgerName = ledgerReader.extraMetadata.get(MANAGED_LEDGER_NAME);
            String topicName = TopicName.fromPersistenceNamingEncoding(managedLedgerName);
            if (ledgerReader.fileSystemWriteException == null) {
                Iterator<LedgerEntry> iterator = ledgerEntriesOnce.iterator();
                while (iterator.hasNext()) {
                    LedgerEntry entry = iterator.next();
                    long entryId = entry.getEntryId();
                    byte[] currentEntryBytes;
                    int currentEntrySize;
                    try {
                        currentEntryBytes = entry.getEntryBytes();
                        currentEntrySize = currentEntryBytes.length;
                        dataWriter.append(entryId, currentEntryBytes, 0, currentEntrySize);
                    } catch (IOException e) {
                        ledgerReader.fileSystemWriteException = e;
                        ledgerReader.offloaderStats.recordWriteToStorageError(topicName);
                        break;
                    }
                    haveOffloadEntryNumber.incrementAndGet();
                    ledgerReader.offloaderStats.recordOffloadBytes(topicName, currentEntrySize);
                }
            }
            countDownLatch.countDown();
            ledgerEntriesOnce.close();
            semaphore.release();
        }
    }

    @Override
    public CompletableFuture<ReadHandle> readOffloaded(long ledgerId, UUID uuid,
                                                       Map<String, String> offloadDriverMetadata) {

        final String ledgerName = offloadDriverMetadata.get(MANAGED_LEDGER_NAME);
        CompletableFuture<ReadHandle> promise = new CompletableFuture<>();
        String storagePath = getStoragePath(storageBasePath, ledgerName);
        String dataFilePath = getDataFilePath(storagePath, ledgerId, uuid);
        scheduler.chooseThread(ledgerId).execute(() -> {
            try {
                NativeMapFile.Reader reader = new NativeMapFile.Reader(Path.of(dataFilePath));
                promise.complete(FileStoreBackedReadHandleImpl.open(
                        scheduler.chooseThread(ledgerId), reader, ledgerId, this.offloaderStats, ledgerName));
            } catch (Throwable t) {
                log.error("Failed to open FileStoreBackedReadHandleImpl: ManagerLedgerName: {}, "
                        + "LegerId: {}, UUID: {}", ledgerName, ledgerId, uuid, t);
                promise.completeExceptionally(t);
            }
        });
        return promise;
    }

    private static String getStoragePath(String storageBasePath, String managedLedgerName) {
        return storageBasePath == null || storageBasePath.isEmpty()
                ? managedLedgerName + "/" : storageBasePath + "/" + managedLedgerName + "/";
    }

    private static String getDataFilePath(String storagePath, long ledgerId, UUID uuid) {
        return storagePath + ledgerId + "-" + uuid.toString();
    }

    @Override
    public CompletableFuture<Void> deleteOffloaded(long ledgerId, UUID uid, Map<String, String> offloadDriverMetadata) {
        String ledgerName = offloadDriverMetadata.get(MANAGED_LEDGER_NAME);
        String storagePath = getStoragePath(storageBasePath, ledgerName);
        String dataFilePath = getDataFilePath(storagePath, ledgerId, uid);
        String topicName = TopicName.fromPersistenceNamingEncoding(ledgerName);
        CompletableFuture<Void> promise = new CompletableFuture<>();
        try {
            Path dirPath = Path.of(dataFilePath);
            if (Files.exists(dirPath)) {
                Files.walkFileTree(dirPath, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            promise.complete(null);
        } catch (IOException e) {
            log.error("Failed to delete Offloaded: ", e);
            promise.completeExceptionally(e);
        }
        return promise.whenComplete((__, t) ->
                this.offloaderStats.recordDeleteOffloadOps(topicName, t == null));
    }

    @Override
    public OffloadPolicies getOffloadPolicies() {
        return offloadPolicies;
    }

    @Override
    public void close() {
        if (assignmentScheduler != null) {
            MoreExecutors.shutdownAndAwaitTermination(assignmentScheduler, 5, TimeUnit.SECONDS);
        }
    }
}
