package org.apache.pulsar.client.api;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public interface TableViewBuilder<T> {

    TableViewBuilder<T> loadConf(Map<String, Object> config);

    TableView<T> create() throws PulsarClientException;

    CompletableFuture<TableView<T>> createAsync();

    TableViewBuilder<T> topic(String topic);

    /**
     * Set the interval of updating partitions <i>(default: 1 minute)</i>.
     *
     * @param interval
     *            the interval of updating partitions
     * @param unit
     *            the time unit of the interval.
     * @return the consumer builder instance
     */
    TableViewBuilder<T> autoUpdatePartitionsInterval(int interval, TimeUnit unit);
}
