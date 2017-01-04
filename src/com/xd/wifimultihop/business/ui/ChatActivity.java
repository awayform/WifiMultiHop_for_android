package com.xd.wifimultihop.business.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.googlecode.android.wifi.tether.R;
import com.xd.wifimultihop.business.app.CommService;
import com.xd.wifimultihop.business.app.Constants;
import com.xd.wifimultihop.business.frame.ChatMessage;

public class ChatActivity extends Activity {

	private class ChatAdapter extends BaseAdapter {

		private Context mContext;
		private List<ChatMessage> mData;

		public ChatAdapter(Context context, List<ChatMessage> data) {
			this.mContext = context;
			this.mData = data;
		}

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public Object getItem(int Index) {
			return mData.get(Index);
		}

		@Override
		public long getItemId(int Index) {
			return Index;
		}

		@Override
		public View getView(int Index, View mView, ViewGroup mParent) {
			TextView Content;
			switch (mData.get(Index).getType()) {
			case ChatMessage.MSG_TYPE_TIME:
				mView = LayoutInflater.from(mContext).inflate(
						R.layout.msg_time, null);
				Content = (TextView) mView.findViewById(R.id.timeText);
				Content.setText(mData.get(Index).getContent());
				break;
			case ChatMessage.MSG_TYPE_FROM:
				mView = LayoutInflater.from(mContext).inflate(
						R.layout.msg_recv, null);
				Content = (TextView) mView.findViewById(R.id.recvText);
				Content.setText(mData.get(Index).getContent());
				break;
			case ChatMessage.MSG_TYPE_TO:
				mView = LayoutInflater.from(mContext).inflate(
						R.layout.msg_send, null);
				Content = (TextView) mView.findViewById(R.id.sendText);
				Content.setText(mData.get(Index).getContent());
				break;
			}
			return mView;
		}

	}

	private class ChatReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(Constants.ACTION_BC)) {
				if (intent.getByteExtra(Constants.EXTRA_BC, (byte) -1) == Constants.MSG) {
					ArrayList<ChatMessage> list = getBinder.getMsgList(dstAddr);
					updateList(list);
					mAdapter.notifyDataSetChanged();
					// 滚动列表到当前消息
					chatListView.smoothScrollToPosition(chatListView
							.getBottom());
				}
			}
		}

	}

	private static final String TAG = "ChatActivity";
	private String dstAddr;
	private TextView friendIpText;
	private EditText chatText;
	private Button sendButton;
	private ListView chatListView;
	private ChatAdapter mAdapter;
	private ArrayList<ChatMessage> msgList;
	private ChatReceiver mReceiver;

	private CommService.MyBinder getBinder;

	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			Log.i(TAG, "Service connected");

			getBinder = (CommService.MyBinder) service;
			getBinder.setChatAddr(dstAddr);
			ArrayList<ChatMessage> list = getBinder.getMsgList(dstAddr);
			updateList(list);
			mAdapter.notifyDataSetChanged();
			chatListView.setSelection(mAdapter.getCount() - 1);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			Log.i(TAG, "Service disconnected");

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_chat);
		friendIpText = (TextView) findViewById(R.id.friendIpText);
		chatText = (EditText) findViewById(R.id.chatText);
		sendButton = (Button) findViewById(R.id.sendButton);
		chatListView = (ListView) findViewById(R.id.chatList);
		msgList = new ArrayList<ChatMessage>();
		mAdapter = new ChatAdapter(this, msgList);
		chatListView.setAdapter(mAdapter);

		Intent getIntent = getIntent();
		dstAddr = getIntent.getStringExtra(Constants.EXTRA_IP);
		friendIpText.setText(dstAddr);
		// extraType = getIntent.getBooleanExtra(Constants.EXTRA_MSG, false);

		sendButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (chatText.getText().toString() != "") {
					// 构造输入消息
					String inputMsg = chatText.getText().toString();
					ChatMessage sendMsg = new ChatMessage(
							ChatMessage.MSG_TYPE_TO, inputMsg);

					// 更新数据
					ArrayList<ChatMessage> list = getBinder.setMsgMap(dstAddr,
							sendMsg);
					updateList(list);
					mAdapter.notifyDataSetChanged();
					getBinder.sendMsg(dstAddr, inputMsg);
				}
				// 清空输入框
				chatText.setText("");

				// 滚动列表到当前消息
				chatListView.smoothScrollToPosition(chatListView.getBottom());
			}
		});
		mReceiver = new ChatReceiver();
		this.registerReceiver(mReceiver, new IntentFilter(Constants.ACTION_BC));
//		Intent serviceIntent = new Intent(Constants.ACTION_COMM);
		Intent serviceIntent = new Intent(this, CommService.class);
		bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);
		Log.i(TAG, "ChatActivity bind CommService");
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		getBinder.setChatAddr(null);
		unbindService(conn);
		// 解决receiver not registered错误
		try {
			unregisterReceiver(mReceiver);
		} catch (IllegalArgumentException e) {
			if (e.getMessage().contains("Receiver not registered")) {
				// Ignore this exception. This is exactly what is desired
			} else {
				// unexpected, re-throw
				throw e;
			}
		}
	}

	private void updateList(ArrayList<ChatMessage> list) {
		if (list != null) {
			msgList.clear();
			msgList.addAll(list);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {  
	    case android.R.id.home:
	    	onBackPressed();
	        return true;
	    }
		return super.onOptionsItemSelected(item);
	}
}
