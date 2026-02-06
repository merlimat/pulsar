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

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.apache.pulsar.client.api.v5.config.EncryptionPolicy;

/**
 * Builder for configuring and creating a {@link CheckpointConsumer}.
 *
 * <p>Since this is an unmanaged consumer (no subscription), the terminal method is
 * {@link #create()} rather than {@code subscribe()}.
 *
 * @param <T> the type of message values the consumer will receive
 */
public interface CheckpointConsumerBuilder<T> {

    /**
     * Create the checkpoint consumer, blocking until it is ready.
     */
    CheckpointConsumer<T> create() throws PulsarClientException;

    /**
     * Create the checkpoint consumer asynchronously.
     */
    CompletableFuture<CheckpointConsumer<T>> createAsync();

    // --- Required ---

    /**
     * The topic to consume from.
     */
    CheckpointConsumerBuilder<T> topic(String topicName);

    // --- Start position ---

    /**
     * Set the initial position for this consumer.
     *
     * <p>Use {@link Checkpoint#earliest()}, {@link Checkpoint#latest()},
     * {@link Checkpoint#atTimestamp}, or {@link Checkpoint#fromByteArray} to
     * create the appropriate starting position.
     *
     * <p>Defaults to {@link Checkpoint#latest()} if not specified.
     */
    CheckpointConsumerBuilder<T> startPosition(Checkpoint checkpoint);

    // --- Optional ---

    /**
     * A custom name for this consumer instance.
     */
    CheckpointConsumerBuilder<T> consumerName(String name);

    /**
     * Configure end-to-end message encryption for decryption.
     *
     * @see EncryptionPolicy#forConsumer
     */
    CheckpointConsumerBuilder<T> encryptionPolicy(EncryptionPolicy policy);

    // --- Metadata ---

    /**
     * Add a single property to the consumer metadata.
     */
    CheckpointConsumerBuilder<T> property(String key, String value);

    /**
     * Add multiple properties to the consumer metadata.
     */
    CheckpointConsumerBuilder<T> properties(Map<String, String> properties);
}
