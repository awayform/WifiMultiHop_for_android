package com.xd.wifimultihop.business.app;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.xd.wifimultihop.business.comm.FileComm;
import com.xd.wifimultihop.business.comm.VideoComm;
import com.xd.wifimultihop.business.comm.VoiceComm;
import com.xd.wifimultihop.business.comm.VoiceMultiComm;
import com.xd.wifimultihop.business.frame.ChatMessage;
import com.xd.wifimultihop.business.frame.Type;
import com.xd.wifimultihop.business.socket.GetIpAddress;
import com.xd.wifimultihop.business.socket.TcpSocket;
import com.xd.wifimultihop.business.socket.TcpSocket.Buffer;
import com.xd.wifimultihop.business.ui.FileTransferActivity;
import com.xd.wifimultihop.business.ui.TalkingActivity;
import com.xd.wifimultihop.business.ui.VideoCallActivity;

public class CommService extends Service {

	public class ListenThread {
		private class CmdRecvThread extends Thread {

			public boolean runFlag;
			private TcpSocket mTcpSocket;

			public void free() {
				// runFlag = false;
				if (mTcpSocket != null) {
					mTcpSocket.tcpEnd();
					mTcpSocket = null;
				}
			}

			private void init() {
				runFlag = true;
				mTcpSocket = new TcpSocket(Constants.TCP_LISTEN_PORT,
						Constants.TCP_CMD_BUF);
				// buffer = new byte[Constants.TCP_CMD_BUF];
			}

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				init();
				while (runFlag) {
					mTcpSocket.socketConnect();
					if (mTcpSocket != null) {
						Buffer recvBuffer = mTcpSocket.tcpRecv();
						if (recvBuffer != null) {
							buffer = recvBuffer.getBuf();
							cmdRecvProcess();
						}
					}
				}
				// free();
			}
		}

		private class CmdSendThread extends Thread {
			public boolean runFlag;
			private TcpSocket mTcpSocket;
			private String dstAddress;
			private byte[] sendBuf;
			private boolean flag;

			public CmdSendThread(String dstAddress, byte[] sendBuf, boolean flag) {
				super();
				this.dstAddress = dstAddress;
				this.sendBuf = sendBuf;
				this.flag = flag;
			}

			public void free() {
				if (mTcpSocket != null) {
					mTcpSocket.tcpEnd();
					mTcpSocket = null;
				}
			}

			private void init() {
				runFlag = true;
				mTcpSocket = new TcpSocket(Constants.TCP_LISTEN_PORT,
						dstAddress);
			}

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				init();
				mTcpSocket.tcpSend(sendBuf, sendBuf.length);
				if (flag) {
					try {
						int i = 0;
						while (runFlag && (i <= 3)) { // 最大重传次数4次
							sleep(1500);
							if (mTcpSocket != null) {
								mTcpSocket.tcpSend(sendBuf, sendBuf.length);
							}
							i++;
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				free();
			}

		}

		private byte[] buffer;

		private CmdSendThread mCmdSendThread;

		private CmdRecvThread mCmdRecvThread;

		public ListenThread() {
			super();
			mCmdRecvThread = new CmdRecvThread();
			mCmdRecvThread.start();
		}

		public void cmdRecvOver() {
			if (mCmdRecvThread != null) {
				mCmdRecvThread.runFlag = false;
				mCmdRecvThread.free();
				mCmdRecvThread = null;
			}
		}

		private void cmdRecvProcess() {
			byte[] data = new byte[Constants.TCP_CMD_BUF];
			String tempSrcAddr;
			Intent serviceBcIntent = new Intent(Constants.ACTION_BC_SERVICE);
			switch (buffer[0]) {
			case Constants.MSG:

				tempSrcAddr = getSrcAddr();
				String getMsgContent = null;
				ChatMessage recvMsg = null;
				byte[] tempMsgSize = new byte[4];
				System.arraycopy(buffer, 6, tempMsgSize, 0, 4);
				int getMsgSize = Type.bytesToInt(tempMsgSize);
				byte[] tempMsgContent = new byte[getMsgSize];
				System.arraycopy(buffer, 10, tempMsgContent, 0, getMsgSize);
				try {
					getMsgContent = new String(tempMsgContent, "UTF-8");
					recvMsg = new ChatMessage(ChatMessage.MSG_TYPE_FROM,
							getMsgContent);
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				binder.setMsgMap(tempSrcAddr, recvMsg);
				if (!mainActivityPauseFlag
						|| (mainActivityPauseFlag && chatFlag && (chatAddr
								.equals(tempSrcAddr)))) {
					bcIntent.putExtra(Constants.EXTRA_BC, Constants.MSG);
					bcIntent.putExtra(Constants.EXTRA_IP, tempSrcAddr);
					CommService.this.sendBroadcast(bcIntent);
					if (newMsgList.contains(tempSrcAddr)) {
						newMsgList.remove(tempSrcAddr);
					}
				} else {
					if (!newMsgList.contains(tempSrcAddr)) {
						newMsgList.add(tempSrcAddr);
					}
				}
				break;
			// 收到请求帧，需要做的处理：启动Activity，等待用户操作
			case Constants.REQ:
				// 如果本机正在通话，再次收到请求帧时，所做的处理
				tempSrcAddr = getSrcAddr();
				if (busyFlag) {
					if (tempSrcAddr.equals(dstAddr)) {
						data[0] = Constants.REQ_ACK;
						data[1] = buffer[1];
					} else {
						data[0] = Constants.BUSY;
						Log.w(TAG, "busy now , req refused");
					}
					cmdSend(tempSrcAddr, data, false);
					return;
				}
				busyFlag = true;
				// 记录正在通话的对方的地址，从而再次收到语音请求帧时判断，如果还是该地址，直接发送ack，如果不是改地址，返回nak
				dstAddr = tempSrcAddr;
				Intent reqIntent = new Intent();
				// 非Activity启动Activity，必须要有下面这句。
				reqIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				switch (buffer[1]) {
				case Constants.VOICE_REQ:
					reqIntent.setClass(CommService.this, TalkingActivity.class);
					reqIntent.putExtra(Constants.EXTRA_TALKING, false); // false:被动打开
					break;
				case Constants.VIDEO_REQ:
					reqIntent.setClass(CommService.this,
							VideoCallActivity.class);
					reqIntent.putExtra(Constants.EXTRA_IP, dstAddr); // for the
																		// instance
																		// of
																		// VideoComm
																		// Class
					reqIntent.putExtra(Constants.EXTRA_VIDEOCALL, false); // false:被动打开
					break;
				case Constants.FILE_REQ:
					// 首先判断sdcard是否为空，如果为空，发一个
					if (!FileComm.hasSdCard()) {
						busyFlag = false;
						data[0] = Constants.REQ_NAK;
						data[1] = Constants.FILE_REQ_NAK;
						cmdSend(dstAddr, data, false);
						serviceBcIntent.putExtra(Constants.EXTRA_CMD,
								Constants.FILE_REQ);
						CommService.this.sendBroadcast(serviceBcIntent);
						return;
					}
					reqIntent.setClass(CommService.this,
							FileTransferActivity.class);
					reqIntent.putExtra(Constants.EXTRA_FILETRANSFER, false); // false:被动打开
					String getFileName;
					long getFileSize;
					byte[] tempFileSize = new byte[8];
					System.arraycopy(buffer, 6, tempFileSize, 0, 8);
					getFileSize = Type.bytesToLong(tempFileSize);
					fileSize = getFileSize;
					int fileNameLength = buffer[14];
					byte[] tempFileName = new byte[fileNameLength];
					System.arraycopy(buffer, 15, tempFileName, 0,
							fileNameLength);
					try {
						getFileName = new String(tempFileName, "UTF-8");
						fileName = getFileName;
						reqIntent.putExtra(Constants.EXTRA_FILENAME,
								getFileName);
					} catch (UnsupportedEncodingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					reqIntent.putExtra(Constants.EXTRA_FILESIZE, getFileSize);
					break;
				default:
					break;
				}
				CommService.this.startActivity(reqIntent);
				break;
			// 收到确认帧，需要做的处理：结束请求帧重发，更改当前Activity
			case Constants.REQ_ACK:
				if (talkingFlag) {
					Log.i("TAG", "Already talking , req ack refused");
					return;
				}
				talkingFlag = true;
				cmdSendOver();
				bcIntent.putExtra(Constants.EXTRA_BC, buffer[1]);
				CommService.this.sendBroadcast(bcIntent);
				switch (buffer[1]) {
				case Constants.VOICE_REQ_ACK:
					if (mVoiceComm == null) {
						mVoiceComm = new VoiceComm(getApplicationContext(),
								dstAddr, true);
					}
					break;
				case Constants.VIDEO_REQ_ACK:
					if (mVoiceComm == null) {
						mVoiceComm = new VoiceComm(getApplicationContext(),
								dstAddr, false);
					}
					if (mVideoComm == null) {
						mVideoComm = VideoComm.getInstance(
								getApplicationContext(), dstAddr);
					}
					break;
				case Constants.FILE_REQ_ACK:
					if (mFileComm == null) {
						mFileComm = new FileComm(getApplicationContext(),
								filePath, dstAddr);
					}
					break;
				default:
					break;
				}
				break;
			// 收到拒绝帧，需要做的处理：结束请求帧重发，关闭当前Activity，返回拨号界面
			case Constants.REQ_NAK:
				cmdSendOver();
				bcIntent.putExtra(Constants.EXTRA_BC, buffer[1]);
				CommService.this.sendBroadcast(bcIntent);

				serviceBcIntent
						.putExtra(Constants.EXTRA_CMD, Constants.REQ_NAK);
				CommService.this.sendBroadcast(serviceBcIntent);
				busyFlag = false;
				break;
			// 收到结束帧，需要做的处理：返回结束确认帧，通知Activity结束,通信结束。
			case Constants.END:
				tempSrcAddr = getSrcAddr();
				if (!tempSrcAddr.equals(dstAddr)) {
					return;
				}
				data[0] = Constants.END_ACK;
				switch (buffer[1]) {
				case Constants.VOICE_END:
					data[1] = Constants.VOICE_END_ACK;
					break;
				case Constants.VIDEO_END:
					data[1] = Constants.VIDEO_END_ACK;
					break;
				case Constants.FILE_END:
					data[1] = Constants.FILE_END_ACK;
				default:
					break;
				}
				cmdSend(dstAddr, data, false);
				// 如果第一次收到结束请求帧
				if (talkingFlag) {
					bcIntent.putExtra(Constants.EXTRA_BC, buffer[1]);
					CommService.this.sendBroadcast(bcIntent);
					if (mVoiceComm != null) {
						mVoiceComm.voiceEnd();
						mVoiceComm = null;
					}
					if (mVideoComm != null) {
						mVideoComm.videoEnd();
						mVideoComm = null;
					}
					if (mFileComm != null) {
						if (!fileSrc) {
							mFileComm.fileCancel();
						}
						mFileComm.fileTransEnd();
						mFileComm = null;
					}
					talkingFlag = false;
					busyFlag = false;
				}
				break;
			// 收到结束确认帧，需要做的处理：结束结束请求帧的发送。
			case Constants.END_ACK:
				cmdSendOver();
				break;
			case Constants.CANCEL:
				tempSrcAddr = getSrcAddr();
				if (!tempSrcAddr.equals(dstAddr)) {
					return;
				}
				data[0] = Constants.CANCEL_ACK;
				cmdSend(dstAddr, data, false);
				if (busyFlag) {
					switch (buffer[1]) {
					case Constants.VOICE_CANCEL:
						bcIntent.putExtra(Constants.EXTRA_BC,
								Constants.VOICE_END);
						break;
					case Constants.VIDEO_CANCEL:
						bcIntent.putExtra(Constants.EXTRA_BC,
								Constants.VIDEO_END);
						break;
					case Constants.FILE_CANCEL:
						bcIntent.putExtra(Constants.EXTRA_BC,
								Constants.FILE_END);
						break;
					default:
						break;
					}
					CommService.this.sendBroadcast(bcIntent);
					busyFlag = false;
				}
				break;
			case Constants.CANCEL_ACK:
				cmdSendOver();
				break;
			case Constants.BUSY:
				serviceBcIntent.putExtra(Constants.EXTRA_CMD, Constants.BUSY);
				CommService.this.sendBroadcast(serviceBcIntent);
				switch (whichReq) {
				case Constants.VOICE_REQ:
					bcIntent.putExtra(Constants.EXTRA_BC, Constants.VOICE_END);
					break;
				case Constants.VIDEO_REQ:
					bcIntent.putExtra(Constants.EXTRA_BC, Constants.VIDEO_END);
					break;
				case Constants.FILE_REQ:
					bcIntent.putExtra(Constants.EXTRA_BC, Constants.FILE_END);
				default:
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				CommService.this.sendBroadcast(bcIntent);
				busyFlag = false;
				break;
			case Constants.PAUSE:
				if (fileSrc) {
					if (buffer[1] == Constants.FILE_PAUSE) {
						mFileComm.fileSendPause(true);
					} else if (buffer[1] == Constants.FILE_RESUME) {
						mFileComm.fileSendPause(false);
					}
				} else {
					if (buffer[1] == Constants.FILE_PAUSE) {
						mFileComm.fileRecvPause(true);
					} else if (buffer[1] == Constants.FILE_RESUME) {
						mFileComm.fileRecvPause(false);
					}
				}
				bcIntent.putExtra(Constants.EXTRA_BC, buffer[1]);
				CommService.this.sendBroadcast(bcIntent);
				break;
			default:
				break;
			}
		}

		public void cmdSend(String dstAddress, byte[] data, boolean flag) {
			if (mCmdSendThread != null) {
				mCmdSendThread = null;
			}
			mCmdSendThread = new CmdSendThread(dstAddress, data, flag);
			mCmdSendThread.start();
		}

		public void cmdSendOver() {
			if (mCmdSendThread != null) {
				mCmdSendThread.runFlag = false;
				mCmdSendThread.free();
				mCmdSendThread = null;
			}
		}

		private String getSrcAddr() {
			byte[] tempGetAddr = new byte[4];
			System.arraycopy(buffer, 2, tempGetAddr, 0, 4);
			String[] tempInetIp = new String[2];
			try {
				tempInetIp = InetAddress.getByAddress(tempGetAddr).toString()
						.split("/");
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return tempInetIp[1];
		}
	}

	public class MyBinder extends Binder {

		public ArrayList<ChatMessage> getMsgList(String ip) {
			return msgMap.get(ip);
		}

		public String getNewMsg() {
			String ip = null;
			Iterator<String> newMsgIterator = newMsgList.iterator();
			if (newMsgIterator.hasNext()) {
				ip = newMsgIterator.next();
				newMsgList.remove(ip);
			}
			return ip;
		}

		public void sendMsg(String addr, String msg) {
			byte[] data = new byte[Constants.TCP_CMD_BUF];
			data[0] = Constants.MSG;
			byte[] tempHostAddr = GetIpAddress.getLocalAddressInet()
					.getAddress();
			System.arraycopy(tempHostAddr, 0, data, 2, 4);
			byte[] tempMsg;
			try {
				tempMsg = msg.getBytes("UTF-8");
				byte[] tempMsgLength = Type.intToBytes(tempMsg.length);
				System.arraycopy(tempMsgLength, 0, data, 6, 4);
				System.arraycopy(tempMsg, 0, data, 10, tempMsg.length);
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			mListenThread.cmdSend(addr, data, false);
		}

		public void setChatAddr(String addr) {
			if (addr != null) {
				chatFlag = true;
			} else {
				chatFlag = false;
			}
			chatAddr = addr;
		}

		public ArrayList<ChatMessage> setMsgMap(String ip, ChatMessage msg) {
			ArrayList<ChatMessage> tempMsgList;
			if (msgMap.containsKey(ip)) {
				tempMsgList = msgMap.get(ip);
			} else {
				tempMsgList = new ArrayList<ChatMessage>();
			}
			long curTime = System.currentTimeMillis();
			if (!msgTimeMap.containsKey(ip)
					|| (msgTimeMap.containsKey(ip) && (curTime
							- msgTimeMap.get(ip) > 60000L))) {
				// 获取时间
				Calendar c = Calendar.getInstance();
				StringBuilder mBuilder = new StringBuilder();
				// mBuilder.append(Integer.toString(c.get(Calendar.YEAR))+"年");
				mBuilder.append(Integer.toString(c.get(Calendar.MONTH)) + "月");
				mBuilder.append(Integer.toString(c.get(Calendar.DATE)) + "日 ");
				mBuilder.append(Integer.toString(c.get(Calendar.HOUR_OF_DAY))
						+ ":");
				int min = c.get(Calendar.MINUTE);
				if (min < 10) {
					mBuilder.append("0");
				}
				mBuilder.append(Integer.toString(min));
				msgTimeMap.put(ip, curTime);
				// 构造时间消息
				ChatMessage timeMsg = new ChatMessage(
						ChatMessage.MSG_TYPE_TIME, mBuilder.toString());
				tempMsgList.add(timeMsg);
			}

			tempMsgList.add(msg);
			msgMap.put(ip, tempMsgList);
			return tempMsgList;
		}

		public void setPauseFlag(boolean pauseFlag) {
			mainActivityPauseFlag = pauseFlag;
		}
	}

	private static final String TAG = "CommService";
	private ListenThread mListenThread;
	private Intent bcIntent;
	private VoiceComm mVoiceComm;
	private VoiceMultiComm mVoiceMultiComm;
	private VideoComm mVideoComm;
	private FileComm mFileComm;

	private boolean busyFlag; // flag for voice/video/file communication now;
	private boolean talkingFlag; // only for cmd over and talking now;
	private String dstAddr;
	private boolean needResend;
	private byte whichReq; // for busy cancel
	private String filePath;
	private String fileName;
	private long fileSize;
	private boolean fileSrc;
	private Map<String, ArrayList<ChatMessage>> msgMap;
	private Map<String, Long> msgTimeMap;
	private ArrayList<String> newMsgList;
	private boolean chatFlag;
	private String chatAddr;

	private boolean mainActivityPauseFlag;

	private MyBinder binder = new MyBinder();

	private boolean cmdSendProcess(byte cmdExtra, byte cmdTypeExtra,
			byte[] data, Intent intent) {
		byte[] tempHostAddr;
		switch (cmdExtra) {
		case Constants.MSG:

			break;
		case Constants.REQ:
			busyFlag = true;
			Log.i(TAG, "Talking start");
			dstAddr = intent.getStringExtra(Constants.EXTRA_IP);
			// 多跳网络无法从tcp连接获取源地址，因此发送请求时打包本机地址
			tempHostAddr = GetIpAddress.getLocalAddressInet().getAddress();
			System.arraycopy(tempHostAddr, 0, data, 2, 4);
			needResend = true;
			// file
			if (cmdTypeExtra == Constants.FILE_REQ) {
				fileSrc = true;
				filePath = intent.getStringExtra(Constants.EXTRA_FILEPATH);
				System.arraycopy(Type.longToBytes(intent.getLongExtra(
						Constants.EXTRA_FILESIZE, -1)), 0, data, 6, 8);
				byte[] fileName;
				try {
					fileName = intent.getStringExtra(Constants.EXTRA_FILENAME)
							.getBytes("UTF-8");
					byte fileNameLength = (byte) fileName.length;
					data[14] = fileNameLength;
					System.arraycopy(fileName, 0, data, 15, fileNameLength);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;
		case Constants.REQ_ACK:
			talkingFlag = true;
			fileSrc = false;
			switch (cmdTypeExtra) {
			case Constants.VOICE_REQ_ACK:
				if (mVoiceComm == null) {
					mVoiceComm = new VoiceComm(getApplicationContext(),
							dstAddr, true);
				}

				break;
			case Constants.VIDEO_REQ_ACK:
				if (mVoiceComm == null) {
					mVoiceComm = new VoiceComm(getApplicationContext(),
							dstAddr, false);
				}
				if (mVideoComm == null) {
					mVideoComm = VideoComm.getInstance(getApplicationContext(),
							dstAddr);
				}
				break;
			case Constants.FILE_REQ_ACK:
				if (mFileComm == null) {
					mFileComm = new FileComm(getApplicationContext(), fileName,
							fileSize);
				}
				break;
			default:
				return false;
			}
			needResend = false;
			break;
		case Constants.REQ_NAK:
			busyFlag = false;
			needResend = false;
			break;
		case Constants.END:
			tempHostAddr = GetIpAddress.getLocalAddressInet().getAddress();
			System.arraycopy(tempHostAddr, 0, data, 2, 4);
			if (mVoiceComm != null) {
				mVoiceComm.voiceEnd();
				mVoiceComm = null;
			}
			if (mVideoComm != null) {
				mVideoComm.videoEnd();
				mVideoComm = null;
			}
			if (mFileComm != null) {
				if (!fileSrc) {
					mFileComm.fileCancel();
				}
				mFileComm.fileTransEnd();
				mFileComm = null;
				// file 接收方
			}
			talkingFlag = false;
			busyFlag = false;
			needResend = true;
			break;
		case Constants.CANCEL:
			mListenThread.cmdSendOver();
			// 多跳网络无法从tcp连接获取源地址，因此发送请求时打包本机地址
			tempHostAddr = GetIpAddress.getLocalAddressInet().getAddress();
			System.arraycopy(tempHostAddr, 0, data, 2, 4);
			busyFlag = false;
			needResend = true;
			break;
		case Constants.MULTI:
			switch (cmdTypeExtra) {
			case Constants.VOICE_MULTI:
				busyFlag = true;
				if (mVoiceMultiComm == null) {
					mVoiceMultiComm = new VoiceMultiComm();
				}
				break;
			case Constants.VOICE_MULTI_QUIT:
				if (mVoiceMultiComm != null) {
					mVoiceMultiComm.voiceEnd();
					mVoiceMultiComm = null;
				}
				busyFlag = false;
				break;
			default:
				break;
			}
			Log.i(TAG, "Multi type, don't need to cmd");
			return false;
			// for file trans pause
		case Constants.PAUSE:
			if (fileSrc) {
				if (cmdTypeExtra == Constants.FILE_PAUSE) {
					mFileComm.fileSendPause(true);
				} else if (cmdTypeExtra == Constants.FILE_RESUME) {
					mFileComm.fileSendPause(false);
				}
			} else {
				if (cmdTypeExtra == Constants.FILE_PAUSE) {
					mFileComm.fileRecvPause(true);
				} else if (cmdTypeExtra == Constants.FILE_RESUME) {
					mFileComm.fileRecvPause(false);
				}
			}
			needResend = false;
			break;
		default:
			return false;
		}
		return true;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return binder;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		msgMap = new HashMap<String, ArrayList<ChatMessage>>();
		newMsgList = new ArrayList<String>();
		msgTimeMap = new HashMap<String, Long>();
		mListenThread = new ListenThread();
		bcIntent = new Intent();
		bcIntent.setAction(Constants.ACTION_BC);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mListenThread.cmdSendOver();
		mListenThread.cmdRecvOver();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if (intent == null) {
			Log.w(TAG, "intent is null,this shouldn't happen!");
			return super.onStartCommand(intent, Service.START_FLAG_REDELIVERY,
					startId);
		}
		byte cmdExtra = intent.getByteExtra(Constants.EXTRA_CMD,
				Constants.SERVICE_CREAT);
		if (cmdExtra == Constants.SERVICE_CREAT) {
			Log.i(TAG, "Service created");
		} else {
			byte cmdTypeExtra = intent.getByteExtra(Constants.EXTRA_CMD_TYPE,
					(byte) 0); // 0 is null
			whichReq = cmdTypeExtra;
			byte[] data = new byte[Constants.TCP_CMD_BUF];
			boolean succeedFlag = cmdSendProcess(cmdExtra, cmdTypeExtra, data,
					intent);
			if (succeedFlag) {
				if (dstAddr != null) {
					data[0] = cmdExtra;
					data[1] = cmdTypeExtra;
					mListenThread.cmdSend(dstAddr, data, needResend);
				} else {
					Log.e(TAG, "destAddress is null,this shouldn't happen"
							+ "+" + cmdExtra);
				}
			} else {
				Log.e(TAG, "cmdSendProcess failure or multitype");
			}
		}
		return super.onStartCommand(intent, Service.START_FLAG_REDELIVERY,
				startId);
	}

}
