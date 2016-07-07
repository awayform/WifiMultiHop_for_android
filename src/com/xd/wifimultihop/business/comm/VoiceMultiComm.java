package com.xd.wifimultihop.business.comm;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import com.xd.wifimultihop.business.app.Constants;
import com.xd.wifimultihop.business.frame.MultiVoicePacket;
import com.xd.wifimultihop.business.frame.Type;
import com.xd.wifimultihop.business.socket.GetIpAddress;
import com.xd.wifimultihop.business.socket.UdpMulticastSocket;
import com.xd.wifimultihop.business.speex.Speex;

public class VoiceMultiComm {
	private class PlayThread extends Thread {

		// 收端单缓冲
		public class VoiceBuffer {
			short[][] buffer;
			short[] tempBuf;
			private int front;
			private int rear;
			private boolean waitFlag;

			public VoiceBuffer(int queueLen, int bufferSize) {
				buffer = new short[queueLen][bufferSize];
				front = 0;
				rear = 0;
				waitFlag = true;
			}

			public short[] deQueue() {
				if (rear == front) {
					waitFlag = true;
					if (PlayThread.this != null) {
						synchronized (this) {
							while (waitFlag) {
								try {
									Log.i("TAG", "队空，播放等待");
									this.wait();
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}
					return null;
				}
				tempBuf = buffer[front];
				front = (front + 1) % buffer.length;
				return tempBuf;
			}

			public void enQueue(short[] bytes) {
				if ((rear + 1) % buffer.length == front) {
					Log.i("TAG", "********************队满*******************");
					return;
				}
				System.arraycopy(bytes, 0, buffer[rear], 0, bytes.length);
				int rearTemp = rear;
				rear = (rear + 1) % buffer.length;
				if (rearTemp == front) {
					if (PlayThread.this != null) {
						synchronized (this) {
							Log.i("TAG", "入队，播放唤醒");
							waitFlag = false;
							this.notify();
						}
					}
				}
			}
		}

		public int lastSeq = -1;
		public boolean runFlag = true;
		private AudioTrack mAudioTrack;
		private int minOutBufSize;
		private short[] decodeBuf;

		public VoiceBuffer mVoiceBuffer = new VoiceBuffer(Constants.QUEUE_LEN,
				Constants.VOICE_RAW_BUF);

		public void free() {
			if (mAudioTrack != null) {
				mAudioTrack.stop();
				mAudioTrack.release();
				mAudioTrack = null;
			}
		}

		private void init() {
			minOutBufSize = AudioTrack.getMinBufferSize(8000,
					AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_16BIT);
			mAudioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, 8000,
					AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_16BIT, minOutBufSize,
					AudioTrack.MODE_STREAM);
			mAudioTrack.play();
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			init();
			while (runFlag) {
				Log.d("TAG", "deQueue--------------------->" + getName());
				decodeBuf = mVoiceBuffer.deQueue();
				if (decodeBuf != null && mAudioTrack != null) {
					mAudioTrack.write(decodeBuf, 0, decodeBuf.length);
				}

			}
		}
	}

	private class RecvThread extends Thread {

		public boolean runFlag = true;
		private InetAddress ip;
		private byte[] tempIp = new byte[4];
		private byte[] tempSeq = new byte[4];
		private int seq;
		private byte[] packet;
		private byte[] mBuf;
		private short[] decodeBuf = new short[Constants.VOICE_RAW_BUF];

		private void free() {
			if (mMultiSocket != null) {
				mMultiSocket.udpEnd();
				mMultiSocket = null;
			}
			if (speex != null && !deInUse && !enInUse) {
				speex.close();
				speex = null;
			}
		}

		private void process(PlayThread mThread) {
			mMultiSocket.udpSend(packet);
			for (int i = 0; i < 10; i++) {
				if (mPacket == null || packet == null) {
					break;
				}
				mBuf = mPacket.unpack(packet);
				if (mBuf == null || speex == null) {
					break;
				}
				deInUse = true;
				speex.decode(mBuf, decodeBuf, mBuf.length);
				deInUse = false;
				mThread.mVoiceBuffer.enQueue(decodeBuf);
			}
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();

			while (runFlag) {
				packet = mMultiSocket.udpRecv();
				if (packet == null) {
					continue;
				} else {
					System.arraycopy(packet, 0, tempIp, 0, 4);
					try {
						ip = InetAddress.getByAddress(tempIp);
						System.out.println("ip_" + ip);
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (ip.equals(GetIpAddress.getLocalAddressInet())) {
						continue;
					}
					PlayThread tempThread = playMap.get(ip);
					if (tempThread != null) {
						System.arraycopy(packet, 4, tempSeq, 0, 4);
						seq = Type.bytesToInt(tempSeq);
						System.out.println("seq_" + seq);
						// 解决某设备退出后再次进入时的问题
						if (seq < 10 && tempThread.lastSeq > 20) {
							tempThread.lastSeq = -1;
						}
						if (seq <= tempThread.lastSeq) {
							tempThread = null;
							continue;
						} else {
							tempThread.lastSeq = seq;
							process(tempThread);
							tempThread = null;
						}
					} else {
						PlayThread newPlayThread = new PlayThread();
						newPlayThread.start();
						process(newPlayThread);
						playMap.put(ip, newPlayThread);
						newPlayThread = null;
					}
				}
			}
			free();
		}
	}

	private class SendThread extends Thread {

		public boolean runFlag = true;
		private AudioRecord mAudioRecord;
		private int minInBufSize;
		private short[] mBuf;
		private byte[] encodeBuf;
		private byte[] packet;

		public void free() {
			mAudioRecord.stop();
			mAudioRecord.release();
			mAudioRecord = null;
			if (mMultiSocket != null) {
				mMultiSocket.udpEnd();
				mMultiSocket = null;
			}
			if (speex != null && !deInUse && !enInUse) {
				speex.close();
				speex = null;
			}
		}

		private void init() {
			packet = new byte[Constants.UDP_MULTI_VOICE_PACKET];
			mBuf = new short[Constants.VOICE_RAW_BUF];
			encodeBuf = new byte[Constants.UDP_VOICE_BUF];
			minInBufSize = AudioRecord
					.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO,
							AudioFormat.ENCODING_PCM_16BIT);
			mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000,
					AudioFormat.CHANNEL_IN_MONO,
					AudioFormat.ENCODING_PCM_16BIT, minInBufSize);
			mAudioRecord.startRecording();
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			init();
			while (runFlag) {
				int length = mAudioRecord.read(mBuf, 0, mBuf.length);
				if (length == AudioRecord.ERROR_INVALID_OPERATION
						|| length == AudioRecord.ERROR_BAD_VALUE) {
					continue;
				}
				if (speex != null && encodeBuf != null) {
					enInUse = true;
					speex.encode(mBuf, 0, encodeBuf, length);
					enInUse = false;
					if (mPacket != null) {
						packet = mPacket.pack(encodeBuf);
						if (packet != null && mMultiSocket != null) {
							mMultiSocket.udpSend(packet);
						}
					}
				}
			}
			free();
		}
	}

	private SendThread mSendThread;
	private RecvThread mRecvThread;
	private UdpMulticastSocket mMultiSocket;
	private Map<InetAddress, PlayThread> playMap;
	private MultiVoicePacket mPacket;

	// private MulticastLock mLock;

	private Speex speex;

	private boolean enInUse = false;

	private boolean deInUse = false;

	public VoiceMultiComm() {

		speex = new Speex();
		speex.init();
		mPacket = new MultiVoicePacket(Constants.UDP_MULTI_VOICE_PACKET);
		playMap = new HashMap<InetAddress, PlayThread>();
		mMultiSocket = new UdpMulticastSocket(
				Constants.VOICE_MULTICAST_ADDRESS,
				Constants.UDP_VOICE_MULTI_PORT,
				Constants.UDP_MULTI_VOICE_PACKET);
		mRecvThread = new RecvThread();
		mSendThread = new SendThread();
		mRecvThread.start();
		mSendThread.start();
	}

	public void voiceEnd() {

		mSendThread.runFlag = false;
		mRecvThread.runFlag = false;

		Iterator<Map.Entry<InetAddress, VoiceMultiComm.PlayThread>> iterator = playMap
				.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<InetAddress, VoiceMultiComm.PlayThread> entry = iterator
					.next();
			PlayThread value = entry.getValue();
			value.runFlag = false;
			value.free();
		}
		// mLock.release();
	}
}
