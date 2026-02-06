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
package org.apache.pulsar.client.api.v5;

import org.apache.pulsar.client.api.v5.internal.PulsarClientProvider;
import org.apache.pulsar.client.api.v5.schema.Schema;

/**
 * Entry point for the Pulsar client. Provides factory methods for creating producers,
 * consumers, and transactions.
 *
 * <p>Instances are created via {@link #builder()}.
 *
 * <p>A {@code PulsarClient} manages internal resources such as connections, threads,
 * and memory buffers. It must be closed when no longer needed.
 */
public interface PulsarClient extends AutoCloseable {

    /**
     * Create a new client builder.
     */
    static PulsarClientBuilder builder() {
        return PulsarClientProvider.get().newClientBuilder();
    }

    /**
     * Create a producer builder with a specific schema.
     */
    <T> ProducerBuilder<T> newProducer(Schema<T> schema);

    /**
     * Create a stream consumer builder with a specific schema.
     */
    <T> StreamConsumerBuilder<T> newStreamConsumer(Schema<T> schema);

    /**
     * Create a queue consumer builder with a specific schema.
     */
    <T> QueueConsumerBuilder<T> newQueueConsumer(Schema<T> schema);

    /**
     * Create a checkpoint consumer builder with a specific schema.
     *
     * <p>Checkpoint consumers are unmanaged — position tracking is external.
     * Designed for connector frameworks (Flink, Spark) that manage their own state.
     */
    <T> CheckpointConsumerBuilder<T> newCheckpointConsumer(Schema<T> schema);

    // --- Transactions ---

    /**
     * Create a new transaction
     */
    Transaction newTransaction();

    // --- Lifecycle ---

    @Override
    void close() throws PulsarClientException;

    /**
     * Shutdown the client instance.
     *
     * <p>Release all resources used by the client, without waiting for pending operations to complete.
     */
    void shutdown();
}
