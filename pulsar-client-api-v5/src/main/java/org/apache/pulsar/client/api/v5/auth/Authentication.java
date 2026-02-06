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
package org.apache.pulsar.client.api.v5.auth;

import org.apache.pulsar.client.api.v5.PulsarClientException;
import java.io.Closeable;

/**
 * Pluggable authentication provider for Pulsar clients.
 *
 * <p>Implementations must be thread-safe.
 */
public interface Authentication extends Closeable {

    /**
     * The authentication method name (e.g., "token", "tls").
     */
    String authMethodName();

    /**
     * Get the authentication data to be sent to the broker.
     */
    AuthenticationData authData() throws PulsarClientException;

    /**
     * Get the authentication data for a specific broker host.
     */
    default AuthenticationData authData(String brokerHostName) throws PulsarClientException {
        return authData();
    }

    /**
     * Initialize the authentication provider. Called once when the client is created.
     */
    default void initialize() throws PulsarClientException {
    }

    @Override
    default void close() {
    }
}
