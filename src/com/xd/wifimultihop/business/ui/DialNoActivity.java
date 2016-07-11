package com.xd.wifimultihop.business.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.android.wifi.tether.R;
import com.xd.wifimultihop.business.app.CommService;
import com.xd.wifimultihop.business.app.Constants;
import com.xd.wifimultihop.business.socket.GetIpAddress;

public class DialNoActivity extends Activity {

	private class DialButtonListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			dstAddr = noText.getText().toString();
			if (dstAddr.isEmpty() || !GetIpAddress.isIpAddress(dstAddr)) {
				Toast.makeText(DialNoActivity.this, "IP地址填写错误",
						Toast.LENGTH_SHORT).show();
				return;
			}
			intent.putExtra(Constants.EXTRA_IP, dstAddr);
			startActivity(intent);
			if (!typeText.getText().toString().equals("聊天")) {
				serviceIntent.putExtra(Constants.EXTRA_CMD, Constants.REQ);
				serviceIntent.putExtra(Constants.EXTRA_IP, dstAddr);
				startService(serviceIntent);
			}
		}

	}

	private static final String TAG = "DialNoActivity";
	private TextView typeText;
	private EditText noText;
	private Button dialButton;
	private Intent serviceIntent;
	private Intent intent;

	private String dstAddr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dial_no);

		typeText = (TextView) findViewById(R.id.typeText);
		noText = (EditText) findViewById(R.id.noText);
		dialButton = (Button) findViewById(R.id.dialButton);
		noText.setSelection(noText.getText().length());
		dialButton.setOnClickListener(new DialButtonListener());

		serviceIntent = new Intent(this, CommService.class);
//		serviceIntent.setAction(Constants.ACTION_COMM);

		Intent getIntent = getIntent();
		Byte businessType = getIntent.getByteExtra(Constants.EXTRA_TYPE,
				(byte) 0);
		switch (businessType) {
		case Constants.VOICE_REQ:
			typeText.setText("语音拨号");
			intent = new Intent(DialNoActivity.this, TalkingActivity.class);
			intent.putExtra(Constants.EXTRA_TALKING, true); // true:主动打开
			serviceIntent.putExtra(Constants.EXTRA_CMD_TYPE,
					Constants.VOICE_REQ);
			break;
		case Constants.VIDEO_REQ:
			typeText.setText("视频拨号");
			intent = new Intent(DialNoActivity.this, VideoCallActivity.class);
			intent.putExtra(Constants.EXTRA_VIDEOCALL, true); // true:主动打开
			serviceIntent.putExtra(Constants.EXTRA_CMD_TYPE,
					Constants.VIDEO_REQ);
			break;
		case Constants.FILE_REQ:
			String filePath = getIntent
					.getStringExtra(Constants.EXTRA_FILEPATH);
			String fileName = getIntent
					.getStringExtra(Constants.EXTRA_FILENAME);
			long fileSize = getIntent
					.getLongExtra(Constants.EXTRA_FILESIZE, -1);
			typeText.setText("文件传输");
			intent = new Intent(DialNoActivity.this, FileTransferActivity.class);
			intent.putExtra(Constants.EXTRA_FILETRANSFER, true); // true:主动打开
			intent.putExtra(Constants.EXTRA_FILENAME, fileName);
			intent.putExtra(Constants.EXTRA_FILESIZE, fileSize);
			serviceIntent
					.putExtra(Constants.EXTRA_CMD_TYPE, Constants.FILE_REQ);
			serviceIntent.putExtra(Constants.EXTRA_FILEPATH, filePath);
			serviceIntent.putExtra(Constants.EXTRA_FILENAME, fileName);
			serviceIntent.putExtra(Constants.EXTRA_FILESIZE, fileSize);
			break;
		case Constants.MSG_REQ:
			typeText.setText("聊天");
			dialButton.setText("打开");
			intent = new Intent(DialNoActivity.this, ChatActivity.class);
			break;
		default:
			Log.w(TAG, "businessType is null,this shouldn't happen!");
			break;
		}

	}
}
