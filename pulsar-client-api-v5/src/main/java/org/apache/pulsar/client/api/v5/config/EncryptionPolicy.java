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

import java.util.List;
import java.util.Objects;
import org.apache.pulsar.client.api.v5.auth.CryptoFailureAction;
import org.apache.pulsar.client.api.v5.auth.CryptoKeyReader;

/**
 * End-to-end encryption configuration for producers and consumers.
 *
 * <p>For producers, supply a {@link CryptoKeyReader} and one or more encryption key names.
 * For consumers/readers, supply a {@link CryptoKeyReader} and a {@link CryptoFailureAction}.
 *
 * @param keyReader     the crypto key reader for loading encryption/decryption keys
 * @param keyNames      encryption key names (producer-side; empty list for consumer/reader)
 * @param failureAction action to take when encryption or decryption fails
 */
public record EncryptionPolicy(
        CryptoKeyReader keyReader,
        List<String> keyNames,
        CryptoFailureAction failureAction
) {
    public EncryptionPolicy {
        Objects.requireNonNull(keyReader, "keyReader must not be null");
        if (keyNames == null) {
            keyNames = List.of();
        }
        if (failureAction == null) {
            failureAction = CryptoFailureAction.FAIL;
        }
    }

    /**
     * Create an encryption policy for producers.
     *
     * @param keyReader the crypto key reader for loading encryption keys
     * @param keyNames  one or more encryption key names to use
     * @return an {@link EncryptionPolicy} configured for producer-side encryption
     */
    public static EncryptionPolicy forProducer(CryptoKeyReader keyReader, String... keyNames) {
        return new EncryptionPolicy(keyReader, List.of(keyNames), CryptoFailureAction.FAIL);
    }

    /**
     * Create an encryption policy for consumers/readers.
     *
     * @param keyReader     the crypto key reader for loading decryption keys
     * @param failureAction the action to take when decryption fails
     * @return an {@link EncryptionPolicy} configured for consumer-side decryption
     */
    public static EncryptionPolicy forConsumer(CryptoKeyReader keyReader, CryptoFailureAction failureAction) {
        return new EncryptionPolicy(keyReader, List.of(), failureAction);
    }
}
