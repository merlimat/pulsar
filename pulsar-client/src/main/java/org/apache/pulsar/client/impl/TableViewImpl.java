package org.apache.pulsar.client.impl;

import io.netty.util.Timeout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Reader;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.TableView;
import org.apache.pulsar.common.util.FutureUtil;

@Slf4j
public class TableViewImpl<T> implements TableView<T> {

    private final PulsarClientImpl client;
    private final Schema<T> schema;
    private final TableViewConfigurationData conf;

    private final ConcurrentMap<String, T> data;

    private final ConcurrentMap<String, Reader<T>> readers;

    private final List<BiConsumer<String, T>> listeners;
    private final ReentrantLock listnersMutex;

    TableViewImpl(PulsarClientImpl client,
                  Schema<T> schema,
                  TableViewConfigurationData conf) {
        this.client = client;
        this.schema = schema;
        this.conf = conf;
        this.data = new ConcurrentHashMap<>();
        this.readers = new ConcurrentHashMap<>();
        this.listeners = new ArrayList<>();
        this.listnersMutex = new ReentrantLock();
    }

    CompletableFuture<TableView<T>> start() {
        return client.getPartitionsForTopic(conf.getTopicName())
                .thenCompose(partitions -> {
                    Set<String> partitionsSet = new HashSet<>(partitions);
                    List<CompletableFuture<?>> futures = new ArrayList<>();

                    // Add new partitions
                    partitions.forEach(partition -> {
                        if (!readers.containsKey(partition)) {
                            futures.add(newReader(partition));
                        }
                    });

                    // Remove partitions that are not used anymore
                    readers.forEach((existingPartition, existingReader) -> {
                        if (!partitionsSet.contains(existingPartition)) {
                            // Remove the reader
                            futures.add(existingReader.closeAsync()
                                    .thenRun(() -> readers.remove(existingPartition, existingReader)
                                    ));
                        }
                    });

                    return FutureUtil.waitForAll(futures)
                            .thenRun(() -> schedulePartitionsCheck());
                }).thenApply(__ -> this);
    }

    private void schedulePartitionsCheck() {
        client.timer()
                .newTimeout(this::checkForPartitionsChanges, conf.getAutoUpdatePartitionsSeconds(), TimeUnit.SECONDS);
    }

    private void checkForPartitionsChanges(Timeout timeout) {
        if (timeout.isCancelled()) {
            return;
        }

        start().whenComplete((tw, ex) -> {
            if (ex != null) {
                log.warn("Failed to check for changes in number of partitions");
            }
        });
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public boolean containsKey(String key) {
        return data.containsKey(key);
    }

    @Override
    public T get(String key) {
        return data.get(key);
    }

    @Override
    public Set<Map.Entry<String, T>> entrySet() {
        return data.entrySet();
    }

    @Override
    public Set<String> keySet() {
        return data.keySet();
    }

    @Override
    public Collection<T> values() {
        return data.values();
    }

    @Override
    public void forEach(BiConsumer<String, T> action) {
        data.forEach(action);
    }

    @Override
    public void forEachAndListen(BiConsumer<String, T> action) {
        // Ensure we iterate over all the existing entry _and_ start the listening from the exact next message
        try {
            listnersMutex.lock();

            // Execute the action over existing entries
            forEach(action);

            listeners.add(action);
        } finally {
            listnersMutex.unlock();
        }
    }

    @Override
    public void close() throws PulsarClientException {
        try {
            closeAsync().get();
        } catch (Exception e) {
            throw PulsarClientException.unwrap(e);
        }
    }

    @Override
    public CompletableFuture<Void> closeAsync() {
        return FutureUtil.waitForAll(
                readers.values().stream()
                        .map(Reader::closeAsync)
                        .collect(Collectors.toList())
        );
    }

    private void handleMessage(Reader<T> reader, Message<T> msg) {
        try {
            if (!msg.hasKey()) {
                return;
            }

            if (log.isDebugEnabled()) {
                log.debug("Applying message. key={} value={}", msg.getKey(), msg.getValue());
            }

            try {
                listnersMutex.lock();
                data.put(msg.getKey(), msg.getValue());

                for (int i = 0; i < listeners.size(); i++) {
                    try {
                        listeners.get(i).accept(msg.getKey(), msg.getValue());
                    } catch (Throwable t) {
                        log.error("Table view listener raised an exception", t);
                    }
                }

            } finally {
                listnersMutex.unlock();
            }
        } finally {
            msg.release();
        }
    }

    private CompletableFuture<Reader<T>> newReader(String partition) {
        return client.newReader(schema)
                .topic(partition)
                .startMessageId(MessageId.earliest)
                .readCompacted(true)
                .poolMessages(true)
                .createAsync()
                .thenCompose(this::readAllExistingMessages);
    }

    private CompletableFuture<Reader<T>> readAllExistingMessages(Reader<T> reader) {
        long startTime = System.nanoTime();
        AtomicLong messagesRead = new AtomicLong();

        CompletableFuture<Reader<T>> future = new CompletableFuture<>();
        readAllExistingMessages(reader, future, startTime, messagesRead);
        return future;
    }

    private void readAllExistingMessages(Reader<T> reader, CompletableFuture<Reader<T>> future, long startTime,
                                         AtomicLong messagesRead) {
        reader.hasMessageAvailableAsync()
                .thenAccept(hasMessage -> {
                    if (hasMessage) {
                        reader.readNextAsync()
                                .thenAccept(msg -> {
                                    messagesRead.incrementAndGet();
                                    handleMessage(reader, msg);
                                    readAllExistingMessages(reader, future, startTime, messagesRead);
                                }).exceptionally(ex -> {
                                    future.completeExceptionally(ex);
                                    return null;
                                });
                    } else {
                        // We reached the end
                        long endTime = System.nanoTime();
                        long durationMillis = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
                        log.info("Started table view for topic {} - Replayed {} messages in {} seconds",
                                reader.getTopic(),
                                messagesRead,
                                durationMillis / 1000.0);
                        future.complete(reader);
                        readTailMessages(reader);
                    }
                });
    }

    private void readTailMessages(Reader<T> reader) {
        reader.readNextAsync()
                .thenAccept(msg -> {
                    handleMessage(reader, msg);
                    readTailMessages(reader);
                }).exceptionally(ex -> {
                    log.info("Reader {} was interrupted", reader.getTopic());
                    return null;
                });
    }
}
