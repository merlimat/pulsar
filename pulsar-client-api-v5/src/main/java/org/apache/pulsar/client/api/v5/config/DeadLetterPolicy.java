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
package org.apache.pulsar.client.api.v5.config;

/**
 * Configuration for the dead letter queue mechanism.
 *
 * <p>When a message has been redelivered more than {@code maxRedeliverCount} times,
 * it is moved to the dead letter topic instead of being redelivered again.
 *
 * @param maxRedeliverCount     maximum number of redelivery attempts before sending to the dead letter topic
 * @param retryLetterTopic      custom topic for retry messages (nullable, auto-generated if null)
 * @param deadLetterTopic       custom topic for dead letter messages (nullable, auto-generated if null)
 * @param initialSubscriptionName subscription name to create on the dead letter topic (nullable)
 */
public record DeadLetterPolicy(
        int maxRedeliverCount,
        String retryLetterTopic,
        String deadLetterTopic,
        String initialSubscriptionName
) {
    public DeadLetterPolicy {
        if (maxRedeliverCount < 0) {
            throw new IllegalArgumentException("maxRedeliverCount must be >= 0");
        }
    }

    /**
     * Create a dead letter policy with just a max redeliver count, using default topic names.
     *
     * @param maxRedeliverCount the maximum number of redelivery attempts before sending to the dead letter topic
     * @return a {@link DeadLetterPolicy} with auto-generated topic names and no initial subscription
     */
    public static DeadLetterPolicy of(int maxRedeliverCount) {
        return new DeadLetterPolicy(maxRedeliverCount, null, null, null);
    }
}
