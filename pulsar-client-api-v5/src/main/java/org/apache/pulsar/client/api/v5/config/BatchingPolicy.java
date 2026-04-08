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

/**
 * Configuration for producer message batching.
 *
 * <p>When batching is enabled, the producer groups multiple messages into a single
 * broker request to improve throughput. A batch is flushed when any of the
 * configured thresholds is reached.
 *
 * @param enabled        whether batching is enabled
 * @param maxPublishDelay maximum time to wait before flushing a batch
 * @param maxMessages    maximum number of messages in a single batch
 * @param maxSize       maximum size of a single batch
 */
public record BatchingPolicy(
        boolean enabled,
        Duration maxPublishDelay,
        int maxMessages,
        MemorySize maxSize
) {
    public BatchingPolicy {
        if (maxPublishDelay == null) {
            maxPublishDelay = Duration.ofMillis(1);
        }
        if (maxMessages < 0) {
            throw new IllegalArgumentException("maxMessages must be >= 0");
        }
        if (maxSize.bytes() < 0) {
            throw new IllegalArgumentException("maxBytes must be >= 0");
        }
    }

    private static final BatchingPolicy DISABLED =
            new BatchingPolicy(false, Duration.ofMillis(1), 1000, MemorySize.ofKilobytes(128));
    private static final BatchingPolicy DEFAULT =
            new BatchingPolicy(true, Duration.ofMillis(1), 1000, MemorySize.ofKilobytes(128));

    /**
     * Batching disabled.
     *
     * @return a {@link BatchingPolicy} with batching disabled
     */
    public static BatchingPolicy ofDisabled() {
        return DISABLED;
    }

    /**
     * Batching enabled with default thresholds (1ms delay, 1000 messages, 128KB).
     *
     * @return a {@link BatchingPolicy} with default batching thresholds
     */
    public static BatchingPolicy ofDefault() {
        return DEFAULT;
    }

    /**
     * Batching enabled with custom thresholds.
     *
     * @param maxPublishDelay the maximum time to wait before flushing a batch
     * @param maxMessages     the maximum number of messages in a single batch
     * @param maxSize         the maximum size of a single batch
     * @return a {@link BatchingPolicy} with batching enabled and the specified thresholds
     */
    public static BatchingPolicy of(Duration maxPublishDelay, int maxMessages, MemorySize maxSize) {
        return new BatchingPolicy(true, maxPublishDelay, maxMessages, maxSize);
    }
}
