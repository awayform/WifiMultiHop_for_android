package com.xd.wifimultihop.business.socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UdpMulticastSocket {
	private InetAddress groupIp;
	int port;
	private MulticastSocket mSocket;
	private DatagramPacket recvPacket;
	private byte[] recvBuf;

	public UdpMulticastSocket(String multiAddr, int port, int length) {
		super();
		try {
			this.groupIp = InetAddress.getByName(multiAddr);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		this.port = port;
		try {
			mSocket = new MulticastSocket(port);
			// 限制缓冲区大小
			mSocket.setSendBufferSize(length);
			mSocket.setReceiveBufferSize(length);
			mSocket.joinGroup(groupIp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			mSocket.setLoopbackMode(true);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		recvBuf = new byte[length];
	}

	public String getUdpSrcAdd() {
		if (recvPacket != null) {
			String address = recvPacket.getAddress().toString();
			String[] temp = address.split("/");
			return temp[1];
		} else {
			return null;
		}
	}

	public void udpEnd() {
		if (mSocket != null) {
			try {
				mSocket.leaveGroup(groupIp);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mSocket.close();
		}
	}

	public byte[] udpRecv() {
		try {
			recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
			mSocket.receive(recvPacket);
			int len = recvPacket.getLength();
			byte[] mData = new byte[len];
			System.arraycopy(recvPacket.getData(), 0, mData, 0, len);
			return mData;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public void udpSend(byte[] data) {
		DatagramPacket packet = new DatagramPacket(data, data.length, groupIp,
				port);
		try {
			mSocket.send(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
