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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import org.apache.pulsar.client.api.v5.config.BackoffPolicy;
import org.apache.pulsar.client.api.v5.config.DeadLetterPolicy;
import org.apache.pulsar.client.api.v5.config.EncryptionPolicy;
import org.apache.pulsar.client.api.v5.config.SubscriptionInitialPosition;

/**
 * Builder for configuring and creating a {@link QueueConsumer}.
 *
 * @param <T> the type of message values the consumer will receive
 */
public interface QueueConsumerBuilder<T> {

    /**
     * Subscribe and create the queue consumer, blocking until ready.
     */
    QueueConsumer<T> subscribe() throws PulsarClientException;

    /**
     * Subscribe and create the queue consumer asynchronously.
     */
    CompletableFuture<QueueConsumer<T>> subscribeAsync();

    // --- Topic selection ---

    /**
     * The topic(s) to subscribe to.
     */
    QueueConsumerBuilder<T> topic(String... topicNames);

    /**
     * The topics to subscribe to.
     */
    QueueConsumerBuilder<T> topics(List<String> topicNames);

    /**
     * Subscribe to all topics matching a regex pattern.
     */
    QueueConsumerBuilder<T> topicsPattern(Pattern pattern);

    /**
     * Subscribe to all topics matching a regex pattern (string form).
     */
    QueueConsumerBuilder<T> topicsPattern(String regex);

    // --- Subscription ---

    /**
     * The subscription name. Required for managed consumers.
     */
    QueueConsumerBuilder<T> subscriptionName(String subscriptionName);

    /**
     * Properties to attach to the subscription.
     */
    QueueConsumerBuilder<T> subscriptionProperties(Map<String, String> properties);

    /**
     * Initial position when the subscription is first created.
     */
    QueueConsumerBuilder<T> subscriptionInitialPosition(SubscriptionInitialPosition position);

    // --- Consumer identity ---

    /**
     * A custom name for this consumer instance.
     */
    QueueConsumerBuilder<T> consumerName(String consumerName);

    /**
     * Size of the receiver queue. Controls prefetch depth.
     */
    QueueConsumerBuilder<T> receiverQueueSize(int receiverQueueSize);

    /**
     * Priority level for this consumer (lower values mean higher priority for
     * message dispatch).
     */
    QueueConsumerBuilder<T> priorityLevel(int priorityLevel);

    // --- Acknowledgment ---

    /**
     * If a message is not acknowledged within this duration, it is automatically redelivered.
     * Set to zero to disable.
     */
    QueueConsumerBuilder<T> ackTimeout(Duration timeout);

    /**
     * How frequently acknowledgments are flushed to the broker.
     */
    QueueConsumerBuilder<T> acknowledgmentGroupTime(Duration delay);

    /**
     * Maximum number of acknowledgments to group before flushing.
     */
    QueueConsumerBuilder<T> maxAcknowledgmentGroupSize(int size);

    // --- Redelivery ---

    /**
     * Backoff strategy for redelivery after negative acknowledgment.
     *
     * @see BackoffPolicy#fixed(Duration)
     * @see BackoffPolicy#exponential(Duration, Duration)
     */
    QueueConsumerBuilder<T> negativeAckRedeliveryBackoff(BackoffPolicy backoff);

    /**
     * Backoff strategy for redelivery after ack timeout.
     *
     * @see BackoffPolicy#fixed(Duration)
     * @see BackoffPolicy#exponential(Duration, Duration)
     */
    QueueConsumerBuilder<T> ackTimeoutRedeliveryBackoff(BackoffPolicy backoff);

    // --- Dead letter queue ---

    /**
     * Configure the dead letter queue policy.
     */
    QueueConsumerBuilder<T> deadLetterPolicy(DeadLetterPolicy policy);

    // --- Pattern subscription ---

    /**
     * How often to re-discover topics matching the pattern.
     */
    QueueConsumerBuilder<T> patternAutoDiscoveryPeriod(Duration interval);

    // --- Encryption ---

    /**
     * Configure end-to-end message encryption for decryption.
     *
     * @see EncryptionPolicy#forConsumer
     */
    QueueConsumerBuilder<T> encryptionPolicy(EncryptionPolicy policy);


    // --- Misc ---

    /**
     * Add a single property to the consumer metadata.
     */
    QueueConsumerBuilder<T> property(String key, String value);

    /**
     * Add multiple properties to the consumer metadata.
     */
    QueueConsumerBuilder<T> properties(Map<String, String> properties);
}
