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

import org.apache.pulsar.client.api.v5.async.AsyncProducer;

/**
 * A producer publishes messages to a Pulsar topic.
 *
 * <p>This interface provides synchronous (blocking) operations. For non-blocking
 * usage, obtain an {@link AsyncProducer} via {@link #async()}.
 *
 * @param <T> the type of message values this producer sends
 */
public interface Producer<T> extends AutoCloseable {

    /**
     * The topic this producer is attached to.
     */
    String topic();

    /**
     * The name of this producer (system-assigned or user-specified).
     */
    String producerName();

    /**
     * Create a message builder for advanced message construction (key, properties, etc.).
     * Use {@link MessageBuilder#send()} as the terminal operation.
     */
    MessageBuilder<T> newMessage();

    /**
     * The last sequence ID published by this producer (for deduplication tracking).
     */
    long lastSequenceId();

    /**
     * Return the asynchronous view of this producer.
     */
    AsyncProducer<T> async();

    @Override
    void close() throws PulsarClientException;
}
