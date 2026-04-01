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
import org.apache.pulsar.client.api.v5.internal.PulsarClientProvider;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Factory for creating common authentication providers.
 */
public final class AuthenticationFactory {

    private AuthenticationFactory() {
    }

    /**
     * Create token-based authentication with a static token.
     */
    public static Authentication token(String token) {
        return PulsarClientProvider.get().authenticationToken(token);
    }

    /**
     * Create token-based authentication with a dynamic token supplier.
     */
    public static Authentication token(Supplier<String> tokenSupplier) {
        return PulsarClientProvider.get().authenticationToken(tokenSupplier);
    }

    /**
     * Create TLS mutual authentication.
     */
    public static Authentication tls(String certFilePath, String keyFilePath) {
        return PulsarClientProvider.get().authenticationTls(certFilePath, keyFilePath);
    }

    /**
     * Create an authentication provider by plugin class name and parameter string.
     */
    public static Authentication create(String authPluginClassName, String authParamsString)
            throws PulsarClientException {
        return PulsarClientProvider.get().createAuthentication(authPluginClassName, authParamsString);
    }

    /**
     * Create an authentication provider by plugin class name and parameter map.
     */
    public static Authentication create(String authPluginClassName, Map<String, String> authParams)
            throws PulsarClientException {
        return PulsarClientProvider.get().createAuthentication(authPluginClassName, authParams);
    }
}
