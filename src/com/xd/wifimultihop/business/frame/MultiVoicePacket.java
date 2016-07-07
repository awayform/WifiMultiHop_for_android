package com.xd.wifimultihop.business.frame;

import com.xd.wifimultihop.business.socket.GetIpAddress;

public class MultiVoicePacket extends Packet {

	private byte[] localIp = (GetIpAddress.getLocalAddressInet() != null) ? GetIpAddress.getLocalAddressInet().getAddress() : null;

	public MultiVoicePacket(int length) {
		mPacket = new byte[length];
		mData = new byte[(length - 8) / 10];
	}

	@Override
	public byte[] pack(byte[] data) {
		if (seqNo % 10 == 0) {
			System.arraycopy(localIp, 0, mPacket, 0, localIp.length);
			System.arraycopy(Type.intToBytes(seqNo / 10), 0, mPacket, 4, 4);
		}
		System.arraycopy(data, 0, mPacket, 8 + (seqNo % 10) * 38, 38);
		seqNo++;
		if (seqNo % 10 == 0) {
			return mPacket;
		} else {
			return null;
		}
	}

	@Override
	public byte[] unpack(byte[] packet) {
		System.arraycopy(packet, 8 + (seqRecv % 10) * 38, mData, 0, 38);
		seqRecv++;
		return mData;
	}
}
