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
package org.apache.pulsar.client.api.v5.async;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.pulsar.client.api.v5.Message;
import org.apache.pulsar.client.api.v5.MessageId;
import org.apache.pulsar.client.api.v5.StreamConsumer;
import org.apache.pulsar.client.api.v5.Transaction;

/**
 * Asynchronous view of a {@link StreamConsumer}.
 *
 * <p>All operations return {@link CompletableFuture} and never block.
 * Obtained via {@link StreamConsumer#async()}.
 *
 * @param <T> the type of message values
 */
public interface AsyncStreamConsumer<T> {

    /**
     * Receive a single message asynchronously.
     */
    CompletableFuture<Message<T>> receive();

    /**
     * Receive a single message, blocking up to the given timeout.
     * Returns {@code null} if the timeout elapses without a message.
     */
    CompletableFuture<Message<T>> receive(Duration timeout);

    /**
     * Receive a batch of messages asynchronously.
     *
     * @param maxNumMessages maximum number of messages to return
     * @param timeout        maximum time to wait for messages
     */
    CompletableFuture<List<Message<T>>> receiveMulti(int maxNumMessages, Duration timeout);

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
     * Close this consumer asynchronously.
     */
    CompletableFuture<Void> close();
}
