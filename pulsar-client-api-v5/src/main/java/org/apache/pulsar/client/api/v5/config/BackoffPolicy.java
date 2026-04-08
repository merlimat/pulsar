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

import java.time.Duration;
import java.util.Objects;

/**
 * Backoff configuration for broker reconnection attempts.
 *
 * <p>The delay for attempt {@code n} is {@code min(initialInterval * multiplier^(n-1), maxInterval)}.
 *
 * @param initialInterval the delay before the first reconnection attempt
 * @param maxInterval     the maximum delay between reconnection attempts
 * @param multiplier      the multiplier applied after each attempt
 */
public record BackoffPolicy(
        Duration initialInterval,
        Duration maxInterval,
        double multiplier
) {
    public BackoffPolicy {
        Objects.requireNonNull(initialInterval, "initialInterval must not be null");
        Objects.requireNonNull(maxInterval, "maxInterval must not be null");
        if (multiplier < 1.0) {
            throw new IllegalArgumentException("multiplier must be >= 1.0");
        }
    }

    /**
     * Create a fixed backoff (no increase between retries).
     *
     * @param initialInterval the constant delay between reconnection attempts
     * @param maxInterval     the maximum delay between reconnection attempts
     * @return a {@link BackoffPolicy} with a multiplier of 1.0
     */
    public static BackoffPolicy fixed(Duration initialInterval, Duration maxInterval) {
        return new BackoffPolicy(initialInterval, maxInterval, 1.0);
    }

    /**
     * Create an exponential backoff with the given bounds and a default multiplier of 2.
     *
     * @param initialInterval the delay before the first reconnection attempt
     * @param maxInterval     the maximum delay between reconnection attempts
     * @return a {@link BackoffPolicy} with a multiplier of 2.0
     */
    public static BackoffPolicy exponential(Duration initialInterval, Duration maxInterval) {
        return new BackoffPolicy(initialInterval, maxInterval, 2.0);
    }

    /**
     * Create an exponential backoff with a custom multiplier.
     *
     * @param initialInterval the delay before the first reconnection attempt
     * @param maxInterval     the maximum delay between reconnection attempts
     * @param multiplier      the multiplier applied after each attempt, must be &gt;= 1.0
     * @return a {@link BackoffPolicy} with the specified parameters
     */
    public static BackoffPolicy exponential(Duration initialInterval, Duration maxInterval, double multiplier) {
        return new BackoffPolicy(initialInterval, maxInterval, multiplier);
    }
}
