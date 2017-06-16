package com.xd.wifimultihop.business.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Log;

public class TcpSocket {

	public class Buffer {
		byte[] buf;
		int size;

		public Buffer(byte[] buf, int size) {
			super();
			this.buf = buf;
			this.size = size;
		}

		public byte[] getBuf() {
			return buf;
		}

		public int getSize() {
			return size;
		}

	}

	private static final String TAG = "TcpSocket";
	private ServerSocket serverSocket;
	private Socket socket;
	private OutputStream outputStream;
	private InputStream inputStream;

	private byte[] buffer;

	// for接收
	public TcpSocket(int port, int bufferSize) {
		super();
		try {
			serverSocket = new ServerSocket(port);
			buffer = new byte[bufferSize];
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			tcpEnd();
		}
	}

	// for发送
	public TcpSocket(int port, String destAddress) {
		super();
		try {
			socket = new Socket(destAddress, port);
			outputStream = socket.getOutputStream();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			tcpEnd();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			tcpEnd();
		}
	}

	public void socketConnect() {
		try {
			if (serverSocket == null) return;
			socket = serverSocket.accept();
			inputStream = socket.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			tcpEnd();
			e.printStackTrace();
		}
	}

	public void tcpEnd() {
		try {
			if (inputStream != null) {
				inputStream.close();
			}
			if (outputStream != null) {
				outputStream.close();
			}
			if (socket != null) {
				socket.close();
			}
			if (serverSocket != null) {
				serverSocket.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Buffer tcpRecv() {
		if(inputStream == null) return null;
		int size;
		try {
			if ((size = inputStream.read(buffer)) != -1) {
				Buffer recvBuf = new Buffer(buffer, size);
				return recvBuf;
			} else {
				return null;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			tcpEnd();
			return null;
		}
	}

	/*
	 * private String getTcpSrcAdd(){ if(socket != null){ String address =
	 * socket.getInetAddress().toString(); String[] temp = address.split("/");
	 * return temp[1]; } else{ return null; } }
	 */

	public void tcpSend(byte[] data, int size) {
		try {
			// 当被叫不在线时的处理
			if (outputStream == null) {
				Log.e(TAG, "outputStream is null");
				return;
			}
			outputStream.write(data, 0, size);
			outputStream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			tcpEnd();
		}
	}
}