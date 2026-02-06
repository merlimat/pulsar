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

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.apache.pulsar.client.api.v5.config.BatchingPolicy;
import org.apache.pulsar.client.api.v5.config.ChunkingPolicy;
import org.apache.pulsar.client.api.v5.config.CompressionPolicy;
import org.apache.pulsar.client.api.v5.config.EncryptionPolicy;
import org.apache.pulsar.client.api.v5.config.ProducerAccessMode;

/**
 * Builder for configuring and creating a {@link Producer}.
 *
 * @param <T> the type of message values the producer will send
 */
public interface ProducerBuilder<T> {

    /**
     * Create the producer, blocking until it is ready.
     */
    Producer<T> create() throws PulsarClientException;

    /**
     * Create the producer asynchronously.
     */
    CompletableFuture<Producer<T>> createAsync();

    // --- Required ---

    /**
     * The topic to produce to.
     */
    ProducerBuilder<T> topic(String topicName);

    // --- Optional ---

    /**
     * Set a custom producer name. If not set, the broker assigns a unique name.
     */
    ProducerBuilder<T> producerName(String producerName);

    /**
     * Access mode for this producer on the topic.
     */
    ProducerBuilder<T> accessMode(ProducerAccessMode accessMode);

    /**
     * Timeout for a send operation. If the message is not acknowledged within this
     * duration, the send future completes exceptionally.
     */
    ProducerBuilder<T> sendTimeout(Duration timeout);

    /**
     * Whether the producer should block when the pending message queue is full,
     * rather than failing immediately. Default is {@code true}.
     */
    ProducerBuilder<T> blockIfQueueFull(boolean blockIfQueueFull);

    /**
     * Configure compression for message payloads.
     *
     * @see CompressionPolicy#of(org.apache.pulsar.client.api.v5.config.CompressionType)
     * @see CompressionPolicy#disabled()
     */
    ProducerBuilder<T> compressionPolicy(CompressionPolicy policy);

    /**
     * Configure message batching. When enabled, the producer groups multiple messages
     * into a single broker request to improve throughput.
     *
     * @see BatchingPolicy#ofDefault()
     * @see BatchingPolicy#ofDisabled()
     * @see BatchingPolicy#of(Duration, int, int)
     */
    ProducerBuilder<T> batchingPolicy(BatchingPolicy policy);

    /**
     * Enable chunking for large messages that exceed the broker's max message size.
     */
    ProducerBuilder<T> chunkingPolicy(ChunkingPolicy policy);

    /**
     * Configure end-to-end message encryption.
     *
     * @see EncryptionPolicy#forProducer(org.apache.pulsar.client.api.v5.auth.CryptoKeyReader, String...)
     */
    ProducerBuilder<T> encryptionPolicy(EncryptionPolicy policy);

    /**
     * Set the initial sequence ID for producer message deduplication.
     */
    ProducerBuilder<T> initialSequenceId(long initialSequenceId);

    // --- Metadata ---

    /**
     * Add a single property to the producer metadata.
     */
    ProducerBuilder<T> property(String key, String value);

    /**
     * Add multiple properties to the producer metadata.
     */
    ProducerBuilder<T> properties(Map<String, String> properties);
}
