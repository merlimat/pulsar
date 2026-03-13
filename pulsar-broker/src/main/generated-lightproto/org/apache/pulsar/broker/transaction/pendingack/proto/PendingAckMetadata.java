package org.apache.pulsar.broker.transaction.pendingack.proto;
public final class PendingAckMetadata {
	private long ledgerId;
	private static final int _LEDGER_ID_FIELD_NUMBER = 1;
	private static final int _LEDGER_ID_TAG = (_LEDGER_ID_FIELD_NUMBER << LightProtoCodec.TAG_TYPE_BITS)
			| LightProtoCodec.WIRETYPE_VARINT;
	private static final int _LEDGER_ID_TAG_SIZE = LightProtoCodec.computeVarIntSize(_LEDGER_ID_TAG);
	private static final int _LEDGER_ID_MASK = 1 << (0 % 32);
	public boolean hasLedgerId() {
		return (_bitField0 & _LEDGER_ID_MASK) != 0;
	}
	public long getLedgerId() {
		if (!hasLedgerId()) {
			throw new IllegalStateException("Field 'ledgerId' is not set");
		}
		return ledgerId;
	}
	public PendingAckMetadata setLedgerId(long ledgerId) {
		this.ledgerId = ledgerId;
		_bitField0 |= _LEDGER_ID_MASK;
		_cachedSize = -1;
		return this;
	}
	public PendingAckMetadata clearLedgerId() {
		_bitField0 &= ~_LEDGER_ID_MASK;
		return this;
	}

	private long entryId;
	private static final int _ENTRY_ID_FIELD_NUMBER = 2;
	private static final int _ENTRY_ID_TAG = (_ENTRY_ID_FIELD_NUMBER << LightProtoCodec.TAG_TYPE_BITS)
			| LightProtoCodec.WIRETYPE_VARINT;
	private static final int _ENTRY_ID_TAG_SIZE = LightProtoCodec.computeVarIntSize(_ENTRY_ID_TAG);
	private static final int _ENTRY_ID_MASK = 1 << (1 % 32);
	public boolean hasEntryId() {
		return (_bitField0 & _ENTRY_ID_MASK) != 0;
	}
	public long getEntryId() {
		if (!hasEntryId()) {
			throw new IllegalStateException("Field 'entryId' is not set");
		}
		return entryId;
	}
	public PendingAckMetadata setEntryId(long entryId) {
		this.entryId = entryId;
		_bitField0 |= _ENTRY_ID_MASK;
		_cachedSize = -1;
		return this;
	}
	public PendingAckMetadata clearEntryId() {
		_bitField0 &= ~_ENTRY_ID_MASK;
		return this;
	}

	private long[] ackSets = null;
	private int _ackSetsCount = 0;
	private static final int _ACK_SET_FIELD_NUMBER = 3;
	private static final int _ACK_SET_TAG = (_ACK_SET_FIELD_NUMBER << LightProtoCodec.TAG_TYPE_BITS)
			| LightProtoCodec.WIRETYPE_VARINT;
	private static final int _ACK_SET_TAG_SIZE = LightProtoCodec.computeVarIntSize(_ACK_SET_TAG);
	private static final int _ACK_SET_TAG_PACKED = (_ACK_SET_FIELD_NUMBER << LightProtoCodec.TAG_TYPE_BITS)
			| LightProtoCodec.WIRETYPE_LENGTH_DELIMITED;
	public int getAckSetsCount() {
		return _ackSetsCount;
	}
	public long getAckSetAt(int idx) {
		if (idx < 0 || idx >= _ackSetsCount) {
			throw new IndexOutOfBoundsException(
					"Index " + idx + " is out of the list size (" + _ackSetsCount + ") for field 'ack_set'");
		}
		return ackSets[idx];
	}
	public void addAckSet(long ackSet) {
		if (ackSets == null) {
			ackSets = new long[4];
		}
		if (ackSets.length == _ackSetsCount) {
			ackSets = java.util.Arrays.copyOf(ackSets, _ackSetsCount * 2);
		}
		_cachedSize = -1;
		ackSets[_ackSetsCount++] = ackSet;
	}
	public PendingAckMetadata clearAckSet() {
		_ackSetsCount = 0;
		return this;
	}

	private int batchSize;
	private static final int _BATCH_SIZE_FIELD_NUMBER = 4;
	private static final int _BATCH_SIZE_TAG = (_BATCH_SIZE_FIELD_NUMBER << LightProtoCodec.TAG_TYPE_BITS)
			| LightProtoCodec.WIRETYPE_VARINT;
	private static final int _BATCH_SIZE_TAG_SIZE = LightProtoCodec.computeVarIntSize(_BATCH_SIZE_TAG);
	private static final int _BATCH_SIZE_MASK = 1 << (3 % 32);
	public boolean hasBatchSize() {
		return (_bitField0 & _BATCH_SIZE_MASK) != 0;
	}
	public int getBatchSize() {
		if (!hasBatchSize()) {
			throw new IllegalStateException("Field 'batch_size' is not set");
		}
		return batchSize;
	}
	public PendingAckMetadata setBatchSize(int batchSize) {
		this.batchSize = batchSize;
		_bitField0 |= _BATCH_SIZE_MASK;
		_cachedSize = -1;
		return this;
	}
	public PendingAckMetadata clearBatchSize() {
		_bitField0 &= ~_BATCH_SIZE_MASK;
		return this;
	}

	private int _bitField0;
	private static final int _REQUIRED_FIELDS_MASK0 = 0 | _LEDGER_ID_MASK | _ENTRY_ID_MASK;
	public int writeTo(io.netty.buffer.ByteBuf _b) {
		checkRequiredFields();
		int _writeIdx = _b.writerIndex();
		LightProtoCodec.writeVarInt(_b, _LEDGER_ID_TAG);
		LightProtoCodec.writeVarInt64(_b, ledgerId);
		LightProtoCodec.writeVarInt(_b, _ENTRY_ID_TAG);
		LightProtoCodec.writeVarInt64(_b, entryId);
		for (int i = 0; i < _ackSetsCount; i++) {
			long _item = ackSets[i];
			LightProtoCodec.writeVarInt(_b, _ACK_SET_TAG);
			LightProtoCodec.writeVarInt64(_b, _item);
		}
		if (hasBatchSize()) {
			LightProtoCodec.writeVarInt(_b, _BATCH_SIZE_TAG);
			LightProtoCodec.writeVarInt(_b, batchSize);
		}
		return (_b.writerIndex() - _writeIdx);
	}
	public int getSerializedSize() {
		if (_cachedSize > -1) {
			return _cachedSize;
		}

		int _size = 0;
		_size += _LEDGER_ID_TAG_SIZE;
		_size += LightProtoCodec.computeVarInt64Size(ledgerId);
		_size += _ENTRY_ID_TAG_SIZE;
		_size += LightProtoCodec.computeVarInt64Size(entryId);
		for (int i = 0; i < _ackSetsCount; i++) {
			long _item = ackSets[i];
			_size += _ACK_SET_TAG_SIZE;
			_size += LightProtoCodec.computeVarInt64Size(_item);
		}
		if (hasBatchSize()) {
			_size += _BATCH_SIZE_TAG_SIZE;
			_size += LightProtoCodec.computeVarIntSize(batchSize);
		}
		_cachedSize = _size;
		return _size;
	}
	public void parseFrom(io.netty.buffer.ByteBuf _buffer, int _size) {
		clear();
		int _endIdx = _buffer.readerIndex() + _size;
		while (_buffer.readerIndex() < _endIdx) {
			int _tag = LightProtoCodec.readVarInt(_buffer);
			switch (_tag) {
				case _LEDGER_ID_TAG :
					_bitField0 |= _LEDGER_ID_MASK;
					ledgerId = LightProtoCodec.readVarInt64(_buffer);
					break;
				case _ENTRY_ID_TAG :
					_bitField0 |= _ENTRY_ID_MASK;
					entryId = LightProtoCodec.readVarInt64(_buffer);
					break;
				case _ACK_SET_TAG :
					addAckSet(LightProtoCodec.readVarInt64(_buffer));
					break;
				case _BATCH_SIZE_TAG :
					_bitField0 |= _BATCH_SIZE_MASK;
					batchSize = LightProtoCodec.readVarInt(_buffer);
					break;
				case _ACK_SET_TAG_PACKED :
					int _ackSetSize = LightProtoCodec.readVarInt(_buffer);
					int _ackSetEndIdx = _buffer.readerIndex() + _ackSetSize;
					while (_buffer.readerIndex() < _ackSetEndIdx) {
						addAckSet(LightProtoCodec.readVarInt64(_buffer));
					}
					break;
				default :
					LightProtoCodec.skipUnknownField(_tag, _buffer);
			}
		}
		checkRequiredFields();
		_parsedBuffer = _buffer;
	}
	private void checkRequiredFields() {
		if ((_bitField0 & _REQUIRED_FIELDS_MASK0) != _REQUIRED_FIELDS_MASK0) {
			throw new IllegalStateException("Some required fields are missing");
		}
	}
	public PendingAckMetadata clear() {
		_ackSetsCount = 0;
		_parsedBuffer = null;
		_cachedSize = -1;
		_bitField0 = 0;
		return this;
	}
	public PendingAckMetadata copyFrom(PendingAckMetadata _other) {
		_cachedSize = -1;
		if (_other.hasLedgerId()) {
			setLedgerId(_other.ledgerId);
		}
		if (_other.hasEntryId()) {
			setEntryId(_other.entryId);
		}
		for (int i = 0; i < _other.getAckSetsCount(); i++) {
			addAckSet(_other.getAckSetAt(i));
		}
		if (_other.hasBatchSize()) {
			setBatchSize(_other.batchSize);
		}
		return this;
	}
	public byte[] toByteArray() {
		byte[] a = new byte[getSerializedSize()];
		io.netty.buffer.ByteBuf b = io.netty.buffer.Unpooled.wrappedBuffer(a).writerIndex(0);
		this.writeTo(b);
		return a;
	}
	public void parseFrom(byte[] a) {
		io.netty.buffer.ByteBuf b = io.netty.buffer.Unpooled.wrappedBuffer(a);
		this.parseFrom(b, b.readableBytes());
	}
	private int _cachedSize;

	private io.netty.buffer.ByteBuf _parsedBuffer;

}
