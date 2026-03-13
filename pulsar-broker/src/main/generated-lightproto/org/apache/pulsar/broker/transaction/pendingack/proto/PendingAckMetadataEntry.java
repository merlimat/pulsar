package org.apache.pulsar.broker.transaction.pendingack.proto;
public final class PendingAckMetadataEntry {
	private PendingAckOp pendingAckOp;
	private static final int _PENDING_ACK_OP_FIELD_NUMBER = 1;
	private static final int _PENDING_ACK_OP_TAG = (_PENDING_ACK_OP_FIELD_NUMBER << LightProtoCodec.TAG_TYPE_BITS)
			| LightProtoCodec.WIRETYPE_VARINT;
	private static final int _PENDING_ACK_OP_TAG_SIZE = LightProtoCodec.computeVarIntSize(_PENDING_ACK_OP_TAG);
	private static final int _PENDING_ACK_OP_MASK = 1 << (0 % 32);
	public boolean hasPendingAckOp() {
		return (_bitField0 & _PENDING_ACK_OP_MASK) != 0;
	}
	public PendingAckOp getPendingAckOp() {
		if (!hasPendingAckOp()) {
			throw new IllegalStateException("Field 'pending_ack_op' is not set");
		}
		return pendingAckOp;
	}
	public PendingAckMetadataEntry setPendingAckOp(PendingAckOp pendingAckOp) {
		this.pendingAckOp = pendingAckOp;
		_bitField0 |= _PENDING_ACK_OP_MASK;
		_cachedSize = -1;
		return this;
	}
	public PendingAckMetadataEntry clearPendingAckOp() {
		_bitField0 &= ~_PENDING_ACK_OP_MASK;
		return this;
	}

	private org.apache.pulsar.common.api.proto.CommandAck.AckType ackType;
	private static final int _ACK_TYPE_FIELD_NUMBER = 2;
	private static final int _ACK_TYPE_TAG = (_ACK_TYPE_FIELD_NUMBER << LightProtoCodec.TAG_TYPE_BITS)
			| LightProtoCodec.WIRETYPE_VARINT;
	private static final int _ACK_TYPE_TAG_SIZE = LightProtoCodec.computeVarIntSize(_ACK_TYPE_TAG);
	private static final int _ACK_TYPE_MASK = 1 << (1 % 32);
	public boolean hasAckType() {
		return (_bitField0 & _ACK_TYPE_MASK) != 0;
	}
	public org.apache.pulsar.common.api.proto.CommandAck.AckType getAckType() {
		if (!hasAckType()) {
			throw new IllegalStateException("Field 'ack_type' is not set");
		}
		return ackType;
	}
	public PendingAckMetadataEntry setAckType(org.apache.pulsar.common.api.proto.CommandAck.AckType ackType) {
		this.ackType = ackType;
		_bitField0 |= _ACK_TYPE_MASK;
		_cachedSize = -1;
		return this;
	}
	public PendingAckMetadataEntry clearAckType() {
		_bitField0 &= ~_ACK_TYPE_MASK;
		return this;
	}

	private long txnidLeastBits;
	private static final int _TXNID_LEAST_BITS_FIELD_NUMBER = 3;
	private static final int _TXNID_LEAST_BITS_TAG = (_TXNID_LEAST_BITS_FIELD_NUMBER << LightProtoCodec.TAG_TYPE_BITS)
			| LightProtoCodec.WIRETYPE_VARINT;
	private static final int _TXNID_LEAST_BITS_TAG_SIZE = LightProtoCodec.computeVarIntSize(_TXNID_LEAST_BITS_TAG);
	private static final int _TXNID_LEAST_BITS_MASK = 1 << (2 % 32);
	public boolean hasTxnidLeastBits() {
		return (_bitField0 & _TXNID_LEAST_BITS_MASK) != 0;
	}
	public long getTxnidLeastBits() {
		if (!hasTxnidLeastBits()) {
			throw new IllegalStateException("Field 'txnid_least_bits' is not set");
		}
		return txnidLeastBits;
	}
	public PendingAckMetadataEntry setTxnidLeastBits(long txnidLeastBits) {
		this.txnidLeastBits = txnidLeastBits;
		_bitField0 |= _TXNID_LEAST_BITS_MASK;
		_cachedSize = -1;
		return this;
	}
	public PendingAckMetadataEntry clearTxnidLeastBits() {
		_bitField0 &= ~_TXNID_LEAST_BITS_MASK;
		return this;
	}

	private long txnidMostBits;
	private static final int _TXNID_MOST_BITS_FIELD_NUMBER = 4;
	private static final int _TXNID_MOST_BITS_TAG = (_TXNID_MOST_BITS_FIELD_NUMBER << LightProtoCodec.TAG_TYPE_BITS)
			| LightProtoCodec.WIRETYPE_VARINT;
	private static final int _TXNID_MOST_BITS_TAG_SIZE = LightProtoCodec.computeVarIntSize(_TXNID_MOST_BITS_TAG);
	private static final int _TXNID_MOST_BITS_MASK = 1 << (3 % 32);
	public boolean hasTxnidMostBits() {
		return (_bitField0 & _TXNID_MOST_BITS_MASK) != 0;
	}
	public long getTxnidMostBits() {
		if (!hasTxnidMostBits()) {
			throw new IllegalStateException("Field 'txnid_most_bits' is not set");
		}
		return txnidMostBits;
	}
	public PendingAckMetadataEntry setTxnidMostBits(long txnidMostBits) {
		this.txnidMostBits = txnidMostBits;
		_bitField0 |= _TXNID_MOST_BITS_MASK;
		_cachedSize = -1;
		return this;
	}
	public PendingAckMetadataEntry clearTxnidMostBits() {
		_bitField0 &= ~_TXNID_MOST_BITS_MASK;
		return this;
	}

	private java.util.List<PendingAckMetadata> pendingAckMetadatas = null;
	private int _pendingAckMetadatasCount = 0;
	private static final int _PENDING_ACK_METADATA_FIELD_NUMBER = 5;
	private static final int _PENDING_ACK_METADATA_TAG = (_PENDING_ACK_METADATA_FIELD_NUMBER << LightProtoCodec.TAG_TYPE_BITS)
			| LightProtoCodec.WIRETYPE_LENGTH_DELIMITED;
	private static final int _PENDING_ACK_METADATA_TAG_SIZE = LightProtoCodec
			.computeVarIntSize(_PENDING_ACK_METADATA_TAG);
	public int getPendingAckMetadatasCount() {
		return _pendingAckMetadatasCount;
	}
	public PendingAckMetadata getPendingAckMetadataAt(int idx) {
		if (idx < 0 || idx >= _pendingAckMetadatasCount) {
			throw new IndexOutOfBoundsException("Index " + idx + " is out of the list size ("
					+ _pendingAckMetadatasCount + ") for field 'pending_ack_metadata'");
		}
		return pendingAckMetadatas.get(idx);
	}
	public java.util.List<PendingAckMetadata> getPendingAckMetadatasList() {
		if (_pendingAckMetadatasCount == 0) {
			return java.util.Collections.emptyList();
		} else {
			return pendingAckMetadatas.subList(0, _pendingAckMetadatasCount);
		}
	}
	public PendingAckMetadata addPendingAckMetadata() {
		if (pendingAckMetadatas == null) {
			pendingAckMetadatas = new java.util.ArrayList<PendingAckMetadata>();
		}
		if (pendingAckMetadatas.size() == _pendingAckMetadatasCount) {
			pendingAckMetadatas.add(new PendingAckMetadata());
		}
		_cachedSize = -1;
		return pendingAckMetadatas.get(_pendingAckMetadatasCount++);
	}
	public PendingAckMetadataEntry addAllPendingAckMetadatas(Iterable<PendingAckMetadata> pendingAckMetadatas) {
		for (PendingAckMetadata _o : pendingAckMetadatas) {
			addPendingAckMetadata().copyFrom(_o);
		}
		return this;
	}
	public PendingAckMetadataEntry clearPendingAckMetadata() {
		for (int i = 0; i < _pendingAckMetadatasCount; i++) {
			pendingAckMetadatas.get(i).clear();
		}
		_pendingAckMetadatasCount = 0;
		return this;
	}

	private int _bitField0;
	private static final int _REQUIRED_FIELDS_MASK0 = 0;
	public int writeTo(io.netty.buffer.ByteBuf _b) {
		int _writeIdx = _b.writerIndex();
		if (hasPendingAckOp()) {
			LightProtoCodec.writeVarInt(_b, _PENDING_ACK_OP_TAG);
			LightProtoCodec.writeVarInt(_b, pendingAckOp.getValue());
		}
		if (hasAckType()) {
			LightProtoCodec.writeVarInt(_b, _ACK_TYPE_TAG);
			LightProtoCodec.writeVarInt(_b, ackType.getValue());
		}
		if (hasTxnidLeastBits()) {
			LightProtoCodec.writeVarInt(_b, _TXNID_LEAST_BITS_TAG);
			LightProtoCodec.writeVarInt64(_b, txnidLeastBits);
		}
		if (hasTxnidMostBits()) {
			LightProtoCodec.writeVarInt(_b, _TXNID_MOST_BITS_TAG);
			LightProtoCodec.writeVarInt64(_b, txnidMostBits);
		}
		for (int i = 0; i < _pendingAckMetadatasCount; i++) {
			PendingAckMetadata _item = pendingAckMetadatas.get(i);
			LightProtoCodec.writeVarInt(_b, _PENDING_ACK_METADATA_TAG);
			LightProtoCodec.writeVarInt(_b, _item.getSerializedSize());
			_item.writeTo(_b);
		}
		return (_b.writerIndex() - _writeIdx);
	}
	public int getSerializedSize() {
		if (_cachedSize > -1) {
			return _cachedSize;
		}

		int _size = 0;
		if (hasPendingAckOp()) {
			_size += _PENDING_ACK_OP_TAG_SIZE;
			_size += LightProtoCodec.computeVarIntSize(pendingAckOp.getValue());
		}
		if (hasAckType()) {
			_size += _ACK_TYPE_TAG_SIZE;
			_size += LightProtoCodec.computeVarIntSize(ackType.getValue());
		}
		if (hasTxnidLeastBits()) {
			_size += _TXNID_LEAST_BITS_TAG_SIZE;
			_size += LightProtoCodec.computeVarInt64Size(txnidLeastBits);
		}
		if (hasTxnidMostBits()) {
			_size += _TXNID_MOST_BITS_TAG_SIZE;
			_size += LightProtoCodec.computeVarInt64Size(txnidMostBits);
		}
		for (int i = 0; i < _pendingAckMetadatasCount; i++) {
			PendingAckMetadata _item = pendingAckMetadatas.get(i);
			_size += _PENDING_ACK_METADATA_TAG_SIZE;
			int MsgsizePendingAckMetadata = _item.getSerializedSize();
			_size += LightProtoCodec.computeVarIntSize(MsgsizePendingAckMetadata) + MsgsizePendingAckMetadata;
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
				case _PENDING_ACK_OP_TAG :
					PendingAckOp _pendingAckOp = PendingAckOp.valueOf(LightProtoCodec.readVarInt(_buffer));
					if (_pendingAckOp != null) {
						_bitField0 |= _PENDING_ACK_OP_MASK;
						pendingAckOp = _pendingAckOp;
					}
					break;
				case _ACK_TYPE_TAG :
					org.apache.pulsar.common.api.proto.CommandAck.AckType _ackType = org.apache.pulsar.common.api.proto.CommandAck.AckType
							.valueOf(LightProtoCodec.readVarInt(_buffer));
					if (_ackType != null) {
						_bitField0 |= _ACK_TYPE_MASK;
						ackType = _ackType;
					}
					break;
				case _TXNID_LEAST_BITS_TAG :
					_bitField0 |= _TXNID_LEAST_BITS_MASK;
					txnidLeastBits = LightProtoCodec.readVarInt64(_buffer);
					break;
				case _TXNID_MOST_BITS_TAG :
					_bitField0 |= _TXNID_MOST_BITS_MASK;
					txnidMostBits = LightProtoCodec.readVarInt64(_buffer);
					break;
				case _PENDING_ACK_METADATA_TAG :
					int _pendingAckMetadataSize = LightProtoCodec.readVarInt(_buffer);
					addPendingAckMetadata().parseFrom(_buffer, _pendingAckMetadataSize);
					break;
				default :
					LightProtoCodec.skipUnknownField(_tag, _buffer);
			}
		}
		_parsedBuffer = _buffer;
	}
	public PendingAckMetadataEntry clear() {
		for (int i = 0; i < _pendingAckMetadatasCount; i++) {
			pendingAckMetadatas.get(i).clear();
		}
		_pendingAckMetadatasCount = 0;
		_parsedBuffer = null;
		_cachedSize = -1;
		_bitField0 = 0;
		return this;
	}
	public PendingAckMetadataEntry copyFrom(PendingAckMetadataEntry _other) {
		_cachedSize = -1;
		if (_other.hasPendingAckOp()) {
			setPendingAckOp(_other.pendingAckOp);
		}
		if (_other.hasAckType()) {
			setAckType(_other.ackType);
		}
		if (_other.hasTxnidLeastBits()) {
			setTxnidLeastBits(_other.txnidLeastBits);
		}
		if (_other.hasTxnidMostBits()) {
			setTxnidMostBits(_other.txnidMostBits);
		}
		for (int i = 0; i < _other.getPendingAckMetadatasCount(); i++) {
			addPendingAckMetadata().copyFrom(_other.getPendingAckMetadataAt(i));
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
