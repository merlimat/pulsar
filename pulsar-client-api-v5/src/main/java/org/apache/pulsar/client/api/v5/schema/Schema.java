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
package org.apache.pulsar.client.api.v5.schema;

import org.apache.pulsar.client.api.v5.internal.PulsarClientProvider;
import java.nio.ByteBuffer;

/**
 * Defines how message values are serialized to bytes and deserialized from bytes.
 *
 * @param <T> the type of the message value
 */
public interface Schema<T> {

    /**
     * Encode a value to bytes.
     */
    byte[] encode(T message);

    /**
     * Decode bytes to a value.
     */
    T decode(byte[] bytes);

    /**
     * Decode bytes with a specific schema version. Useful for schema evolution.
     */
    default T decode(byte[] bytes, byte[] schemaVersion) {
        return decode(bytes);
    }

    /**
     * Decode from a ByteBuffer.
     */
    default T decode(ByteBuffer data) {
        if (data == null) {
            return null;
        }
        byte[] bytes = new byte[data.remaining()];
        data.get(bytes);
        return decode(bytes);
    }

    /**
     * The schema descriptor for broker-side negotiation.
     */
    SchemaInfo schemaInfo();

    // --- Built-in schema factories ---

    static Schema<byte[]> BYTES() {
        return PulsarClientProvider.get().bytesSchema();
    }

    static Schema<String> STRING() {
        return PulsarClientProvider.get().stringSchema();
    }

    static Schema<Boolean> BOOL() {
        return PulsarClientProvider.get().booleanSchema();
    }

    static Schema<Byte> INT8() {
        return PulsarClientProvider.get().byteSchema();
    }

    static Schema<Short> INT16() {
        return PulsarClientProvider.get().shortSchema();
    }

    static Schema<Integer> INT32() {
        return PulsarClientProvider.get().intSchema();
    }

    static Schema<Long> INT64() {
        return PulsarClientProvider.get().longSchema();
    }

    static Schema<Float> FLOAT() {
        return PulsarClientProvider.get().floatSchema();
    }

    static Schema<Double> DOUBLE() {
        return PulsarClientProvider.get().doubleSchema();
    }

    static <T> Schema<T> JSON(Class<T> pojo) {
        return PulsarClientProvider.get().jsonSchema(pojo);
    }

    static <T> Schema<T> AVRO(Class<T> pojo) {
        return PulsarClientProvider.get().avroSchema(pojo);
    }

    static <T extends com.google.protobuf.GeneratedMessageV3> Schema<T> PROTOBUF(Class<T> clazz) {
        return PulsarClientProvider.get().protobufSchema(clazz);
    }

    static Schema<byte[]> AUTO_PRODUCE_BYTES() {
        return PulsarClientProvider.get().autoProduceBytesSchema();
    }
}
