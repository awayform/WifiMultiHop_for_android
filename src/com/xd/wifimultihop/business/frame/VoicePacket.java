package com.xd.wifimultihop.business.frame;

public class VoicePacket extends Packet {

	private byte[] tempSeq;
	private int lastSeq;
	private int lossSeq;
	private byte[] tempTime;
	private long curTime;
	private long recvPacketTime;

	public VoicePacket(int length) {
		super();
		mPacket = new byte[length];
		mData = new byte[(length - 12) / 10];
		tempSeq = new byte[4];
		tempTime = new byte[8];
		lastSeq = -1; // 因为读取的包的序号从0开始
		lossSeq = 0;
		seqRecv = 0;
		seqNo = 0;
		curTime = 0;
		recvPacketTime = 0;
	}

	public int getLossSeq() {
		return lossSeq;
	}

	public long getPacketTime() {
		return recvPacketTime;
	}

	@Override
	public byte[] pack(byte[] data) {
		if (seqNo % 10 == 0) {
			curTime = System.currentTimeMillis();
			System.arraycopy(Type.intToBytes(seqNo / 10), 0, mPacket, 0, 4);
			System.arraycopy(Type.longToBytes(curTime), 0, mPacket, 4, 8);
		}
		System.arraycopy(data, 0, mPacket, 12 + (seqNo % 10) * 38, 38);
		seqNo++;
		if (seqNo % 10 == 0) {
			return mPacket;
		} else {
			return null;
		}
	}

	public void setLossSeq() {
		lossSeq = 0;
	}

	@Override
	public byte[] unpack(byte[] packet) {
		if (seqRecv % 10 == 0) {
			System.arraycopy(packet, 0, tempSeq, 0, 4);
			int thisSeq = Type.bytesToInt(tempSeq);
			if (thisSeq <= lastSeq) {
				return null;
			}
			lossSeq = lossSeq + thisSeq - lastSeq - 1;
			lastSeq = thisSeq;
			System.arraycopy(packet, 4, tempTime, 0, 8);
			recvPacketTime = Type.bytesToLong(tempTime);
			System.out.println("[delay]: "
					+ (System.currentTimeMillis() - recvPacketTime));
		}
		System.arraycopy(packet, 12 + (seqRecv % 10) * 38, mData, 0, 38);
		seqRecv++;
		return mData;
	}
}
