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

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Native implementation of Hadoop's MapFile format, backed by {@link SequenceFile}.
 * <p>
 * A MapFile is a directory containing:
 * <ul>
 *   <li>{@code data} - a SequenceFile with sorted LongWritable keys and BytesWritable values</li>
 *   <li>{@code index} - a SequenceFile with sampled key-to-offset pairs for fast lookup</li>
 * </ul>
 * <p>
 * Compatible with data written by Hadoop's {@code org.apache.hadoop.io.MapFile}.
 */
class NativeMapFile {

    static final String DATA_FILE_NAME = "data";
    static final String INDEX_FILE_NAME = "index";
    static final String KEY_CLASS = "org.apache.hadoop.io.LongWritable";
    static final String VALUE_CLASS = "org.apache.hadoop.io.BytesWritable";
    static final int INDEX_INTERVAL = 128;

    // -- Writer --

    static class Writer implements Closeable {
        private final SequenceFile.Writer dataWriter;
        private final SequenceFile.Writer indexWriter;
        private int entriesWritten;

        Writer(Path dir) throws IOException {
            this(dir, null, null);
        }

        Writer(Path dir, byte[] dataSyncMarker, byte[] indexSyncMarker) throws IOException {
            Files.createDirectories(dir);
            this.dataWriter = new SequenceFile.Writer(
                    dir.resolve(DATA_FILE_NAME), KEY_CLASS, VALUE_CLASS, dataSyncMarker);
            this.indexWriter = new SequenceFile.Writer(
                    dir.resolve(INDEX_FILE_NAME), KEY_CLASS, KEY_CLASS, indexSyncMarker);
            this.entriesWritten = 0;
        }

        void append(long key, byte[] value, int offset, int length) throws IOException {
            byte[] keyBytes = SequenceFile.serializeLong(key);
            byte[] valueBytes = SequenceFile.serializeBytes(value, offset, length);

            // Write index entry at the configured interval
            if (entriesWritten % INDEX_INTERVAL == 0) {
                // Index value is the byte offset in the data file, serialized as LongWritable
                long dataPos = dataWriter.getPosition();
                byte[] indexValue = SequenceFile.serializeLong(dataPos);
                indexWriter.append(keyBytes, indexValue);
            }

            dataWriter.append(keyBytes, valueBytes);
            entriesWritten++;
        }

        @Override
        public void close() throws IOException {
            try {
                dataWriter.close();
            } finally {
                indexWriter.close();
            }
        }
    }

    // -- Reader --

    static class Reader implements Closeable {
        private final Path dir;
        private SequenceFile.Reader dataReader;

        // In-memory index: sorted list of (key, dataFileOffset) pairs
        private long[] indexKeys;
        private long[] indexOffsets;

        // In-memory full scan index for seeking: entry key -> offset in data file
        // Built on first seek if needed
        private long[] allKeys;
        private long[] allOffsets;
        private boolean fullIndexBuilt;

        Reader(Path dir) throws IOException {
            this.dir = dir;
            this.dataReader = new SequenceFile.Reader(dir.resolve(DATA_FILE_NAME));
            loadIndex();
        }

        private void loadIndex() throws IOException {
            Path indexPath = dir.resolve(INDEX_FILE_NAME);
            if (!Files.exists(indexPath)) {
                indexKeys = new long[0];
                indexOffsets = new long[0];
                return;
            }

            List<long[]> entries = new ArrayList<>();
            try (SequenceFile.Reader indexReader = new SequenceFile.Reader(indexPath)) {
                SequenceFile.KeyValue kv = new SequenceFile.KeyValue();
                while (indexReader.next(kv)) {
                    long key = SequenceFile.deserializeLong(kv.key);
                    long offset = SequenceFile.deserializeLong(kv.value);
                    entries.add(new long[]{key, offset});
                }
            }
            indexKeys = new long[entries.size()];
            indexOffsets = new long[entries.size()];
            for (int i = 0; i < entries.size(); i++) {
                indexKeys[i] = entries.get(i)[0];
                indexOffsets[i] = entries.get(i)[1];
            }
        }

        /**
         * Build a complete in-memory index by scanning the entire data file.
         * This is needed for efficient random-access by key.
         */
        private void buildFullIndex() throws IOException {
            if (fullIndexBuilt) {
                return;
            }
            // Reopen data reader from beginning
            dataReader.close();
            dataReader = new SequenceFile.Reader(dir.resolve(DATA_FILE_NAME));

            List<Long> keys = new ArrayList<>();
            List<Long> offsets = new ArrayList<>();

            SequenceFile.KeyValue kv = new SequenceFile.KeyValue();
            while (true) {
                long pos = dataReader.getPosition();
                if (!dataReader.next(kv)) {
                    break;
                }
                long key = SequenceFile.deserializeLong(kv.key);
                keys.add(key);
                offsets.add(pos);
            }

            allKeys = new long[keys.size()];
            allOffsets = new long[offsets.size()];
            for (int i = 0; i < keys.size(); i++) {
                allKeys[i] = keys.get(i);
                allOffsets[i] = offsets.get(i);
            }
            fullIndexBuilt = true;

            // Reopen for subsequent reads
            dataReader.close();
            dataReader = new SequenceFile.Reader(dir.resolve(DATA_FILE_NAME));
        }

        /**
         * Get the value for a specific key by scanning from the beginning.
         */
        void get(long targetKey, SequenceFile.KeyValue kv) throws IOException {
            buildFullIndex();
            // Binary search in full index
            int idx = binarySearch(allKeys, targetKey);
            if (idx < 0) {
                throw new IOException("Key not found: " + targetKey);
            }
            // Reopen and scan to position
            dataReader.close();
            dataReader = new SequenceFile.Reader(dir.resolve(DATA_FILE_NAME));
            // Skip records until we reach the target
            int recordsToSkip = idx;
            for (int i = 0; i < recordsToSkip; i++) {
                if (!dataReader.next(kv)) {
                    throw new IOException("Unexpected EOF while seeking to key: " + targetKey);
                }
            }
            if (!dataReader.next(kv)) {
                throw new IOException("Unexpected EOF at key: " + targetKey);
            }
        }

        /**
         * Position the reader just before the given key, so the next call to
         * {@link #next(SequenceFile.KeyValue)} returns the entry with this key.
         */
        void seek(long targetKey) throws IOException {
            buildFullIndex();
            // Find the index of targetKey
            int idx = binarySearch(allKeys, targetKey);
            if (idx < 0) {
                // Key not found, position at the first key >= targetKey
                idx = -(idx + 1);
            }
            // Reopen and skip to position
            dataReader.close();
            dataReader = new SequenceFile.Reader(dir.resolve(DATA_FILE_NAME));
            SequenceFile.KeyValue tmp = new SequenceFile.KeyValue();
            for (int i = 0; i < idx; i++) {
                if (!dataReader.next(tmp)) {
                    break;
                }
            }
        }

        /**
         * Read the next key-value record.
         */
        boolean next(SequenceFile.KeyValue kv) throws IOException {
            return dataReader.next(kv);
        }

        private static int binarySearch(long[] keys, long target) {
            int lo = 0, hi = keys.length - 1;
            while (lo <= hi) {
                int mid = (lo + hi) >>> 1;
                if (keys[mid] < target) {
                    lo = mid + 1;
                } else if (keys[mid] > target) {
                    hi = mid - 1;
                } else {
                    return mid;
                }
            }
            return -(lo + 1);
        }

        @Override
        public void close() throws IOException {
            dataReader.close();
        }
    }
}
