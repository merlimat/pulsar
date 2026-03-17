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
package org.apache.bookkeeper.mledger.offload.filesystem;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.bookkeeper.common.util.OrderedScheduler;
import org.apache.bookkeeper.mledger.LedgerOffloaderStats;
import org.apache.bookkeeper.mledger.offload.filesystem.impl.FileSystemManagedLedgerOffloader;
import org.apache.pulsar.common.policies.data.OffloadPoliciesImpl;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

public abstract class FileStoreTestBase {
    protected FileSystemManagedLedgerOffloader fileSystemManagedLedgerOffloader;
    protected OrderedScheduler scheduler;
    protected String basePath;
    protected LedgerOffloaderStats offloaderStats;
    private ScheduledExecutorService scheduledExecutorService;
    private Path tempDir;

    @BeforeClass(alwaysRun = true)
    public final void beforeClass() throws Exception {
        init();
    }

    public void init() throws Exception {
        scheduler = OrderedScheduler.newSchedulerBuilder().numThreads(1).name("offloader").build();
    }

    @AfterClass(alwaysRun = true)
    public final void afterClass() throws IOException {
        cleanup();
    }

    public void cleanup() throws IOException {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void start() throws Exception {
        tempDir = Files.createTempDirectory("pulsar-offload-test");
        basePath = tempDir.toAbsolutePath().toString();
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        this.offloaderStats = LedgerOffloaderStats.create(true, true, scheduledExecutorService, 60);
        fileSystemManagedLedgerOffloader = new FileSystemManagedLedgerOffloader(
                OffloadPoliciesImpl.create(new Properties()),
                scheduler, basePath, offloaderStats);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() throws Exception {
        if (fileSystemManagedLedgerOffloader != null) {
            fileSystemManagedLedgerOffloader.close();
            fileSystemManagedLedgerOffloader = null;
        }
        if (offloaderStats != null) {
            offloaderStats.close();
            offloaderStats = null;
        }
        if (tempDir != null) {
            deleteRecursively(tempDir);
            tempDir = null;
        }
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdownNow();
            scheduledExecutorService = null;
        }
    }

    private static void deleteRecursively(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }
        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path d, IOException exc) throws IOException {
                Files.delete(d);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
