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

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * An immutable message received from a Pulsar topic.
 *
 * @param <T> the type of the deserialized message value
 */
public interface Message<T> {

    /**
     * The deserialized value of the message according to the schema.
     */
    T value();

    /**
     * The raw bytes of the message payload.
     */
    byte[] data();

    /**
     * The unique identifier of this message within the topic.
     */
    MessageId id();

    /**
     * The message key, used for per-key ordering.
     */
    Optional<String> key();

    /**
     * Application-defined properties attached to the message.
     */
    Map<String, String> properties();

    /**
     * The timestamp when the message was published by the broker.
     */
    Instant publishTime();

    /**
     * The event time set by the producer, if any.
     */
    Optional<Instant> eventTime();

    /**
     * The producer-assigned sequence ID for deduplication.
     */
    long sequenceId();

    /**
     * The name of the producer that published this message.
     */
    Optional<String> producerName();

    /**
     * The topic this message was published to.
     */
    String topic();

    /**
     * The number of times the broker has redelivered this message.
     */
    int redeliveryCount();

    /**
     * The uncompressed size of the message payload in bytes.
     */
    int size();

    /**
     * The cluster from which this message was replicated, if applicable.
     */
    Optional<String> replicatedFrom();
}
