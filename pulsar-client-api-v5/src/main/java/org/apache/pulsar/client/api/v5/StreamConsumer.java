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

import java.io.Closeable;
import java.time.Duration;
import org.apache.pulsar.client.api.v5.async.AsyncStreamConsumer;

/**
 * A consumer for streaming (ordered) consumption with broker-managed position tracking.
 *
 * <p>Messages are delivered in order (per-key if keyed). Acknowledgment is cumulative only:
 * acknowledging a message ID means all messages up to and including that ID are acknowledged.
 *
 * <p>This interface provides synchronous (blocking) operations. For non-blocking
 * usage, obtain an {@link AsyncStreamConsumer} via {@link #async()}.
 *
 * <p>This maps to the Exclusive/Failover subscription model in the Pulsar v4 API.
 *
 * @param <T> the type of message values
 */
public interface StreamConsumer<T> extends Closeable {

    /**
     * The topic this consumer is subscribed to.
     */
    String topic();

    /**
     * The subscription name.
     */
    String subscription();

    /**
     * The consumer name (system-assigned or user-specified).
     */
    String consumerName();

    /**
     * Receive a single message, blocking indefinitely.
     */
    Message<T> receive() throws PulsarClientException;

    /**
     * Receive a single message, blocking up to the given timeout.
     * Returns {@code null} if the timeout elapses without a message.
     */
    Message<T> receive(Duration timeout) throws PulsarClientException;

    Messages<T> receiveMulti(int maxNumMessages, Duration timeout) throws PulsarClientException;

    /**
     * Acknowledge all messages up to and including the given message ID.
     */
    void acknowledgeCumulative(MessageId messageId);

    /**
     * Acknowledge within a transaction. The acknowledgment becomes effective when the
     * transaction is committed.
     */
    void acknowledgeCumulative(MessageId messageId, Transaction txn);

    /**
     * Return the asynchronous view of this consumer.
     */
    AsyncStreamConsumer<T> async();

    // --- Lifecycle ---

    @Override
    void close() throws PulsarClientException;
}
