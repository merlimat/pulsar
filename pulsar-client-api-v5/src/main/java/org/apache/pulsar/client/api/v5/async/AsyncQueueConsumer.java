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

import java.util.concurrent.CompletableFuture;
import org.apache.pulsar.client.api.v5.Message;
import org.apache.pulsar.client.api.v5.MessageId;
import org.apache.pulsar.client.api.v5.QueueConsumer;
import org.apache.pulsar.client.api.v5.Transaction;

/**
 * Asynchronous view of a {@link QueueConsumer}.
 *
 * <p>All operations return {@link CompletableFuture} and never block.
 * Obtained via {@link QueueConsumer#async()}.
 *
 * @param <T> the type of message values
 */
public interface AsyncQueueConsumer<T> {

    /**
     * Receive a single message asynchronously.
     */
    CompletableFuture<Message<T>> receive();

    /**
     * Acknowledge a single message by its ID.
     */
    void acknowledge(MessageId messageId);

    /**
     * Acknowledge a single message.
     */
    void acknowledge(Message<T> message);

    /**
     * Acknowledge within a transaction. The acknowledgment becomes effective when the
     * transaction is committed.
     */
    void acknowledge(MessageId messageId, Transaction txn);

    /**
     * Signal that this message could not be processed. It will be redelivered later.
     */
    void negativeAcknowledge(MessageId messageId);

    /**
     * Signal that this message could not be processed. It will be redelivered later.
     */
    void negativeAcknowledge(Message<T> message);

    /**
     * Close this consumer asynchronously.
     */
    CompletableFuture<Void> close();
}
