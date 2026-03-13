package org.apache.pulsar.broker.transaction.pendingack.proto;
public final class BatchedPendingAckMetadataEntry {
	private java.util.List<PendingAckMetadataEntry> pendingAckLogs = null;
	private int _pendingAckLogsCount = 0;
	private static final int _PENDING_ACK_LOGS_FIELD_NUMBER = 1;
	private static final int _PENDING_ACK_LOGS_TAG = (_PENDING_ACK_LOGS_FIELD_NUMBER << LightProtoCodec.TAG_TYPE_BITS)
			| LightProtoCodec.WIRETYPE_LENGTH_DELIMITED;
	private static final int _PENDING_ACK_LOGS_TAG_SIZE = LightProtoCodec.computeVarIntSize(_PENDING_ACK_LOGS_TAG);
	public int getPendingAckLogsCount() {
		return _pendingAckLogsCount;
	}
	public PendingAckMetadataEntry getPendingAckLogAt(int idx) {
		if (idx < 0 || idx >= _pendingAckLogsCount) {
			throw new IndexOutOfBoundsException("Index " + idx + " is out of the list size (" + _pendingAckLogsCount
					+ ") for field 'pending_ack_logs'");
		}
		return pendingAckLogs.get(idx);
	}
	public java.util.List<PendingAckMetadataEntry> getPendingAckLogsList() {
		if (_pendingAckLogsCount == 0) {
			return java.util.Collections.emptyList();
		} else {
			return pendingAckLogs.subList(0, _pendingAckLogsCount);
		}
	}
	public PendingAckMetadataEntry addPendingAckLog() {
		if (pendingAckLogs == null) {
			pendingAckLogs = new java.util.ArrayList<PendingAckMetadataEntry>();
		}
		if (pendingAckLogs.size() == _pendingAckLogsCount) {
			pendingAckLogs.add(new PendingAckMetadataEntry());
		}
		_cachedSize = -1;
		return pendingAckLogs.get(_pendingAckLogsCount++);
	}
	public BatchedPendingAckMetadataEntry addAllPendingAckLogs(Iterable<PendingAckMetadataEntry> pendingAckLogs) {
		for (PendingAckMetadataEntry _o : pendingAckLogs) {
			addPendingAckLog().copyFrom(_o);
		}
		return this;
	}
	public BatchedPendingAckMetadataEntry clearPendingAckLogs() {
		for (int i = 0; i < _pendingAckLogsCount; i++) {
			pendingAckLogs.get(i).clear();
		}
		_pendingAckLogsCount = 0;
		return this;
	}

	private int _bitField0;
	private static final int _REQUIRED_FIELDS_MASK0 = 0;
	public int writeTo(io.netty.buffer.ByteBuf _b) {
		int _writeIdx = _b.writerIndex();
		for (int i = 0; i < _pendingAckLogsCount; i++) {
			PendingAckMetadataEntry _item = pendingAckLogs.get(i);
			LightProtoCodec.writeVarInt(_b, _PENDING_ACK_LOGS_TAG);
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
		for (int i = 0; i < _pendingAckLogsCount; i++) {
			PendingAckMetadataEntry _item = pendingAckLogs.get(i);
			_size += _PENDING_ACK_LOGS_TAG_SIZE;
			int MsgsizePendingAckLogs = _item.getSerializedSize();
			_size += LightProtoCodec.computeVarIntSize(MsgsizePendingAckLogs) + MsgsizePendingAckLogs;
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
				case _PENDING_ACK_LOGS_TAG :
					int _pendingAckLogsSize = LightProtoCodec.readVarInt(_buffer);
					addPendingAckLog().parseFrom(_buffer, _pendingAckLogsSize);
					break;
				default :
					LightProtoCodec.skipUnknownField(_tag, _buffer);
			}
		}
		_parsedBuffer = _buffer;
	}
	public BatchedPendingAckMetadataEntry clear() {
		for (int i = 0; i < _pendingAckLogsCount; i++) {
			pendingAckLogs.get(i).clear();
		}
		_pendingAckLogsCount = 0;
		_parsedBuffer = null;
		_cachedSize = -1;
		_bitField0 = 0;
		return this;
	}
	public BatchedPendingAckMetadataEntry copyFrom(BatchedPendingAckMetadataEntry _other) {
		_cachedSize = -1;
		for (int i = 0; i < _other.getPendingAckLogsCount(); i++) {
			addPendingAckLog().copyFrom(_other.getPendingAckLogAt(i));
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
