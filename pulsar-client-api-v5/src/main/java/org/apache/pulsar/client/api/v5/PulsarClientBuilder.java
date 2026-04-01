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

import io.opentelemetry.api.OpenTelemetry;
import java.time.Duration;
import org.apache.pulsar.client.api.v5.auth.Authentication;
import org.apache.pulsar.client.api.v5.config.BackoffPolicy;
import org.apache.pulsar.client.api.v5.config.MemorySize;
import org.apache.pulsar.client.api.v5.config.ProxyProtocol;
import org.apache.pulsar.client.api.v5.config.TlsPolicy;
import org.apache.pulsar.client.api.v5.config.TransactionPolicy;

/**
 * Builder for configuring and creating a {@link PulsarClient}.
 */
public interface PulsarClientBuilder {

    /**
     * Build and return the configured client.
     */
    PulsarClient build() throws PulsarClientException;

    // --- Connection ---

    /**
     * Set the Pulsar service URL (e.g., {@code pulsar://localhost:6650}).
     */
    PulsarClientBuilder serviceUrl(String serviceUrl);

    /**
     * Set the authentication provider.
     */
    PulsarClientBuilder authentication(Authentication authentication);

    /**
     * Set authentication by plugin class name and parameter string.
     */
    PulsarClientBuilder authentication(String authPluginClassName, String authParamsString)
            throws PulsarClientException;

    // --- Timeouts ---

    /**
     * Timeout for client operations (e.g., creating producers/consumers).
     */
    PulsarClientBuilder operationTimeout(Duration timeout);

    /**
     * Timeout for establishing a TCP connection to the broker.
     */
    PulsarClientBuilder connectionTimeout(Duration timeout);

    // --- Threading ---

    /**
     * Number of I/O threads for managing connections and reading data.
     */
    PulsarClientBuilder ioThreads(int numIoThreads);

    /**
     * Number of threads for callbacks.
     */
    PulsarClientBuilder callbackThreads(int numCallbackThreads);

    // --- Connection pool ---

    /**
     * Maximum number of TCP connections per broker.
     */
    PulsarClientBuilder connectionsPerBroker(int connectionsPerBroker);

    /**
     * Enable TCP no-delay (disable Nagle's algorithm).
     */
    PulsarClientBuilder enableTcpNoDelay(boolean enableTcpNoDelay);

    /**
     * Interval for sending keep-alive probes on idle connections.
     */
    PulsarClientBuilder keepAliveInterval(Duration interval);

    /**
     * Maximum idle time before a connection is closed.
     */
    PulsarClientBuilder connectionMaxIdleTime(Duration duration);

    /**
     * Set the transaction policy.
     */
    PulsarClientBuilder transactionPolicy(TransactionPolicy policy);

    // --- TLS ---

    /**
     * Configure TLS for the client connection.
     *
     * @see TlsPolicy#of(String)
     * @see TlsPolicy#ofMutualTls(String, String, String)
     * @see TlsPolicy#ofInsecure()
     */
    PulsarClientBuilder tlsPolicy(TlsPolicy policy);

    // --- Proxy ---

    /**
     * Connect through a proxy.
     */
    PulsarClientBuilder proxyServiceUrl(String proxyServiceUrl, ProxyProtocol proxyProtocol);

    // --- Observability ---

    /**
     * Set the OpenTelemetry instance for metrics and tracing.
     */
    PulsarClientBuilder openTelemetry(OpenTelemetry openTelemetry);

    /**
     * Enable distributed tracing.
     */
    PulsarClientBuilder enableTracing(boolean enable);

    // --- Memory ---

    /**
     * Maximum amount of direct memory the client can use for pending messages.
     *
     * @see MemorySize#ofMegabytes(long)
     * @see MemorySize#ofGigabytes(long)
     */
    PulsarClientBuilder memoryLimit(MemorySize size);

    // --- Misc ---

    /**
     * Set the listener name for multi-listener brokers.
     */
    PulsarClientBuilder listenerName(String name);

    /**
     * A human-readable description of this client (for logging and debugging).
     */
    PulsarClientBuilder description(String description);

    // --- Reconnection backoff ---

    /**
     * Configure the backoff strategy for broker reconnection attempts.
     *
     * @see BackoffPolicy#exponential(Duration, Duration)
     */
    PulsarClientBuilder connectionBackoff(BackoffPolicy backoff);
}
