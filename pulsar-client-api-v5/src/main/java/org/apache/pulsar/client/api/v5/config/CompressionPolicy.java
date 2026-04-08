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
 * Compression configuration for producer message payloads.
 *
 * @param type the compression codec to use
 */
public record CompressionPolicy(CompressionType type) {

    /**
     * No compression.
     *
     * @return a {@link CompressionPolicy} with compression disabled
     */
    public static CompressionPolicy disabled() {
        return new CompressionPolicy(CompressionType.NONE);
    }

    /**
     * Create a compression policy with the given codec.
     *
     * @param type the compression codec to use for message payloads
     * @return a {@link CompressionPolicy} configured with the specified codec
     */
    public static CompressionPolicy of(CompressionType type) {
        return new CompressionPolicy(type);
    }
}
