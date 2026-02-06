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

import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Map;
import java.util.Set;

/**
 * Provides authentication credentials for different transport mechanisms.
 */
public interface AuthenticationData {

    // --- HTTP authentication ---

    default boolean hasDataForHttp() {
        return false;
    }

    default Set<Map.Entry<String, String>> getHttpHeaders() {
        return Set.of();
    }

    // --- TLS mutual authentication ---

    default boolean hasDataForTls() {
        return false;
    }

    default Certificate[] getTlsCertificates() {
        return null;
    }

    default PrivateKey getTlsPrivateKey() {
        return null;
    }

    // --- Binary protocol authentication ---

    default boolean hasDataFromCommand() {
        return false;
    }

    default String getCommandData() {
        return null;
    }
}
