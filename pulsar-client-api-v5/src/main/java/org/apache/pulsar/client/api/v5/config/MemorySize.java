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
 * A type-safe representation of a memory size in bytes.
 *
 * <p>Use the static factory methods to create instances from common units:
 * <pre>{@code
 * MemorySize.ofMegabytes(64)   // 64 MB
 * MemorySize.ofGigabytes(1)    // 1 GB
 * MemorySize.ofKilobytes(512)  // 512 KB
 * }</pre>
 *
 * @param bytes the size in bytes
 */
public record MemorySize(long bytes) {

    public MemorySize {
        if (bytes < 0) {
            throw new IllegalArgumentException("bytes must be >= 0");
        }
    }

    private static final long KB = 1024;
    private static final long MB = 1024 * KB;
    private static final long GB = 1024 * MB;

    /**
     * Create a memory size from a number of bytes.
     */
    public static MemorySize ofBytes(long bytes) {
        return new MemorySize(bytes);
    }

    /**
     * Create a memory size from a number of kilobytes.
     */
    public static MemorySize ofKilobytes(long kb) {
        return new MemorySize(Math.multiplyExact(kb, KB));
    }

    /**
     * Create a memory size from a number of megabytes.
     */
    public static MemorySize ofMegabytes(long mb) {
        return new MemorySize(Math.multiplyExact(mb, MB));
    }

    /**
     * Create a memory size from a number of gigabytes.
     */
    public static MemorySize ofGigabytes(long gb) {
        return new MemorySize(Math.multiplyExact(gb, GB));
    }

    @Override
    public String toString() {
        if (bytes >= GB && bytes % GB == 0) {
            return bytes / GB + " GB";
        } else if (bytes >= MB && bytes % MB == 0) {
            return bytes / MB + " MB";
        } else if (bytes >= KB && bytes % KB == 0) {
            return bytes / KB + " KB";
        }
        return bytes + " bytes";
    }
}
