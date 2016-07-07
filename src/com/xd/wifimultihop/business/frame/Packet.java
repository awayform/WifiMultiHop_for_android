package com.xd.wifimultihop.business.frame;

public class Packet {
	public byte[] mPacket;
	public byte[] mData;
	public int seqNo;
	public int seqRecv;

	public byte[] pack(byte[] data) {
		return mPacket;
	}

	public byte[] unpack(byte[] packet) {
		return mData;
	}
}
