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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Native implementation of Hadoop's SequenceFile format (version 6).
 * <p>
 * Supports writing with NONE compression only.
 * Supports reading NONE, RECORD, and BLOCK compressed files.
 */
class SequenceFile {

    static final byte[] SEQ_MAGIC = {'S', 'E', 'Q'};
    static final int VERSION = 6;
    static final int SYNC_SIZE = 16;
    static final int SYNC_INTERVAL = 100 * 1024; // ~100KB between sync markers

    // -- VInt encoding (Hadoop's WritableUtils format) --

    static void writeVInt(DataOutput out, int i) throws IOException {
        writeVLong(out, i);
    }

    static void writeVLong(DataOutput out, long i) throws IOException {
        if (i >= -112 && i <= 127) {
            out.writeByte((int) i);
            return;
        }
        int len = -112;
        if (i < 0) {
            i ^= -1L; // take complement
            len = -120;
        }
        long tmp = i;
        while (tmp != 0) {
            tmp >>= 8;
            len--;
        }
        out.writeByte(len);
        len = (len < -120) ? -(len + 120) : -(len + 112);
        for (int idx = len; idx != 0; idx--) {
            int shift = (idx - 1) * 8;
            out.writeByte((int) ((i >> shift) & 0xFF));
        }
    }

    static int readVInt(DataInput in) throws IOException {
        return (int) readVLong(in);
    }

    static long readVLong(DataInput in) throws IOException {
        byte firstByte = in.readByte();
        int len = decodeVIntSize(firstByte);
        if (len == 1) {
            return firstByte;
        }
        long i = 0;
        for (int idx = 0; idx < len - 1; idx++) {
            byte b = in.readByte();
            i = (i << 8) | (b & 0xFF);
        }
        return (isNegativeVInt(firstByte) ? ~i : i);
    }

    static int decodeVIntSize(byte value) {
        if (value >= -112) {
            return 1;
        } else if (value < -120) {
            return -119 - value;
        }
        return -111 - value;
    }

    static boolean isNegativeVInt(byte value) {
        return value < -120 || (value >= -112 && value < 0);
    }

    // -- Text string encoding --

    static void writeString(DataOutput out, String s) throws IOException {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        writeVInt(out, bytes.length);
        out.write(bytes);
    }

    static String readString(DataInput in) throws IOException {
        int length = readVInt(in);
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    // -- LongWritable serialization --

    static byte[] serializeLong(long value) {
        byte[] bytes = new byte[8];
        bytes[0] = (byte) (value >>> 56);
        bytes[1] = (byte) (value >>> 48);
        bytes[2] = (byte) (value >>> 40);
        bytes[3] = (byte) (value >>> 32);
        bytes[4] = (byte) (value >>> 24);
        bytes[5] = (byte) (value >>> 16);
        bytes[6] = (byte) (value >>> 8);
        bytes[7] = (byte) value;
        return bytes;
    }

    static long deserializeLong(byte[] bytes) {
        return ((long) (bytes[0] & 0xFF) << 56)
                | ((long) (bytes[1] & 0xFF) << 48)
                | ((long) (bytes[2] & 0xFF) << 40)
                | ((long) (bytes[3] & 0xFF) << 32)
                | ((long) (bytes[4] & 0xFF) << 24)
                | ((long) (bytes[5] & 0xFF) << 16)
                | ((long) (bytes[6] & 0xFF) << 8)
                | ((long) (bytes[7] & 0xFF));
    }

    // -- BytesWritable serialization --

    static byte[] serializeBytes(byte[] data, int offset, int length) {
        byte[] result = new byte[4 + length];
        result[0] = (byte) (length >>> 24);
        result[1] = (byte) (length >>> 16);
        result[2] = (byte) (length >>> 8);
        result[3] = (byte) length;
        System.arraycopy(data, offset, result, 4, length);
        return result;
    }

    static byte[] deserializeBytes(byte[] serialized) {
        int length = ((serialized[0] & 0xFF) << 24)
                | ((serialized[1] & 0xFF) << 16)
                | ((serialized[2] & 0xFF) << 8)
                | (serialized[3] & 0xFF);
        byte[] result = new byte[length];
        System.arraycopy(serialized, 4, result, 0, length);
        return result;
    }

    enum CompressionType {
        NONE,
        RECORD,
        BLOCK
    }

    // -- Header --

    static class Header {
        final String keyClassName;
        final String valueClassName;
        final CompressionType compressionType;
        final String compressionCodecClassName;
        final byte[] sync;

        Header(String keyClassName, String valueClassName, CompressionType compressionType,
               String compressionCodecClassName, byte[] sync) {
            this.keyClassName = keyClassName;
            this.valueClassName = valueClassName;
            this.compressionType = compressionType;
            this.compressionCodecClassName = compressionCodecClassName;
            this.sync = sync;
        }
    }

    static Header readHeader(DataInputStream in) throws IOException {
        byte[] magic = new byte[3];
        in.readFully(magic);
        if (magic[0] != SEQ_MAGIC[0] || magic[1] != SEQ_MAGIC[1] || magic[2] != SEQ_MAGIC[2]) {
            throw new IOException("Not a SequenceFile: invalid magic");
        }
        int version = in.readByte() & 0xFF;
        if (version > VERSION) {
            throw new IOException("Unsupported SequenceFile version: " + version);
        }

        String keyClassName = readString(in);
        String valueClassName = readString(in);

        boolean isCompressed = in.readBoolean();
        boolean isBlockCompressed = false;
        if (version >= 4) {
            isBlockCompressed = in.readBoolean();
        }

        String codecClassName = null;
        if (isCompressed) {
            codecClassName = readString(in);
        }

        // Read metadata
        int metaCount = in.readInt();
        for (int i = 0; i < metaCount; i++) {
            readString(in); // key
            readString(in); // value
        }

        byte[] sync = new byte[SYNC_SIZE];
        in.readFully(sync);

        CompressionType ct;
        if (!isCompressed) {
            ct = CompressionType.NONE;
        } else if (isBlockCompressed) {
            ct = CompressionType.BLOCK;
        } else {
            ct = CompressionType.RECORD;
        }

        return new Header(keyClassName, valueClassName, ct, codecClassName, sync);
    }

    static void writeHeader(DataOutputStream out, String keyClassName, String valueClassName,
                            byte[] sync) throws IOException {
        out.write(SEQ_MAGIC);
        out.writeByte(VERSION);
        writeString(out, keyClassName);
        writeString(out, valueClassName);
        out.writeBoolean(false); // isCompressed
        out.writeBoolean(false); // isBlockCompressed (always written for v6)
        // metadata: 0 entries
        out.writeInt(0);
        out.write(sync);
    }

    // -- Writer (NONE compression only) --

    static class Writer implements Closeable {
        private final DataOutputStream out;
        private final CountingOutputStream counting;
        private final byte[] sync;
        private long lastSyncPos;

        Writer(Path path, String keyClassName, String valueClassName) throws IOException {
            this(path, keyClassName, valueClassName, null);
        }

        Writer(Path path, String keyClassName, String valueClassName, byte[] syncMarker)
                throws IOException {
            this.counting = new CountingOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(path,
                            StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                            StandardOpenOption.TRUNCATE_EXISTING)));
            this.out = new DataOutputStream(counting);
            this.sync = new byte[SYNC_SIZE];
            if (syncMarker != null) {
                System.arraycopy(syncMarker, 0, sync, 0, SYNC_SIZE);
            } else {
                ThreadLocalRandom.current().nextBytes(sync);
            }
            writeHeader(out, keyClassName, valueClassName, sync);
            this.lastSyncPos = counting.count;
        }

        long getPosition() {
            return counting.count;
        }

        void append(byte[] key, byte[] value) throws IOException {
            maybeWriteSync();
            int recordLength = key.length + value.length;
            out.writeInt(recordLength);
            out.writeInt(key.length);
            out.write(key);
            out.write(value);
        }

        private void maybeWriteSync() throws IOException {
            if (counting.count - lastSyncPos >= SYNC_INTERVAL) {
                out.writeInt(-1);
                out.write(sync);
                lastSyncPos = counting.count;
            }
        }

        void sync() throws IOException {
            out.writeInt(-1);
            out.write(sync);
            lastSyncPos = counting.count;
        }

        @Override
        public void close() throws IOException {
            out.flush();
            out.close();
        }
    }

    // -- Reader (supports NONE, RECORD, BLOCK compression) --

    static class Reader implements Closeable {
        private final DataInputStream in;
        private final Header header;
        private final CountingInputStream counting;
        private final Path path;

        // For BLOCK compression
        private byte[][] blockKeys;
        private byte[][] blockValues;
        private int blockIndex;
        private int blockSize;

        Reader(Path path) throws IOException {
            this.path = path;
            this.counting = new CountingInputStream(
                    new BufferedInputStream(Files.newInputStream(path, StandardOpenOption.READ)));
            this.in = new DataInputStream(counting);
            this.header = readHeader(in);
        }

        Header getHeader() {
            return header;
        }

        long getPosition() {
            return counting.count;
        }

        /**
         * Read the next key-value record.
         * @param kv output holder for key and value bytes
         * @return true if a record was read, false at EOF
         */
        boolean next(KeyValue kv) throws IOException {
            switch (header.compressionType) {
                case NONE:
                    return nextUncompressed(kv);
                case RECORD:
                    return nextRecordCompressed(kv);
                case BLOCK:
                    return nextBlockCompressed(kv);
                default:
                    throw new IOException("Unsupported compression type: " + header.compressionType);
            }
        }

        private boolean nextUncompressed(KeyValue kv) throws IOException {
            while (true) {
                int recordLength;
                try {
                    recordLength = in.readInt();
                } catch (EOFException e) {
                    return false;
                }
                // Check for sync marker
                if (recordLength == -1) {
                    // Skip sync marker
                    in.skipNBytes(SYNC_SIZE);
                    continue;
                }
                int keyLength = in.readInt();
                kv.key = new byte[keyLength];
                in.readFully(kv.key);
                int valueLength = recordLength - keyLength;
                kv.value = new byte[valueLength];
                in.readFully(kv.value);
                return true;
            }
        }

        private boolean nextRecordCompressed(KeyValue kv) throws IOException {
            while (true) {
                int recordLength;
                try {
                    recordLength = in.readInt();
                } catch (EOFException e) {
                    return false;
                }
                if (recordLength == -1) {
                    in.skipNBytes(SYNC_SIZE);
                    continue;
                }
                int keyLength = in.readInt();
                kv.key = new byte[keyLength];
                in.readFully(kv.key);
                int compressedValueLength = recordLength - keyLength;
                byte[] compressedValue = new byte[compressedValueLength];
                in.readFully(compressedValue);
                kv.value = decompress(compressedValue);
                return true;
            }
        }

        private boolean nextBlockCompressed(KeyValue kv) throws IOException {
            // If we have buffered block records, return next one
            if (blockKeys != null && blockIndex < blockSize) {
                kv.key = blockKeys[blockIndex];
                kv.value = blockValues[blockIndex];
                blockIndex++;
                return true;
            }

            // Read next block
            if (!readBlock()) {
                return false;
            }
            kv.key = blockKeys[blockIndex];
            kv.value = blockValues[blockIndex];
            blockIndex++;
            return true;
        }

        private boolean readBlock() throws IOException {
            // Read sync marker (preceded by int -1)
            int syncPrefix;
            try {
                syncPrefix = in.readInt();
            } catch (EOFException e) {
                return false;
            }
            if (syncPrefix != -1) {
                throw new IOException("Expected sync marker prefix (-1), got: " + syncPrefix);
            }
            byte[] syncCheck = new byte[SYNC_SIZE];
            in.readFully(syncCheck);

            // Number of records in this block
            int numRecords = readVInt(in);
            blockSize = numRecords;
            blockIndex = 0;
            blockKeys = new byte[numRecords][];
            blockValues = new byte[numRecords][];

            // Read compressed key lengths
            byte[] keyLengthsData = readCompressedBlock(in);
            int[] keyLengths = decodeVInts(keyLengthsData, numRecords);

            // Read compressed keys
            byte[] keysData = readCompressedBlock(in);
            blockKeys = splitByLengths(keysData, keyLengths);

            // Read compressed value lengths
            byte[] valueLengthsData = readCompressedBlock(in);
            int[] valueLengths = decodeVInts(valueLengthsData, numRecords);

            // Read compressed values
            byte[] valuesData = readCompressedBlock(in);
            blockValues = splitByLengths(valuesData, valueLengths);

            return true;
        }

        private static byte[] readCompressedBlock(DataInputStream in) throws IOException {
            int compressedSize = readVInt(in);
            byte[] compressed = new byte[compressedSize];
            in.readFully(compressed);
            return decompress(compressed);
        }

        private static int[] decodeVInts(byte[] data, int count) throws IOException {
            int[] result = new int[count];
            DataInputStream dis = new DataInputStream(new java.io.ByteArrayInputStream(data));
            for (int i = 0; i < count; i++) {
                result[i] = readVInt(dis);
            }
            return result;
        }

        private static byte[][] splitByLengths(byte[] data, int[] lengths) {
            byte[][] result = new byte[lengths.length][];
            int offset = 0;
            for (int i = 0; i < lengths.length; i++) {
                result[i] = new byte[lengths[i]];
                System.arraycopy(data, offset, result[i], 0, lengths[i]);
                offset += lengths[i];
            }
            return result;
        }

        /**
         * Reopen the reader at the beginning (after header).
         * Used when we need to scan from the start.
         */
        Reader reopen() throws IOException {
            close();
            return new Reader(path);
        }

        @Override
        public void close() throws IOException {
            in.close();
        }
    }

    static class KeyValue {
        byte[] key;
        byte[] value;
    }

    // -- Compression utilities (deflate/zlib) --

    private static byte[] decompress(byte[] compressed) throws IOException {
        try (InflaterInputStream iis = new InflaterInputStream(
                new java.io.ByteArrayInputStream(compressed))) {
            return iis.readAllBytes();
        }
    }

    private static byte[] decompressWithSize(byte[] compressed, int uncompressedSize) throws IOException {
        Inflater inflater = new Inflater();
        try {
            inflater.setInput(compressed);
            byte[] result = new byte[uncompressedSize];
            int offset = 0;
            while (offset < uncompressedSize) {
                try {
                    int n = inflater.inflate(result, offset, uncompressedSize - offset);
                    if (n == 0 && inflater.finished()) {
                        break;
                    }
                    offset += n;
                } catch (java.util.zip.DataFormatException e) {
                    throw new IOException("Decompression failed", e);
                }
            }
            return result;
        } finally {
            inflater.end();
        }
    }

    // -- Counting streams --

    private static class CountingOutputStream extends OutputStream {
        private final OutputStream wrapped;
        long count;

        CountingOutputStream(OutputStream wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public void write(int b) throws IOException {
            wrapped.write(b);
            count++;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            wrapped.write(b, off, len);
            count += len;
        }

        @Override
        public void flush() throws IOException {
            wrapped.flush();
        }

        @Override
        public void close() throws IOException {
            wrapped.close();
        }
    }

    private static class CountingInputStream extends InputStream {
        private final InputStream wrapped;
        long count;

        CountingInputStream(InputStream wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public int read() throws IOException {
            int b = wrapped.read();
            if (b >= 0) {
                count++;
            }
            return b;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int n = wrapped.read(b, off, len);
            if (n > 0) {
                count += n;
            }
            return n;
        }

        @Override
        public long skip(long n) throws IOException {
            long skipped = wrapped.skip(n);
            count += skipped;
            return skipped;
        }

        @Override
        public void skipNBytes(long n) throws IOException {
            wrapped.skipNBytes(n);
            count += n;
        }

        @Override
        public int available() throws IOException {
            return wrapped.available();
        }

        @Override
        public void close() throws IOException {
            wrapped.close();
        }
    }
}
