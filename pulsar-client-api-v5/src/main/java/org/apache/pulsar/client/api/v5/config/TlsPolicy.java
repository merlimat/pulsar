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
 * TLS configuration for the Pulsar client connection.
 *
 * @param trustCertsFilePath          path to the trusted CA certificate file (PEM format)
 * @param keyFilePath                 path to the client private key file (PEM format), or {@code null}
 * @param certificateFilePath         path to the client certificate file (PEM format), or {@code null}
 * @param allowInsecureConnection     whether to allow connecting to brokers with untrusted certificates
 * @param enableHostnameVerification  whether to verify the broker hostname against the certificate
 */
public record TlsPolicy(
        String trustCertsFilePath,
        String keyFilePath,
        String certificateFilePath,
        boolean allowInsecureConnection,
        boolean enableHostnameVerification
) {
    /**
     * Create a TLS policy that trusts the given CA certificate and verifies hostnames.
     *
     * @param trustCertsFilePath the path to the trusted CA certificate file (PEM format)
     * @return a {@link TlsPolicy} with hostname verification enabled and insecure connections disabled
     */
    public static TlsPolicy of(String trustCertsFilePath) {
        return new TlsPolicy(trustCertsFilePath, null, null, false, true);
    }

    /**
     * Create a TLS policy with mutual TLS (mTLS) authentication.
     *
     * @param trustCertsFilePath the path to the trusted CA certificate file (PEM format)
     * @param keyFilePath        the path to the client private key file (PEM format)
     * @param certificateFilePath the path to the client certificate file (PEM format)
     * @return a {@link TlsPolicy} configured for mutual TLS with hostname verification enabled
     */
    public static TlsPolicy ofMutualTls(String trustCertsFilePath, String keyFilePath, String certificateFilePath) {
        return new TlsPolicy(trustCertsFilePath, keyFilePath, certificateFilePath, false, true);
    }

    /**
     * Create an insecure TLS policy that accepts any certificate (for development only).
     *
     * @return a {@link TlsPolicy} with insecure connections allowed and hostname verification disabled
     */
    public static TlsPolicy ofInsecure() {
        return new TlsPolicy(null, null, null, true, false);
    }
}
