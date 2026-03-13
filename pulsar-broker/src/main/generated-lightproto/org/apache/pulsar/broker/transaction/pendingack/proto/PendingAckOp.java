package org.apache.pulsar.broker.transaction.pendingack.proto;
public enum PendingAckOp {
	ACK(1), COMMIT(2), ABORT(3),;
	private final int value;
	private PendingAckOp(int value) {
		this.value = value;
	}
	public int getValue() {
		return value;
	}
	public static PendingAckOp valueOf(int n) {
		switch (n) {
			case 1 :
				return ACK;
			case 2 :
				return COMMIT;
			case 3 :
				return ABORT;
			default :
				return null;

		}
	}
	public static final int ACK_VALUE = 1;
	public static final int COMMIT_VALUE = 2;
	public static final int ABORT_VALUE = 3;
}
