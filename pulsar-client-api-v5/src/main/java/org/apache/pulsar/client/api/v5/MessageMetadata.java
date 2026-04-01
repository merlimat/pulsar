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
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Common message metadata that can be set on any outgoing message.
 *
 * <p>This is the shared base for {@link MessageBuilder} (sync) and
 * {@link org.apache.pulsar.client.api.v5.async.AsyncMessageBuilder AsyncMessageBuilder} (async).
 * The self-referential type parameter {@code BUILDER} enables fluent chaining
 * on both subtypes.
 *
 * @param <T>    the type of the message value
 * @param <BUILDER> the concrete builder type (for fluent returns)
 */
public interface MessageMetadata<T, BUILDER extends MessageMetadata<T, BUILDER>> {

    /**
     * Set the message value.
     */
    BUILDER value(T value);

    /**
     * Set the message key. Messages with the same key are guaranteed to be delivered
     * in order to stream consumers. Queue consumers may use the key for routing.
     */
    BUILDER key(String key);

    /**
     * Associate this message with a transaction.
     */
    BUILDER transaction(Transaction txn);

    /**
     * Add a single property to the message.
     */
    BUILDER property(String name, String value);

    /**
     * Add multiple properties to the message.
     */
    BUILDER properties(Map<String, String> properties);

    /**
     * Set the event time of the message.
     */
    BUILDER eventTime(Instant eventTime);

    /**
     * Set the sequence ID for producer deduplication.
     */
    BUILDER sequenceId(long sequenceId);

    /**
     * Request delayed delivery: the message becomes visible to consumers after the given delay.
     */
    BUILDER deliverAfter(Duration delay);

    /**
     * Request delayed delivery: the message becomes visible to consumers at the given time.
     */
    BUILDER deliverAt(Instant timestamp);

    /**
     * Restrict geo-replication to the specified clusters only.
     */
    BUILDER replicationClusters(List<String> clusters);
}
