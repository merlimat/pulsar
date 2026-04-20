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
 * Connection-level settings for the Pulsar client.
 *
 * <p>Groups TCP connection timeout, connection pool sizing, keep-alive, idle timeout,
 * TCP no-delay, I/O and callback threading, and proxy configuration.
 */
public record ConnectionPolicy(
        Duration connectionTimeout,
        int connectionsPerBroker,
        boolean enableTcpNoDelay,
        Duration keepAliveInterval,
        Duration connectionMaxIdleTime,
        int ioThreads,
        int callbackThreads,
        String proxyServiceUrl,
        ProxyProtocol proxyProtocol,
        BackoffPolicy connectionBackoff
) {

    /**
     * Create a connection policy with the given parameters.
     *
     * @throws NullPointerException if {@code connectionTimeout}, {@code keepAliveInterval},
     *         {@code connectionMaxIdleTime}, or {@code connectionBackoff} is null
     * @throws IllegalArgumentException if {@code connectionsPerBroker}, {@code ioThreads},
     *         or {@code callbackThreads} is less than 1
     */
    public ConnectionPolicy {
        Objects.requireNonNull(connectionTimeout, "connectionTimeout must not be null");
        Objects.requireNonNull(keepAliveInterval, "keepAliveInterval must not be null");
        Objects.requireNonNull(connectionMaxIdleTime, "connectionMaxIdleTime must not be null");
        Objects.requireNonNull(connectionBackoff, "connectionBackoff must not be null");
        if (connectionsPerBroker < 1) {
            throw new IllegalArgumentException("connectionsPerBroker must be >= 1");
        }
        if (ioThreads < 1) {
            throw new IllegalArgumentException("ioThreads must be >= 1");
        }
        if (callbackThreads < 1) {
            throw new IllegalArgumentException("callbackThreads must be >= 1");
        }
    }

    /**
     * Create a builder for constructing a {@link ConnectionPolicy}.
     *
     * @return a new builder with sensible defaults
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link ConnectionPolicy}.
     */
    public static final class Builder {
        private Duration connectionTimeout = Duration.ofSeconds(10);
        private int connectionsPerBroker = 1;
        private boolean enableTcpNoDelay = true;
        private Duration keepAliveInterval = Duration.ofSeconds(30);
        private Duration connectionMaxIdleTime = Duration.ofMinutes(3);
        private int ioThreads = 1;
        private int callbackThreads = 1;
        private String proxyServiceUrl;
        private ProxyProtocol proxyProtocol;
        private BackoffPolicy connectionBackoff =
                BackoffPolicy.exponential(Duration.ofMillis(100), Duration.ofSeconds(60));

        private Builder() {
        }

        /**
         * Timeout for establishing a TCP connection to the broker.
         *
         * @param connectionTimeout the maximum duration to wait for a TCP connection
         * @return this builder
         */
        public Builder connectionTimeout(Duration connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        /**
         * Maximum number of TCP connections per broker.
         *
         * @param connectionsPerBroker the number of connections to maintain per broker
         * @return this builder
         */
        public Builder connectionsPerBroker(int connectionsPerBroker) {
            this.connectionsPerBroker = connectionsPerBroker;
            return this;
        }

        /**
         * Enable TCP no-delay (disable Nagle's algorithm). Default is {@code true}.
         *
         * @param enableTcpNoDelay {@code true} to enable TCP no-delay
         * @return this builder
         */
        public Builder enableTcpNoDelay(boolean enableTcpNoDelay) {
            this.enableTcpNoDelay = enableTcpNoDelay;
            return this;
        }

        /**
         * Interval for sending keep-alive probes on idle connections.
         *
         * @param keepAliveInterval the duration between keep-alive probes
         * @return this builder
         */
        public Builder keepAliveInterval(Duration keepAliveInterval) {
            this.keepAliveInterval = keepAliveInterval;
            return this;
        }

        /**
         * Maximum idle time before a connection is closed.
         *
         * @param connectionMaxIdleTime the maximum idle duration
         * @return this builder
         */
        public Builder connectionMaxIdleTime(Duration connectionMaxIdleTime) {
            this.connectionMaxIdleTime = connectionMaxIdleTime;
            return this;
        }

        /**
         * Number of I/O threads for managing connections and reading data.
         *
         * @param ioThreads the number of I/O threads
         * @return this builder
         */
        public Builder ioThreads(int ioThreads) {
            this.ioThreads = ioThreads;
            return this;
        }

        /**
         * Number of threads for message listener callbacks.
         *
         * @param callbackThreads the number of callback threads
         * @return this builder
         */
        public Builder callbackThreads(int callbackThreads) {
            this.callbackThreads = callbackThreads;
            return this;
        }

        /**
         * Connect through a proxy.
         *
         * @param proxyServiceUrl the URL of the proxy service
         * @param proxyProtocol   the protocol to use when connecting through the proxy
         * @return this builder
         */
        public Builder proxy(String proxyServiceUrl, ProxyProtocol proxyProtocol) {
            this.proxyServiceUrl = proxyServiceUrl;
            this.proxyProtocol = proxyProtocol;
            return this;
        }

        /**
         * Backoff strategy for broker reconnection attempts.
         *
         * @param connectionBackoff the backoff policy to use when reconnecting to the broker
         * @return this builder
         * @see BackoffPolicy#exponential(Duration, Duration)
         */
        public Builder connectionBackoff(BackoffPolicy connectionBackoff) {
            this.connectionBackoff = connectionBackoff;
            return this;
        }

        /**
         * Build the {@link ConnectionPolicy}.
         *
         * @return a new {@link ConnectionPolicy} instance
         */
        public ConnectionPolicy build() {
            return new ConnectionPolicy(
                    connectionTimeout,
                    connectionsPerBroker,
                    enableTcpNoDelay,
                    keepAliveInterval,
                    connectionMaxIdleTime,
                    ioThreads,
                    callbackThreads,
                    proxyServiceUrl,
                    proxyProtocol,
                    connectionBackoff
            );
        }
    }
}
