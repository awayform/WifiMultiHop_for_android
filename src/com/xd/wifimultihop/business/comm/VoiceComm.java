package com.xd.wifimultihop.business.comm;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

import com.xd.wifimultihop.business.app.Constants;
import com.xd.wifimultihop.business.frame.Type;
import com.xd.wifimultihop.business.frame.VoicePacket;
import com.xd.wifimultihop.business.socket.UdpSocket;
import com.xd.wifimultihop.business.speex.Speex;

public class VoiceComm {

	public class DelayTestThread extends Thread {
		public boolean runFlag = true;

		private void free() {
			if (delayTestSocket != null) {
				delayTestSocket.udpEnd();
				delayTestSocket = null;
			}
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			byte[] delayPacketSend = new byte[9];
			byte[] delayPacketRecv;
			byte[] delayPacketReply = new byte[9];
			byte[] delayPacketNotify = new byte[9];
			delayPacketSend[0] = 0; // 0 for ask
			delayPacketReply[0] = 1; // 1 for reply
			delayPacketNotify[0] = 2; // 2 for notify
			long timeSend = System.currentTimeMillis();
			delayTestSocket.udpSend(delayPacketSend);
			while (runFlag) {
				delayPacketRecv = delayTestSocket.udpRecv();
				if (delayPacketRecv == null) {
					continue;
				}
				long timeRecv = System.currentTimeMillis();
				switch (delayPacketRecv[0]) {
				case 0: // 收到请求，打包当前时间，发送回应
					System.arraycopy(Type.longToBytes(timeRecv), 0,
							delayPacketReply, 1, 8);
					delayTestSocket.udpSend(delayPacketReply);
					break;
				case 1: // 收到回应，读取包内时间，计算系统时间差，并通知给对方
					byte[] timeGet = new byte[8];
					System.arraycopy(delayPacketRecv, 1, timeGet, 0, 8);
					timeDiff = Type.bytesToLong(timeGet)
							- (timeRecv + timeSend) / 2; // 计算通话双方手机系统的时间差值
					System.arraycopy(Type.longToBytes(timeDiff), 0,
							delayPacketNotify, 1, 8);
					delayTestSocket.udpSend(delayPacketNotify);
					runFlag = false;
					free();
					break;
				case 2: // 收到时间差通知，赋值
					byte[] timeDiffGet = new byte[8];
					System.arraycopy(delayPacketRecv, 1, timeDiffGet, 0, 8);
					timeDiff = -Type.bytesToLong(timeDiffGet);
					runFlag = false;
					free();
					break;
				default:
					break;
				}
			}
			free();
		}
	}

	private class RecordingThread extends Thread {

		private AudioRecord mAudioRecord;
		public boolean runFlag;
		private short[] mBuf;
		private byte[] encodeBuf;
		private byte[] packet;
		private int minInBufSize;

		private void free() {
			if (mUdpSocket != null) {
				mUdpSocket.udpEnd();
				mUdpSocket = null;
			}
			if (mAudioRecord != null) {
				try {
					mAudioRecord.stop();
					mAudioRecord.release();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} finally {
					mAudioRecord = null;
				}
			}
			if (speex != null && !deInUse && !enInUse) {
				speex.close();
				speex = null;
			}
		}

		private void init() {
			minInBufSize = AudioRecord
					.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO,
							AudioFormat.ENCODING_PCM_16BIT);
			mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000,
					AudioFormat.CHANNEL_IN_MONO,
					AudioFormat.ENCODING_PCM_16BIT, minInBufSize);
			runFlag = true;
			mBuf = new short[Constants.VOICE_RAW_BUF];
			encodeBuf = new byte[Constants.UDP_VOICE_BUF];
			packet = new byte[Constants.UDP_VOICE_PACKET];
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			init();
			if (mAudioRecord == null) {
				return;
			}
			mAudioRecord.startRecording();
			while (runFlag) {
				// 这个返回的length和参数里面的mBuf.length相同，那保存长度还有什么必要？
				int length = mAudioRecord.read(mBuf, 0, mBuf.length);
				// Log.i("TAG","length前:" + length);
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
						if (packet != null && mUdpSocket != null) {
							mUdpSocket.udpSend(packet);
						}
					}
				}
			}
			free();
		}
	}

	private class VoicePlayThread extends Thread {
		public boolean runFlag;
		// private UdpSocket mUdpSocket;
		private AudioTrack mAudioTrack;
		private int minOutBufSize;
		private byte[] mBuf;
		private byte[] packet;
		private short[] decodeBuf;
		private boolean loopFlag;
		private int statsPacketSum;
		private int statsLossSum;
		private long statsTempTime;
		private float statsLossPacketRate;
		private float statsThroughput;
		private Intent bcIntent;
		private long statsDelay;

		private void free() {
			if (mUdpSocket != null) {
				mUdpSocket.udpEnd();
				mUdpSocket = null;
			}
			if (mAudioTrack != null) {
				try {
					mAudioTrack.stop();
					mAudioTrack.release();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} finally {
					mAudioTrack = null;
				}
			}
			if (speex != null && !deInUse && !enInUse) {
				speex.close();
				speex = null;
			}
		}

		private void init() {
			runFlag = true;
			// 最小的用于声卡播放的buffer大小，AudioTrack的bufferSizeInBytes
			// 值不应小于该值，播放时write的buffer大小不应大于AudioTrack的bufferSizeInBytes
			minOutBufSize = AudioTrack.getMinBufferSize(8000,
					AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_16BIT);
			mAudioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, 8000,
					AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_16BIT, minOutBufSize,
					AudioTrack.MODE_STREAM);
			decodeBuf = new short[Constants.VOICE_RAW_BUF];
			if (playType) {
				statsPacketSum = 0;
				statsLossSum = 0;
				statsThroughput = 0;
				statsTempTime = System.currentTimeMillis();
				bcIntent = new Intent();
				bcIntent.setAction(Constants.ACTION_BC_STATS);
				loopFlag = false;
				statsDelay = 0;
			}
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			init();
			if (mAudioTrack != null) {
				mAudioTrack.play();
			}
			while (runFlag) {
				if (loopFlag) {
					loopFlag = false;
				}
				packet = mUdpSocket.udpRecv();
				if (packet == null) {
					continue;
				}
				for (int i = 0; i < 10; i++) {
					if (mPacket == null) {
						loopFlag = true;
						break;
					}
					mBuf = mPacket.unpack(packet);
					if (mBuf == null) {
						loopFlag = true;
						break;
					}
					if (speex != null) {
						deInUse = true;
						int length = speex.decode(mBuf, decodeBuf, mBuf.length);
						deInUse = false;
						// Log.i("TAG", "decode size:"+length);
						if (decodeBuf != null && mAudioTrack != null) {
							// 有可能write进去就立即播放了？
							mAudioTrack.write(decodeBuf, 0, length);
						}
					}
				}
				if (!loopFlag) {
					if (mAudioTrack != null) {
						try {
							mAudioTrack.flush();
						} catch (IllegalStateException e) {
							e.printStackTrace();
						}
					}
					if (playType) {
						// 统计信息
						if (++statsPacketSum == 10) {
							statsLossSum = mPacket.getLossSeq();
							statsLossPacketRate = statsLossSum * 100
									/ (float) (statsPacketSum + statsLossSum);
							statsThroughput = (float) (statsPacketSum
									* Constants.UDP_VOICE_PACKET * 8)
									/ (System.currentTimeMillis() - statsTempTime);
							statsDelay = System.currentTimeMillis()
									- mPacket.getPacketTime() + timeDiff;
							String[] value = {
									String.format("%.2f", statsLossPacketRate),
									String.format("%.2f", statsThroughput),
									Long.toString(statsDelay) };
							bcIntent.putExtra(Constants.EXTRA_BC, value);
							mContext.sendBroadcast(bcIntent);
							statsPacketSum = 0;
							mPacket.setLossSeq();
							statsTempTime = System.currentTimeMillis();
						}
					}
				}
			}
			free();
		}
	}

	private RecordingThread mRecordingThread;
	private VoicePlayThread mVoicePlayThread;
	private DelayTestThread mDelayTestThread;
	private Speex speex;
	private VoicePacket mPacket;
	private UdpSocket mUdpSocket;
	private UdpSocket delayTestSocket;
	private Context mContext;
	private long timeDiff;

	private boolean enInUse = false;

	private boolean deInUse = false;

	private boolean playType; // true for voice , false for video

	public VoiceComm(Context context, String dstAddr, boolean playType) {
		this.mContext = context;
		this.playType = playType;
		timeDiff = 0;
		speex = new Speex();
		speex.init();
		mPacket = new VoicePacket(Constants.UDP_VOICE_PACKET);
		mRecordingThread = new RecordingThread();
		mVoicePlayThread = new VoicePlayThread();
		mUdpSocket = new UdpSocket(Constants.UDP_VOICE_PORT,
				Constants.UDP_VOICE_PACKET, Constants.UDP_VOICE_SOCKET_BUF);
		mUdpSocket.udpSendSetting(dstAddr);
		mRecordingThread.start();
		mVoicePlayThread.start();
		if (playType) {
			mDelayTestThread = new DelayTestThread();
			delayTestSocket = new UdpSocket(Constants.UDP_VOICE_DELAY_PORT,
					Constants.UDP_VOICE_DELAY_PACKET);
			delayTestSocket.udpSendSetting(dstAddr);
			mDelayTestThread.start();
		}
	}

	public void voiceEnd() {
		mRecordingThread.runFlag = false;
		mVoicePlayThread.runFlag = false;
		if (mDelayTestThread != null) {
			mDelayTestThread.runFlag = false;
		}
	}
}