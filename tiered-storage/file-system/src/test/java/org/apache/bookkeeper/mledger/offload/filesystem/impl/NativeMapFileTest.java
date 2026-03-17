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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class NativeMapFileTest {

    private Path tempDir;

    @BeforeMethod
    public void setup() throws IOException {
        tempDir = Files.createTempDirectory("mapfile-test");
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

    @Test
    public void testDirectoryStructure() throws IOException {
        Path mapDir = tempDir.resolve("test-mapfile");
        try (NativeMapFile.Writer writer = new NativeMapFile.Writer(mapDir)) {
            writer.append(0, "value".getBytes(StandardCharsets.UTF_8), 0, 5);
        }

        assertTrue(Files.isDirectory(mapDir));
        assertTrue(Files.exists(mapDir.resolve("data")));
        assertTrue(Files.exists(mapDir.resolve("index")));
    }

    @Test
    public void testDataFileIsValidSequenceFile() throws IOException {
        Path mapDir = tempDir.resolve("seqfile-check");
        try (NativeMapFile.Writer writer = new NativeMapFile.Writer(mapDir)) {
            writer.append(0, "value".getBytes(StandardCharsets.UTF_8), 0, 5);
        }

        // Both data and index should be valid SequenceFiles
        try (SequenceFile.Reader dataReader = new SequenceFile.Reader(mapDir.resolve("data"))) {
            assertEquals(dataReader.getHeader().keyClassName, NativeMapFile.KEY_CLASS);
            assertEquals(dataReader.getHeader().valueClassName, NativeMapFile.VALUE_CLASS);
            assertEquals(dataReader.getHeader().compressionType, SequenceFile.CompressionType.NONE);
        }

        try (SequenceFile.Reader indexReader = new SequenceFile.Reader(mapDir.resolve("index"))) {
            assertEquals(indexReader.getHeader().keyClassName, NativeMapFile.KEY_CLASS);
            assertEquals(indexReader.getHeader().valueClassName, NativeMapFile.KEY_CLASS);
        }
    }

    @Test
    public void testWriteReadSingleEntry() throws IOException {
        Path mapDir = tempDir.resolve("single");
        byte[] data = "hello".getBytes(StandardCharsets.UTF_8);

        try (NativeMapFile.Writer writer = new NativeMapFile.Writer(mapDir)) {
            writer.append(42, data, 0, data.length);
        }

        try (NativeMapFile.Reader reader = new NativeMapFile.Reader(mapDir)) {
            SequenceFile.KeyValue kv = new SequenceFile.KeyValue();
            reader.get(42, kv);
            assertEquals(SequenceFile.deserializeLong(kv.key), 42L);
            assertEquals(SequenceFile.deserializeBytes(kv.value), data);
        }
    }

    @Test
    public void testWriteReadSequentialEntries() throws IOException {
        Path mapDir = tempDir.resolve("sequential");
        int numEntries = 1000;

        try (NativeMapFile.Writer writer = new NativeMapFile.Writer(mapDir)) {
            for (int i = 0; i < numEntries; i++) {
                byte[] data = ("entry-" + i).getBytes(StandardCharsets.UTF_8);
                writer.append(i, data, 0, data.length);
            }
        }

        try (NativeMapFile.Reader reader = new NativeMapFile.Reader(mapDir)) {
            SequenceFile.KeyValue kv = new SequenceFile.KeyValue();
            // Read all sequentially
            for (int i = 0; i < numEntries; i++) {
                assertTrue(reader.next(kv));
                assertEquals(SequenceFile.deserializeLong(kv.key), (long) i);
                assertEquals(SequenceFile.deserializeBytes(kv.value),
                        ("entry-" + i).getBytes(StandardCharsets.UTF_8));
            }
            assertFalse(reader.next(kv));
        }
    }

    @Test
    public void testGetByKey() throws IOException {
        Path mapDir = tempDir.resolve("get-by-key");
        int numEntries = 500;

        try (NativeMapFile.Writer writer = new NativeMapFile.Writer(mapDir)) {
            for (int i = 0; i < numEntries; i++) {
                byte[] data = ("data-" + i).getBytes(StandardCharsets.UTF_8);
                writer.append(i, data, 0, data.length);
            }
        }

        try (NativeMapFile.Reader reader = new NativeMapFile.Reader(mapDir)) {
            SequenceFile.KeyValue kv = new SequenceFile.KeyValue();

            // Get first entry
            reader.get(0, kv);
            assertEquals(SequenceFile.deserializeBytes(kv.value),
                    "data-0".getBytes(StandardCharsets.UTF_8));

            // Get last entry
            reader.get(numEntries - 1, kv);
            assertEquals(SequenceFile.deserializeBytes(kv.value),
                    ("data-" + (numEntries - 1)).getBytes(StandardCharsets.UTF_8));

            // Get entry in the middle
            reader.get(250, kv);
            assertEquals(SequenceFile.deserializeBytes(kv.value),
                    "data-250".getBytes(StandardCharsets.UTF_8));
        }
    }

    @Test
    public void testSeekAndRead() throws IOException {
        Path mapDir = tempDir.resolve("seek");
        int numEntries = 300;

        try (NativeMapFile.Writer writer = new NativeMapFile.Writer(mapDir)) {
            for (int i = 0; i < numEntries; i++) {
                byte[] data = ("val-" + i).getBytes(StandardCharsets.UTF_8);
                writer.append(i, data, 0, data.length);
            }
        }

        try (NativeMapFile.Reader reader = new NativeMapFile.Reader(mapDir)) {
            SequenceFile.KeyValue kv = new SequenceFile.KeyValue();

            // Seek to entry 100, then read sequentially
            reader.seek(100);
            for (int i = 100; i < numEntries; i++) {
                assertTrue(reader.next(kv), "Should have entry " + i);
                assertEquals(SequenceFile.deserializeLong(kv.key), (long) i);
                assertEquals(SequenceFile.deserializeBytes(kv.value),
                        ("val-" + i).getBytes(StandardCharsets.UTF_8));
            }
            assertFalse(reader.next(kv));
        }
    }

    @Test
    public void testSeekToFirstEntry() throws IOException {
        Path mapDir = tempDir.resolve("seek-first");

        try (NativeMapFile.Writer writer = new NativeMapFile.Writer(mapDir)) {
            for (int i = 0; i < 10; i++) {
                byte[] data = ("v" + i).getBytes(StandardCharsets.UTF_8);
                writer.append(i, data, 0, data.length);
            }
        }

        try (NativeMapFile.Reader reader = new NativeMapFile.Reader(mapDir)) {
            SequenceFile.KeyValue kv = new SequenceFile.KeyValue();
            reader.seek(0);
            assertTrue(reader.next(kv));
            assertEquals(SequenceFile.deserializeLong(kv.key), 0L);
        }
    }

    @Test
    public void testMetadataKeyPattern() throws IOException {
        // Simulate the offloader pattern: metadata at key -1, then entries 0..N
        Path mapDir = tempDir.resolve("metadata-pattern");
        byte[] metadata = "ledger-metadata-bytes".getBytes(StandardCharsets.UTF_8);

        try (NativeMapFile.Writer writer = new NativeMapFile.Writer(mapDir)) {
            writer.append(-1, metadata, 0, metadata.length);
            for (int i = 0; i < 100; i++) {
                byte[] data = ("entry-" + i).getBytes(StandardCharsets.UTF_8);
                writer.append(i, data, 0, data.length);
            }
        }

        try (NativeMapFile.Reader reader = new NativeMapFile.Reader(mapDir)) {
            SequenceFile.KeyValue kv = new SequenceFile.KeyValue();

            // Get metadata by key
            reader.get(-1, kv);
            assertEquals(SequenceFile.deserializeBytes(kv.value), metadata);

            // Seek to entry 0 and read sequentially
            reader.seek(0);
            for (int i = 0; i < 100; i++) {
                assertTrue(reader.next(kv));
                assertEquals(SequenceFile.deserializeLong(kv.key), (long) i);
                assertEquals(SequenceFile.deserializeBytes(kv.value),
                        ("entry-" + i).getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    @Test
    public void testIndexEntries() throws IOException {
        // With INDEX_INTERVAL=128, writing 300 entries should produce index entries
        // at positions 0, 128, 256
        Path mapDir = tempDir.resolve("index-entries");
        int numEntries = 300;

        try (NativeMapFile.Writer writer = new NativeMapFile.Writer(mapDir)) {
            for (int i = 0; i < numEntries; i++) {
                byte[] data = ("d" + i).getBytes(StandardCharsets.UTF_8);
                writer.append(i, data, 0, data.length);
            }
        }

        // Read the index file directly and verify entries
        try (SequenceFile.Reader indexReader = new SequenceFile.Reader(mapDir.resolve("index"))) {
            SequenceFile.KeyValue kv = new SequenceFile.KeyValue();
            int indexCount = 0;
            while (indexReader.next(kv)) {
                long key = SequenceFile.deserializeLong(kv.key);
                assertEquals(key, (long) indexCount * NativeMapFile.INDEX_INTERVAL);
                indexCount++;
            }
            // 300 entries / 128 interval = entries at 0, 128, 256 = 3 index entries
            assertEquals(indexCount, 3);
        }
    }

    @Test
    public void testIndexWithMetadataKey() throws IOException {
        // When key -1 is at position 0, it should be in the index
        Path mapDir = tempDir.resolve("index-metadata");

        try (NativeMapFile.Writer writer = new NativeMapFile.Writer(mapDir)) {
            writer.append(-1, "meta".getBytes(StandardCharsets.UTF_8), 0, 4);
            for (int i = 0; i < 200; i++) {
                byte[] data = ("e" + i).getBytes(StandardCharsets.UTF_8);
                writer.append(i, data, 0, data.length);
            }
        }

        // Verify first index entry has key -1
        try (SequenceFile.Reader indexReader = new SequenceFile.Reader(mapDir.resolve("index"))) {
            SequenceFile.KeyValue kv = new SequenceFile.KeyValue();
            assertTrue(indexReader.next(kv));
            assertEquals(SequenceFile.deserializeLong(kv.key), -1L);

            // Second index entry should be at position 128 (key=127 since -1 is entry 0)
            assertTrue(indexReader.next(kv));
            assertEquals(SequenceFile.deserializeLong(kv.key), 127L);
        }
    }

    @Test
    public void testReadWithoutIndex() throws IOException {
        // Write a MapFile, delete the index, and verify it still works
        Path mapDir = tempDir.resolve("no-index");

        try (NativeMapFile.Writer writer = new NativeMapFile.Writer(mapDir)) {
            for (int i = 0; i < 50; i++) {
                byte[] data = ("val-" + i).getBytes(StandardCharsets.UTF_8);
                writer.append(i, data, 0, data.length);
            }
        }

        // Delete index file
        Files.delete(mapDir.resolve("index"));
        assertFalse(Files.exists(mapDir.resolve("index")));

        // Should still be able to read
        try (NativeMapFile.Reader reader = new NativeMapFile.Reader(mapDir)) {
            SequenceFile.KeyValue kv = new SequenceFile.KeyValue();
            reader.get(25, kv);
            assertEquals(SequenceFile.deserializeBytes(kv.value),
                    "val-25".getBytes(StandardCharsets.UTF_8));
        }
    }

    @Test(expectedExceptions = IOException.class, expectedExceptionsMessageRegExp = "Key not found: 999")
    public void testGetNonExistentKey() throws IOException {
        Path mapDir = tempDir.resolve("missing-key");

        try (NativeMapFile.Writer writer = new NativeMapFile.Writer(mapDir)) {
            writer.append(0, "v".getBytes(StandardCharsets.UTF_8), 0, 1);
        }

        try (NativeMapFile.Reader reader = new NativeMapFile.Reader(mapDir)) {
            SequenceFile.KeyValue kv = new SequenceFile.KeyValue();
            reader.get(999, kv);
        }
    }

    @Test
    public void testLargeEntries() throws IOException {
        Path mapDir = tempDir.resolve("large-entries");
        byte[] largeData = new byte[100_000];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 251); // prime modulus for varied bytes
        }

        try (NativeMapFile.Writer writer = new NativeMapFile.Writer(mapDir)) {
            writer.append(0, largeData, 0, largeData.length);
            writer.append(1, largeData, 0, largeData.length);
        }

        try (NativeMapFile.Reader reader = new NativeMapFile.Reader(mapDir)) {
            SequenceFile.KeyValue kv = new SequenceFile.KeyValue();
            reader.get(0, kv);
            assertEquals(SequenceFile.deserializeBytes(kv.value), largeData);

            reader.get(1, kv);
            assertEquals(SequenceFile.deserializeBytes(kv.value), largeData);
        }
    }

    @Test
    public void testMultipleSeeksOnSameReader() throws IOException {
        Path mapDir = tempDir.resolve("multi-seek");
        int numEntries = 200;

        try (NativeMapFile.Writer writer = new NativeMapFile.Writer(mapDir)) {
            for (int i = 0; i < numEntries; i++) {
                byte[] data = ("v" + i).getBytes(StandardCharsets.UTF_8);
                writer.append(i, data, 0, data.length);
            }
        }

        try (NativeMapFile.Reader reader = new NativeMapFile.Reader(mapDir)) {
            SequenceFile.KeyValue kv = new SequenceFile.KeyValue();

            // Seek forward
            reader.seek(50);
            assertTrue(reader.next(kv));
            assertEquals(SequenceFile.deserializeLong(kv.key), 50L);

            // Seek backward
            reader.seek(10);
            assertTrue(reader.next(kv));
            assertEquals(SequenceFile.deserializeLong(kv.key), 10L);

            // Seek forward again
            reader.seek(150);
            assertTrue(reader.next(kv));
            assertEquals(SequenceFile.deserializeLong(kv.key), 150L);

            // Read a few more sequentially
            assertTrue(reader.next(kv));
            assertEquals(SequenceFile.deserializeLong(kv.key), 151L);
            assertTrue(reader.next(kv));
            assertEquals(SequenceFile.deserializeLong(kv.key), 152L);
        }
    }
}
