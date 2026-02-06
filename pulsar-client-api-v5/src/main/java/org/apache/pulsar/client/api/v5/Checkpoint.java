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

import java.io.IOException;
import java.time.Instant;
import org.apache.pulsar.client.api.v5.internal.PulsarClientProvider;

/**
 * An opaque, serializable position vector representing a consistent point across all
 * internal hash-range segments of a topic.
 *
 * <p>Checkpoints are created via {@link CheckpointConsumer#checkpoint()} and can be
 * serialized for external storage (e.g. Flink state, S3) using {@link #toByteArray()}.
 *
 * <p>This is the sole position type used with {@link CheckpointConsumer} — for initial
 * positioning use the static factories {@link #earliest()}, {@link #latest()},
 * {@link #atTimestamp(Instant)}, or {@link #fromByteArray(byte[])} to restore from
 * a previously saved checkpoint.
 */
public interface Checkpoint {

    /**
     * Serialize this checkpoint for external storage.
     */
    byte[] toByteArray();

    /**
     * The time at which this checkpoint was created.
     */
    Instant creationTime();

    // --- Static factories ---

    /**
     * A sentinel checkpoint representing the beginning of the topic (oldest available data).
     */
    static Checkpoint earliest() {
        return PulsarClientProvider.get().earliestCheckpoint();
    }

    /**
     * A sentinel checkpoint representing the end of the topic (next message to be published).
     */
    static Checkpoint latest() {
        return PulsarClientProvider.get().latestCheckpoint();
    }

    /**
     * A checkpoint that positions at the first message published at or after the given timestamp.
     */
    static Checkpoint atTimestamp(Instant timestamp) {
        return PulsarClientProvider.get().checkpointAtTimestamp(timestamp);
    }

    /**
     * Deserialize a checkpoint from a byte array previously obtained via {@link #toByteArray()}.
     */
    static Checkpoint fromByteArray(byte[] data) throws IOException {
        return PulsarClientProvider.get().checkpointFromBytes(data);
    }
}
