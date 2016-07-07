package com.xd.wifimultihop.business.socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UdpSocket {

	private DatagramSocket socket;
	private DatagramPacket recvPacket;
	private InetAddress destAddress;
	private byte[] recvBuf;
	private int port;

	// 不限制缓冲区大小
	public UdpSocket(int port, int length) {
		super();
		try {
			this.port = port;
			socket = new DatagramSocket(port);
			// 最高可靠性
			socket.setTrafficClass(0x04);
			recvBuf = new byte[length];
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			udpEnd();
		}
	}

	// 限制缓冲区大小
	public UdpSocket(int port, int length, int size) {
		super();
		try {
			this.port = port;
			socket = new DatagramSocket(port);
			// 最小时延
			// socket.setTrafficClass(0x10);
			socket.setSendBufferSize(size);
			socket.setReceiveBufferSize(size);
			recvBuf = new byte[length];
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			udpEnd();
		}
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
		if (socket != null) {
			socket.close();
		}
	}

	public byte[] udpRecv() {
		try {
			recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
			socket.receive(recvPacket);
			int len = recvPacket.getLength();
			byte[] mData = new byte[len];
			System.arraycopy(recvPacket.getData(), 0, mData, 0, len);
			return mData;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			udpEnd();
			return null;
		}
	}

	public void udpSend(byte[] data) {
		DatagramPacket packet = new DatagramPacket(data, data.length,
				destAddress, port);
		try {
			socket.send(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			udpEnd();
		}
	}

	public void udpSendSetting(String ip) {
		try {
			destAddress = InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
