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
package org.apache.bookkeeper.mledger.offload.filesystem.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SequenceFileTest {

    private Path tempDir;

    @BeforeMethod
    public void setup() throws IOException {
        tempDir = Files.createTempDirectory("seqfile-test");
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() throws IOException {
        if (tempDir != null) {
            Files.walk(tempDir)
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            // ignore
                        }
                    });
        }
    }

    // -- VInt encoding tests --

    @DataProvider(name = "vintValues")
    public Object[][] vintValues() {
        return new Object[][] {
                {0}, {1}, {-1}, {127}, {-112},
                {128}, {255}, {256}, {-113}, {-128},
                {1000}, {-1000},
                {Integer.MAX_VALUE}, {Integer.MIN_VALUE},
                {100000}, {-100000},
        };
    }

    @Test(dataProvider = "vintValues")
    public void testVIntRoundTrip(int value) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        SequenceFile.writeVInt(out, value);
        out.flush();

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
        int result = SequenceFile.readVInt(in);
        assertEquals(result, value);
    }

    @DataProvider(name = "vlongValues")
    public Object[][] vlongValues() {
        return new Object[][] {
                {0L}, {1L}, {-1L}, {127L}, {-112L},
                {128L}, {255L}, {256L},
                {Long.MAX_VALUE}, {Long.MIN_VALUE},
                {(long) Integer.MAX_VALUE + 1}, {(long) Integer.MIN_VALUE - 1},
        };
    }

    @Test(dataProvider = "vlongValues")
    public void testVLongRoundTrip(long value) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        SequenceFile.writeVLong(out, value);
        out.flush();

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
        long result = SequenceFile.readVLong(in);
        assertEquals(result, value);
    }

    @Test
    public void testVIntSingleByteRange() throws IOException {
        // Values -112 to 127 should encode to a single byte
        for (int v = -112; v <= 127; v++) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);
            SequenceFile.writeVInt(out, v);
            out.flush();
            assertEquals(baos.size(), 1, "Value " + v + " should be single byte");
        }
    }

    @Test
    public void testVIntMultiByteValues() throws IOException {
        // Values outside -112..127 need more than 1 byte
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        SequenceFile.writeVInt(out, 128);
        out.flush();
        assertTrue(baos.size() > 1);
    }

    // -- Text string encoding tests --

    @Test
    public void testStringRoundTrip() throws IOException {
        String[] strings = {"", "hello", "org.apache.hadoop.io.LongWritable", "\u00e9\u00e8\u00ea"};
        for (String s : strings) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);
            SequenceFile.writeString(out, s);
            out.flush();

            DataInputStream in = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
            assertEquals(SequenceFile.readString(in), s);
        }
    }

    // -- LongWritable serialization tests --

    @DataProvider(name = "longValues")
    public Object[][] longValues() {
        return new Object[][] {
                {0L}, {1L}, {-1L}, {Long.MAX_VALUE}, {Long.MIN_VALUE},
                {1000000000L}, {-9999999999L},
        };
    }

    @Test(dataProvider = "longValues")
    public void testLongSerializationRoundTrip(long value) {
        byte[] serialized = SequenceFile.serializeLong(value);
        assertEquals(serialized.length, 8);
        assertEquals(SequenceFile.deserializeLong(serialized), value);
    }

    @Test
    public void testLongSerializationBigEndian() {
        // 1,000,000,000 = 0x3B9ACA00
        byte[] serialized = SequenceFile.serializeLong(1000000000L);
        assertEquals(serialized[0], 0x00);
        assertEquals(serialized[1], 0x00);
        assertEquals(serialized[2], 0x00);
        assertEquals(serialized[3], 0x00);
        assertEquals(serialized[4], 0x3B);
        assertEquals(serialized[5], (byte) 0x9A);
        assertEquals(serialized[6], (byte) 0xCA);
        assertEquals(serialized[7], 0x00);
    }

    // -- BytesWritable serialization tests --

    @Test
    public void testBytesSerializationRoundTrip() {
        byte[] data = "hello world".getBytes(StandardCharsets.UTF_8);
        byte[] serialized = SequenceFile.serializeBytes(data, 0, data.length);
        // 4 bytes length prefix + data
        assertEquals(serialized.length, 4 + data.length);
        byte[] deserialized = SequenceFile.deserializeBytes(serialized);
        assertEquals(deserialized, data);
    }

    @Test
    public void testBytesSerializationWithOffset() {
        byte[] data = "hello world".getBytes(StandardCharsets.UTF_8);
        byte[] serialized = SequenceFile.serializeBytes(data, 6, 5); // "world"
        byte[] deserialized = SequenceFile.deserializeBytes(serialized);
        assertEquals(deserialized, "world".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void testBytesSerializationEmpty() {
        byte[] serialized = SequenceFile.serializeBytes(new byte[0], 0, 0);
        assertEquals(serialized.length, 4); // just the length prefix
        byte[] deserialized = SequenceFile.deserializeBytes(serialized);
        assertEquals(deserialized.length, 0);
    }

    @Test
    public void testBytesSerializationLengthPrefix() {
        // Verify 4-byte big-endian length prefix
        byte[] data = new byte[300];
        byte[] serialized = SequenceFile.serializeBytes(data, 0, data.length);
        // 300 = 0x0000012C
        assertEquals(serialized[0], 0x00);
        assertEquals(serialized[1], 0x00);
        assertEquals(serialized[2], 0x01);
        assertEquals(serialized[3], 0x2C);
    }

    // -- SequenceFile header format tests --

    @Test
    public void testHeaderFormat() throws IOException {
        Path file = tempDir.resolve("header-test");
        String keyClass = "org.apache.hadoop.io.LongWritable";
        String valueClass = "org.apache.hadoop.io.BytesWritable";

        try (SequenceFile.Writer writer = new SequenceFile.Writer(file, keyClass, valueClass)) {
            // Just create the file with header
        }

        // Verify header bytes directly
        byte[] fileBytes = Files.readAllBytes(file);
        // Magic: SEQ
        assertEquals(fileBytes[0], 'S');
        assertEquals(fileBytes[1], 'E');
        assertEquals(fileBytes[2], 'Q');
        // Version: 6
        assertEquals(fileBytes[3], 6);

        // Read back via reader and verify header
        try (SequenceFile.Reader reader = new SequenceFile.Reader(file)) {
            SequenceFile.Header header = reader.getHeader();
            assertEquals(header.keyClassName, keyClass);
            assertEquals(header.valueClassName, valueClass);
            assertEquals(header.compressionType, SequenceFile.CompressionType.NONE);
            assertEquals(header.sync.length, SequenceFile.SYNC_SIZE);
        }
    }

    @Test
    public void testHeaderNoCompression() throws IOException {
        Path file = tempDir.resolve("no-compress-test");
        try (SequenceFile.Writer writer = new SequenceFile.Writer(file, "KeyClass", "ValueClass")) {
            // empty
        }

        try (SequenceFile.Reader reader = new SequenceFile.Reader(file)) {
            assertEquals(reader.getHeader().compressionType, SequenceFile.CompressionType.NONE);
            assertEquals(reader.getHeader().compressionCodecClassName, null);
        }
    }

    // -- Write and read round-trip tests --

    @Test
    public void testWriteReadSingleRecord() throws IOException {
        Path file = tempDir.resolve("single-record");
        byte[] key = SequenceFile.serializeLong(42);
        byte[] value = SequenceFile.serializeBytes("test-data".getBytes(StandardCharsets.UTF_8), 0, 9);

        try (SequenceFile.Writer writer = new SequenceFile.Writer(file,
                NativeMapFile.KEY_CLASS, NativeMapFile.VALUE_CLASS)) {
            writer.append(key, value);
        }

        try (SequenceFile.Reader reader = new SequenceFile.Reader(file)) {
            SequenceFile.KeyValue kv = new SequenceFile.KeyValue();
            assertTrue(reader.next(kv));
            assertEquals(SequenceFile.deserializeLong(kv.key), 42L);
            assertEquals(SequenceFile.deserializeBytes(kv.value),
                    "test-data".getBytes(StandardCharsets.UTF_8));
            assertFalse(reader.next(kv));
        }
    }

    @Test
    public void testWriteReadMultipleRecords() throws IOException {
        Path file = tempDir.resolve("multi-record");
        int numRecords = 500;

        try (SequenceFile.Writer writer = new SequenceFile.Writer(file,
                NativeMapFile.KEY_CLASS, NativeMapFile.VALUE_CLASS)) {
            for (int i = 0; i < numRecords; i++) {
                byte[] key = SequenceFile.serializeLong(i);
                byte[] data = ("entry-" + i).getBytes(StandardCharsets.UTF_8);
                byte[] value = SequenceFile.serializeBytes(data, 0, data.length);
                writer.append(key, value);
            }
        }

        try (SequenceFile.Reader reader = new SequenceFile.Reader(file)) {
            SequenceFile.KeyValue kv = new SequenceFile.KeyValue();
            for (int i = 0; i < numRecords; i++) {
                assertTrue(reader.next(kv), "Should have record " + i);
                assertEquals(SequenceFile.deserializeLong(kv.key), (long) i);
                assertEquals(SequenceFile.deserializeBytes(kv.value),
                        ("entry-" + i).getBytes(StandardCharsets.UTF_8));
            }
            assertFalse(reader.next(kv));
        }
    }

    @Test
    public void testWriteReadEmptyFile() throws IOException {
        Path file = tempDir.resolve("empty");
        try (SequenceFile.Writer writer = new SequenceFile.Writer(file,
                NativeMapFile.KEY_CLASS, NativeMapFile.VALUE_CLASS)) {
            // no records
        }

        try (SequenceFile.Reader reader = new SequenceFile.Reader(file)) {
            SequenceFile.KeyValue kv = new SequenceFile.KeyValue();
            assertFalse(reader.next(kv));
        }
    }

    @Test
    public void testSyncMarkersWritten() throws IOException {
        // Write enough data to trigger sync markers (>100KB)
        Path file = tempDir.resolve("sync-test");
        byte[] largeData = new byte[1024];
        java.util.Arrays.fill(largeData, (byte) 'x');

        int numRecords = 200; // 200 * ~1KB = ~200KB, should trigger at least one sync

        try (SequenceFile.Writer writer = new SequenceFile.Writer(file,
                NativeMapFile.KEY_CLASS, NativeMapFile.VALUE_CLASS)) {
            for (int i = 0; i < numRecords; i++) {
                byte[] key = SequenceFile.serializeLong(i);
                byte[] value = SequenceFile.serializeBytes(largeData, 0, largeData.length);
                writer.append(key, value);
            }
        }

        // Verify we can still read all records (sync markers are transparent)
        try (SequenceFile.Reader reader = new SequenceFile.Reader(file)) {
            SequenceFile.KeyValue kv = new SequenceFile.KeyValue();
            int count = 0;
            while (reader.next(kv)) {
                assertEquals(SequenceFile.deserializeLong(kv.key), (long) count);
                count++;
            }
            assertEquals(count, numRecords);
        }

        // Verify file contains sync markers by checking for -1 (0xFFFFFFFF) markers
        byte[] fileBytes = Files.readAllBytes(file);
        boolean foundSync = false;
        for (int i = 0; i < fileBytes.length - 3; i++) {
            if (fileBytes[i] == (byte) 0xFF && fileBytes[i + 1] == (byte) 0xFF
                    && fileBytes[i + 2] == (byte) 0xFF && fileBytes[i + 3] == (byte) 0xFF) {
                foundSync = true;
                break;
            }
        }
        assertTrue(foundSync, "Should have at least one sync marker in the file");
    }

    @Test
    public void testRecordFormat() throws IOException {
        // Verify the exact binary layout of a record:
        // [recordLength:4bytes] [keyLength:4bytes] [key:8bytes] [value:4+Nbytes]
        Path file = tempDir.resolve("format-test");
        byte[] key = SequenceFile.serializeLong(7);
        byte[] data = "AB".getBytes(StandardCharsets.UTF_8);
        byte[] value = SequenceFile.serializeBytes(data, 0, data.length);

        try (SequenceFile.Writer writer = new SequenceFile.Writer(file,
                NativeMapFile.KEY_CLASS, NativeMapFile.VALUE_CLASS)) {
            writer.append(key, value);
        }

        // Read file and find the record after the header
        byte[] fileBytes = Files.readAllBytes(file);

        // Find the record by reading past the header using the reader to determine header size
        long headerSize;
        try (SequenceFile.Reader reader = new SequenceFile.Reader(file)) {
            headerSize = reader.getPosition();
        }

        int offset = (int) headerSize;
        // recordLength = key(8) + value(4+2) = 14
        int recordLength = readBigEndianInt(fileBytes, offset);
        assertEquals(recordLength, 14);

        // keyLength = 8 (LongWritable is always 8 bytes)
        int keyLength = readBigEndianInt(fileBytes, offset + 4);
        assertEquals(keyLength, 8);

        // key = 7 in big-endian
        long keyVal = readBigEndianLong(fileBytes, offset + 8);
        assertEquals(keyVal, 7L);

        // value: first 4 bytes = length(2), then 2 bytes = "AB"
        int valueLength = readBigEndianInt(fileBytes, offset + 16);
        assertEquals(valueLength, 2);
        assertEquals(fileBytes[offset + 20], 'A');
        assertEquals(fileBytes[offset + 21], 'B');
    }

    @Test
    public void testLargeValues() throws IOException {
        Path file = tempDir.resolve("large-values");
        byte[] largeData = new byte[64 * 1024]; // 64KB
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i & 0xFF);
        }

        try (SequenceFile.Writer writer = new SequenceFile.Writer(file,
                NativeMapFile.KEY_CLASS, NativeMapFile.VALUE_CLASS)) {
            byte[] key = SequenceFile.serializeLong(0);
            byte[] value = SequenceFile.serializeBytes(largeData, 0, largeData.length);
            writer.append(key, value);
        }

        try (SequenceFile.Reader reader = new SequenceFile.Reader(file)) {
            SequenceFile.KeyValue kv = new SequenceFile.KeyValue();
            assertTrue(reader.next(kv));
            byte[] readBack = SequenceFile.deserializeBytes(kv.value);
            assertEquals(readBack, largeData);
        }
    }

    @Test
    public void testNegativeKeys() throws IOException {
        // MapFile uses key -1 for metadata
        Path file = tempDir.resolve("negative-keys");
        try (SequenceFile.Writer writer = new SequenceFile.Writer(file,
                NativeMapFile.KEY_CLASS, NativeMapFile.VALUE_CLASS)) {
            byte[] key = SequenceFile.serializeLong(-1);
            byte[] data = "metadata".getBytes(StandardCharsets.UTF_8);
            byte[] value = SequenceFile.serializeBytes(data, 0, data.length);
            writer.append(key, value);

            key = SequenceFile.serializeLong(0);
            data = "entry0".getBytes(StandardCharsets.UTF_8);
            value = SequenceFile.serializeBytes(data, 0, data.length);
            writer.append(key, value);
        }

        try (SequenceFile.Reader reader = new SequenceFile.Reader(file)) {
            SequenceFile.KeyValue kv = new SequenceFile.KeyValue();

            assertTrue(reader.next(kv));
            assertEquals(SequenceFile.deserializeLong(kv.key), -1L);
            assertEquals(SequenceFile.deserializeBytes(kv.value),
                    "metadata".getBytes(StandardCharsets.UTF_8));

            assertTrue(reader.next(kv));
            assertEquals(SequenceFile.deserializeLong(kv.key), 0L);
            assertEquals(SequenceFile.deserializeBytes(kv.value),
                    "entry0".getBytes(StandardCharsets.UTF_8));
        }
    }

    private static int readBigEndianInt(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 24)
                | ((data[offset + 1] & 0xFF) << 16)
                | ((data[offset + 2] & 0xFF) << 8)
                | (data[offset + 3] & 0xFF);
    }

    private static long readBigEndianLong(byte[] data, int offset) {
        return ((long) (data[offset] & 0xFF) << 56)
                | ((long) (data[offset + 1] & 0xFF) << 48)
                | ((long) (data[offset + 2] & 0xFF) << 40)
                | ((long) (data[offset + 3] & 0xFF) << 32)
                | ((long) (data[offset + 4] & 0xFF) << 24)
                | ((long) (data[offset + 5] & 0xFF) << 16)
                | ((long) (data[offset + 6] & 0xFF) << 8)
                | ((long) (data[offset + 7] & 0xFF));
    }
}
