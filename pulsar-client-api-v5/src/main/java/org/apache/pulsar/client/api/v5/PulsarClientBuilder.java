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
     *
     * @return the configured {@link PulsarClient} instance
     * @throws PulsarClientException if the client cannot be created (e.g., invalid configuration
     *         or connection failure)
     */
    PulsarClient build() throws PulsarClientException;

    // --- Connection ---

    /**
     * Set the Pulsar service URL (e.g., {@code pulsar://localhost:6650}).
     *
     * @param serviceUrl the Pulsar service URL to connect to
     * @return this builder instance for chaining
     */
    PulsarClientBuilder serviceUrl(String serviceUrl);

    /**
     * Set the authentication provider.
     *
     * @param authentication the authentication provider to use for connecting to the broker
     * @return this builder instance for chaining
     */
    PulsarClientBuilder authentication(Authentication authentication);

    /**
     * Set authentication by plugin class name and parameter string.
     *
     * @param authPluginClassName the fully qualified class name of the authentication plugin
     * @param authParamsString the authentication parameters as a serialized string
     * @return this builder instance for chaining
     * @throws PulsarClientException if the authentication plugin cannot be loaded or configured
     */
    PulsarClientBuilder authentication(String authPluginClassName, String authParamsString)
            throws PulsarClientException;

    // --- Timeouts ---

    /**
     * Timeout for client operations (e.g., creating producers/consumers).
     *
     * @param timeout the maximum duration to wait for an operation to complete
     * @return this builder instance for chaining
     */
    PulsarClientBuilder operationTimeout(Duration timeout);

    /**
     * Timeout for establishing a TCP connection to the broker.
     *
     * @param timeout the maximum duration to wait for a TCP connection to be established
     * @return this builder instance for chaining
     */
    PulsarClientBuilder connectionTimeout(Duration timeout);

    // --- Threading ---

    /**
     * Number of I/O threads for managing connections and reading data.
     *
     * @param numIoThreads the number of I/O threads to use
     * @return this builder instance for chaining
     */
    PulsarClientBuilder ioThreads(int numIoThreads);

    /**
     * Number of threads for callbacks.
     *
     * @param numCallbackThreads the number of threads dedicated to message listener callbacks
     * @return this builder instance for chaining
     */
    PulsarClientBuilder callbackThreads(int numCallbackThreads);

    // --- Connection pool ---

    /**
     * Maximum number of TCP connections per broker.
     *
     * @param connectionsPerBroker the maximum number of connections to maintain per broker
     * @return this builder instance for chaining
     */
    PulsarClientBuilder connectionsPerBroker(int connectionsPerBroker);

    /**
     * Enable TCP no-delay (disable Nagle's algorithm).
     *
     * @param enableTcpNoDelay {@code true} to enable TCP no-delay, {@code false} to use Nagle's algorithm
     * @return this builder instance for chaining
     */
    PulsarClientBuilder enableTcpNoDelay(boolean enableTcpNoDelay);

    /**
     * Interval for sending keep-alive probes on idle connections.
     *
     * @param interval the duration between keep-alive probes
     * @return this builder instance for chaining
     */
    PulsarClientBuilder keepAliveInterval(Duration interval);

    /**
     * Maximum idle time before a connection is closed.
     *
     * @param duration the maximum idle duration before a connection is eligible for closure
     * @return this builder instance for chaining
     */
    PulsarClientBuilder connectionMaxIdleTime(Duration duration);

    /**
     * Set the transaction policy.
     *
     * @param policy the transaction policy controlling transaction behavior and timeouts
     * @return this builder instance for chaining
     */
    PulsarClientBuilder transactionPolicy(TransactionPolicy policy);

    // --- TLS ---

    /**
     * Configure TLS for the client connection.
     *
     * @param policy the TLS policy to apply to broker connections
     * @return this builder instance for chaining
     * @see TlsPolicy#of(String)
     * @see TlsPolicy#ofMutualTls(String, String, String)
     * @see TlsPolicy#ofInsecure()
     */
    PulsarClientBuilder tlsPolicy(TlsPolicy policy);

    // --- Proxy ---

    /**
     * Connect through a proxy.
     *
     * @param proxyServiceUrl the URL of the proxy service
     * @param proxyProtocol the protocol to use when connecting through the proxy
     * @return this builder instance for chaining
     */
    PulsarClientBuilder proxyServiceUrl(String proxyServiceUrl, ProxyProtocol proxyProtocol);

    // --- Observability ---

    /**
     * Set the OpenTelemetry instance for metrics and tracing.
     *
     * @param openTelemetry the OpenTelemetry instance to use for emitting metrics and traces
     * @return this builder instance for chaining
     */
    PulsarClientBuilder openTelemetry(OpenTelemetry openTelemetry);

    /**
     * Enable distributed tracing.
     *
     * @param enable {@code true} to enable distributed tracing, {@code false} to disable it
     * @return this builder instance for chaining
     */
    PulsarClientBuilder enableTracing(boolean enable);

    // --- Memory ---

    /**
     * Maximum amount of direct memory the client can use for pending messages.
     *
     * @param size the memory limit for pending messages across all producers
     * @return this builder instance for chaining
     * @see MemorySize#ofMegabytes(long)
     * @see MemorySize#ofGigabytes(long)
     */
    PulsarClientBuilder memoryLimit(MemorySize size);

    // --- Misc ---

    /**
     * Set the listener name for multi-listener brokers.
     *
     * @param name the listener name to use when connecting to brokers that advertise
     *        multiple listener endpoints
     * @return this builder instance for chaining
     */
    PulsarClientBuilder listenerName(String name);

    /**
     * A human-readable description of this client (for logging and debugging).
     *
     * @param description a descriptive label for this client instance
     * @return this builder instance for chaining
     */
    PulsarClientBuilder description(String description);

    // --- Reconnection backoff ---

    /**
     * Configure the backoff strategy for broker reconnection attempts.
     *
     * @param backoff the backoff policy to use when reconnecting to the broker
     * @return this builder instance for chaining
     * @see BackoffPolicy#exponential(Duration, Duration)
     */
    PulsarClientBuilder connectionBackoff(BackoffPolicy backoff);
}
