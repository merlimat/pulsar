package org.apache.pulsar.client.impl;

import static com.google.common.base.Preconditions.checkArgument;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.TableView;
import org.apache.pulsar.client.api.TableViewBuilder;
import org.apache.pulsar.client.impl.conf.ConfigurationDataUtils;

public class TableViewBuilderImpl<T> implements TableViewBuilder<T> {

    private final PulsarClientImpl client;
    private TableViewConfigurationData conf;
    private final Schema<T> schema;

    TableViewBuilderImpl(PulsarClientImpl client, Schema<T> schema) {
        this.client = client;
        this.schema = schema;
        this.conf = new TableViewConfigurationData();
    }

    @Override
    public TableViewBuilder<T> loadConf(Map<String, Object> config) {
        conf = ConfigurationDataUtils.loadData(
                config, conf, TableViewConfigurationData.class);
        return this;
    }

    @Override
    public TableView<T> create() throws PulsarClientException {
        try {
            return createAsync().get();
        } catch (Exception e) {
            throw PulsarClientException.unwrap(e);
        }
    }

    @Override
    public CompletableFuture<TableView<T>> createAsync() {
        return new TableViewImpl<>(client, schema, conf).start();
    }

    @Override
    public TableViewBuilder<T> topic(String topic) {
        checkArgument(StringUtils.isNotBlank(topic), "topic cannot be blank");
        conf.setTopicName(StringUtils.trim(topic));
        return this;
    }

    @Override
    public TableViewBuilder<T> autoUpdatePartitionsInterval(int interval, TimeUnit unit) {
        checkArgument(unit.toSeconds(interval) >= 1, "minimum is 1 second");
        conf.setAutoUpdatePartitionsSeconds(unit.toSeconds(interval));
        return this;
    }
}
