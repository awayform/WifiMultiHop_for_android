package com.xd.wifimultihop.business.comm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.xd.wifimultihop.business.app.Constants;
import com.xd.wifimultihop.business.socket.TcpSocket;
import com.xd.wifimultihop.business.socket.TcpSocket.Buffer;

public class FileComm {

	private class RecvThread extends Thread {

		public boolean pauseFlag = false;
		public boolean runFlag = true;
		public long sumPauseTime = 0;
		public long curPauseTime = 0;
		int needRecvTimes;
		int sum = 0;
		long startTime;
		int progress;
		float speed;
		long lastTime;
		int eachSize;
		long gotSize;

		public RecvThread() {
			super();
			needRecvTimes = (int) (recvFileSize / Constants.TCP_FILE_BUF) + 1;
		}

		private void free() {
			try {
				recvFileStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			mTcpSocket.socketConnect();
			Buffer recvBuffer;
			Log.i(TAG, "recvfilesize:" + recvFileSize);
			while (runFlag) {
				if (recvFileSize == 0) {
					Log.i(TAG, "~~~~~~~~~~~~~RECV OVER~~~~~~~~~~~");
					progress = 100;
					speed = 0;
					bcIntent.putExtra(Constants.EXTRA_FILEDONE, true);
					bcIntent.putExtra(Constants.EXTRA_FILEPROGESS, progress);
					bcIntent.putExtra(Constants.EXTRA_FILESPEED, speed);
					context.sendBroadcast(bcIntent);
					runFlag = false;
					free();
					break;
				}
				if ((recvBuffer = mTcpSocket.tcpRecv()) != null) {
					try {
						if (sum == 0) {
							lastTime = System.currentTimeMillis();
							startTime = lastTime;
						}
						eachSize = recvBuffer.getSize();
						recvFileStream.write(recvBuffer.getBuf(), 0, eachSize);
						gotSize = gotSize + eachSize;
						sum++;
						if ((sum % 1000) == 0) {
							progress = 100 * sum / needRecvTimes;
							speed = (float) (1000 * Constants.TCP_FILE_BUF)
									/ (System.currentTimeMillis() - lastTime - curPauseTime);
							curPauseTime = 0;
							// Log.i(TAG, "speed"+speed);
							lastTime = System.currentTimeMillis();
							if (!pauseFlag) {
								bcIntent.putExtra(Constants.EXTRA_FILEPROGESS,
										progress);
								bcIntent.putExtra(Constants.EXTRA_FILESPEED,
										speed);
								context.sendBroadcast(bcIntent);
							}
						}
						// Log.i(TAG, "sum:"+sum);
						if (gotSize == recvFileSize) {
							// Log.i(TAG, "needRecvTimes:"+needRecvTimes);
							Log.i(TAG, "~~~~~~~~~~~~~RECV OVER~~~~~~~~~~~");
							progress = 100;
							speed = (float) (recvFileSize)
									/ (float) (System.currentTimeMillis()
											- startTime - sumPauseTime);
							bcIntent.putExtra(Constants.EXTRA_FILEDONE, true);
							bcIntent.putExtra(Constants.EXTRA_FILEPROGESS,
									progress);
							bcIntent.putExtra(Constants.EXTRA_FILESPEED, speed);
							context.sendBroadcast(bcIntent);
							runFlag = false;
							free();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						free();
					}
				} else {
					runFlag = false;
					free();
				}
			}
		}
	}

	private class SendThread extends Thread {

		public long curPauseTime = 0;
		public long sumPauseTime = 0;
		private byte[] sendBuf;
		public boolean runFlag = true;
		public boolean pauseFlag = false;
		int needSendTimes;
		int sum = 0;
		long startTime;
		int progress;
		float speed;
		long lastTime;

		public SendThread() {
			super();
			this.sendBuf = new byte[Constants.TCP_FILE_BUF];
			needSendTimes = (int) (getFileSize() / Constants.TCP_FILE_BUF) + 1;
		}

		private void free() {
			try {
				sendFileStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			int size;

			try {
				while (runFlag) {
					if (pauseFlag) {
						synchronized (mSendThread) {
							try {
								mSendThread.wait();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					if ((size = sendFileStream.read(sendBuf, 0, sendBuf.length)) != -1) {
						mTcpSocket.tcpSend(sendBuf, size);
						if (sum == 0) {
							lastTime = System.currentTimeMillis();
							startTime = lastTime;
						}
						sum++;
						if ((sum % 1000) == 0) {
							progress = 100 * sum / needSendTimes;
							speed = (float) (1000 * Constants.TCP_FILE_BUF)
									/ (float) (System.currentTimeMillis()
											- lastTime - curPauseTime);
							curPauseTime = 0;
							Log.i(TAG, "" + speed);
							lastTime = System.currentTimeMillis();
							if (!pauseFlag) {
								bcIntent.putExtra(Constants.EXTRA_FILEPROGESS,
										progress);
								bcIntent.putExtra(Constants.EXTRA_FILESPEED,
										speed);
								context.sendBroadcast(bcIntent);
							}
						}
					} else {
						progress = 100;
						speed = (float) (getFileSize())
								/ (float) (System.currentTimeMillis()
										- startTime - sumPauseTime);
						bcIntent.putExtra(Constants.EXTRA_FILEDONE, true);
						bcIntent.putExtra(Constants.EXTRA_FILEPROGESS, progress);
						bcIntent.putExtra(Constants.EXTRA_FILESPEED, speed);
						context.sendBroadcast(bcIntent);
						runFlag = false;
						free();
					}

				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				free();
			}
		}
	}

	private static final String TAG = "FileComm";

	public static boolean hasSdCard() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	private File root;
	private File sendFile;
	private FileInputStream sendFileStream;
	private String recvFileName;
	private long recvFileSize;
	private FileOutputStream recvFileStream;
	private TcpSocket mTcpSocket;
	private RecvThread mRecvThread;
	private SendThread mSendThread;

	private Context context;

	private long tempTime; // for PauseTime calculate

	private Intent bcIntent = new Intent(Constants.ACTION_BC_STATS);

	// for recv
	public FileComm(Context context, String fileName, long fileSize) {
		this.context = context;
		this.recvFileName = fileName;
		this.recvFileSize = fileSize;
		mTcpSocket = new TcpSocket(Constants.TCP_FILE_TRANS_PORT,
				Constants.TCP_FILE_BUF);
		root = Environment.getExternalStorageDirectory();
		try {
			File dir = createDir(root.getCanonicalPath() + "/HwnsClient");
			recvFileStream = new FileOutputStream(dir.toString() + "/"
					+ fileName, false);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mRecvThread = new RecvThread();
		mRecvThread.start();
	}

	// for send
	public FileComm(Context context, String filePath, String dstAddr) {
		// TODO Auto-generated constructor stub
		this.context = context;
		sendFile = new File(filePath);
		try {
			sendFileStream = new FileInputStream(sendFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mTcpSocket = new TcpSocket(Constants.TCP_FILE_TRANS_PORT, dstAddr);
		mSendThread = new SendThread();
		mSendThread.start();
	}

	private File createDir(String dirpath) {

		File dir = new File(dirpath);
		if (!dir.exists()) {
			dir.mkdir();
		}
		return dir;
	}

	public void fileCancel() {
		try {
			File recvFile = new File(root.getCanonicalPath() + "/HwnsClient",
					recvFileName);
			if (recvFile != null) {
				if (recvFile.length() < recvFileSize) {
					recvFile.delete();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void fileRecvPause(boolean pause) {

		if (pause) {
			mRecvThread.pauseFlag = true;
			tempTime = System.currentTimeMillis();
		} else {
			mRecvThread.pauseFlag = false;
			mRecvThread.curPauseTime = System.currentTimeMillis() - tempTime;
			mRecvThread.sumPauseTime = mRecvThread.sumPauseTime
					+ mRecvThread.curPauseTime;
		}

	}

	public void fileSendPause(boolean pause) {
		if (pause) {
			mSendThread.pauseFlag = true;
			tempTime = System.currentTimeMillis();
		} else {
			mSendThread.pauseFlag = false;
			mSendThread.curPauseTime = System.currentTimeMillis() - tempTime;
			mSendThread.sumPauseTime = mSendThread.sumPauseTime
					+ mSendThread.curPauseTime;
			synchronized (mSendThread) {
				mSendThread.notify();
			}
		}
	}

	public void fileTransEnd() {
		if (mRecvThread != null) {
			mRecvThread.runFlag = false;
			mRecvThread.free();
		}
		if (mSendThread != null) {
			mSendThread.runFlag = false;
			mSendThread.free();
		}
		mTcpSocket.tcpEnd();
	}

	public long getFileSize() {
		if (sendFile != null)
			return sendFile.length();
		else
			return -1;
	}

}
