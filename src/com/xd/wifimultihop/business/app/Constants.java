package com.xd.wifimultihop.business.app;

public class Constants {
	public static final int TCP_CMD_BUF = 1024;
	public static final int TCP_FILE_BUF = 1024;
	public static final int VOICE_RAW_BUF = 160;
	public static final int UDP_VOICE_BUF = 38;
	public static final int UDP_VOICE_PACKET = 392;
	public static final int UDP_VOICE_DELAY_PACKET = 9;
	public static final int UDP_VOICE_SOCKET_BUF = 392;
	public static final int UDP_MULTI_VOICE_PACKET = 388;
	public static final int UDP_VIDEO_PACKET = 10240;
	public static final int TCP_LISTEN_PORT = 8800;
	public static final int UDP_VOICE_PORT = 8811;
	public static final int UDP_VOICE_DELAY_PORT = 8812;
	public static final int UDP_VOICE_MULTI_PORT = 8822;
	public static final int UDP_VIDEO_PORT = 8833;
	public static final int TCP_FILE_TRANS_PORT = 8844;

	public static final int QUEUE_LEN = 100;

	public static final int MSG_REQ_CODE = 1;
	public static final int MSG_RESULT_CODE = 2;

	public static final String VOICE_MULTICAST_ADDRESS = "224.0.0.4";

	public static final String EXTRA_BC = "edu.xd.net.Bc";
	public static final String EXTRA_IP = "edu.xd.net.Ip";
	public static final String EXTRA_TYPE = "edu.xd.net.BusinessType";
	public static final String EXTRA_TALKING = "edu.xd.net.Talking";
	public static final String EXTRA_VIDEOCALL = "edu.xd.net.VideoCall";
	public static final String EXTRA_FILETRANSFER = "edu.xd.net.FileTransfer";
	public static final String EXTRA_FILEPATH = "edu.xd.net.FilePath";
	public static final String EXTRA_FILENAME = "edu.xd.net.FileName";
	public static final String EXTRA_FILESIZE = "edu.xd.net.FileSize";
	public static final String EXTRA_FILEPROGESS = "edu.xd.net.FileProgess";
	public static final String EXTRA_FILESPEED = "edu.xd.net.FileSpeed";
	public static final String EXTRA_FILEDONE = "edu.xd.net.FileDone";
	public static final String EXTRA_MSG = "edu.xd.net.Message";
	public static final String ACTION_COMM = "edu.xd.net.COMM_SERVICE";
	public static final String ACTION_BC = "edu.xd.net.RECEIVER";
	public static final String ACTION_BC_STATS = "edu.xd.net.RECEIVER_STATS";
	public static final String ACTION_BC_SERVICE = "edu.xd.net.SERVICE_BROADCAST_RECEIVER";

	public static final String EXTRA_CMD = "edu.xd.net.Cmd";
	public static final byte BUSY = 100;
	public static final byte SERVICE_CREAT = 101;
	public static final byte REQ = 102;
	public static final byte REQ_ACK = 103;
	public static final byte REQ_NAK = 104;
	public static final byte END = 105;
	public static final byte END_ACK = 106;
	public static final byte CANCEL = 107;
	public static final byte CANCEL_ACK = 108;
	public static final byte MULTI = 109;
	public static final byte PAUSE = 110;
	public static final byte MSG = 111;

	public static final String EXTRA_CMD_TYPE = "edu.xd.net.CmdType";
	public static final byte VOICE_REQ = 1;
	public static final byte VOICE_REQ_ACK = 2;
	public static final byte VOICE_REQ_NAK = 3;
	public static final byte VOICE_END = 4;
	public static final byte VOICE_END_ACK = 5;
	public static final byte VOICE_CANCEL = 6;
	public static final byte VOICE_MULTI = 7;
	public static final byte VOICE_MULTI_QUIT = 8;
	public static final byte VIDEO_REQ = 11;
	public static final byte VIDEO_REQ_ACK = 12;
	public static final byte VIDEO_REQ_NAK = 13;
	public static final byte VIDEO_END = 14;
	public static final byte VIDEO_END_ACK = 15;
	public static final byte VIDEO_CANCEL = 16;
	public static final byte FILE_REQ = 17;
	public static final byte FILE_REQ_ACK = 18;
	public static final byte FILE_REQ_NAK = 19;
	public static final byte FILE_END = 20;
	public static final byte FILE_END_ACK = 21;
	public static final byte FILE_CANCEL = 22;
	public static final byte FILE_PAUSE = 23;
	public static final byte FILE_RESUME = 24;
	public static final byte MSG_REQ = 25;
}
