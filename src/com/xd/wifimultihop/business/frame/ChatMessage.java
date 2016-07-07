package com.xd.wifimultihop.business.frame;

public class ChatMessage {
	// 定义3种布局类型
	public static final int MSG_TYPE_TIME = 0;
	public static final int MSG_TYPE_FROM = 1;
	public static final int MSG_TYPE_TO = 2;

	// 消息类型
	private int mType;

	// 消息内容
	private String mContent;

	public ChatMessage(int Type, String Content) {
		this.mType = Type;
		this.mContent = Content;
	}

	// 获取内容
	public String getContent() {
		return mContent;
	}

	// 获取类型
	public int getType() {
		return mType;
	}

	// 设置内容
	public void setContent(String mContent) {
		this.mContent = mContent;
	}

	// 设置类型
	public void setType(int mType) {
		this.mType = mType;
	}
}