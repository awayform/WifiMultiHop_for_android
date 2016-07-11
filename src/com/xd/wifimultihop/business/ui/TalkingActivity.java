package com.xd.wifimultihop.business.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.googlecode.android.wifi.tether.R;
import com.xd.wifimultihop.business.app.CommService;
import com.xd.wifimultihop.business.app.Constants;

public class TalkingActivity extends Activity {

	private class AckListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			TalkingActivity.this.stateText.setText("Talking...");
			serviceIntent.putExtra(Constants.EXTRA_CMD, Constants.REQ_ACK);
			serviceIntent.putExtra(Constants.EXTRA_CMD_TYPE,
					Constants.VOICE_REQ_ACK);
			startService(serviceIntent);
			ackButton.setVisibility(View.INVISIBLE);
			nakButton.setVisibility(View.INVISIBLE);
			endButton.setVisibility(View.VISIBLE);
		}

	}

	private class CancelListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			serviceIntent.putExtra(Constants.EXTRA_CMD, Constants.CANCEL);
			serviceIntent.putExtra(Constants.EXTRA_CMD_TYPE,
					Constants.VOICE_CANCEL);
			startService(serviceIntent);
			TalkingActivity.this.quit();
		}

	}

	private class EndListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			serviceIntent.putExtra(Constants.EXTRA_CMD, Constants.END);
			serviceIntent.putExtra(Constants.EXTRA_CMD_TYPE,
					Constants.VOICE_END);
			startService(serviceIntent);
			TalkingActivity.this.quit();
		}

	}

	private class NakListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			serviceIntent.putExtra(Constants.EXTRA_CMD, Constants.REQ_NAK);
			serviceIntent.putExtra(Constants.EXTRA_CMD_TYPE,
					Constants.VOICE_REQ_NAK);
			startService(serviceIntent);
			TalkingActivity.this.quit();
		}

	}

	public class TalkingReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction() == Constants.ACTION_BC) {
				byte extra = intent.getByteExtra(Constants.EXTRA_BC,
						Constants.VOICE_REQ);
				switch (extra) {
				case Constants.VOICE_REQ_NAK:
					TalkingActivity.this.quit();
					break;
				case Constants.VOICE_REQ_ACK:
					TalkingActivity.this.stateText.setText("Talking...");
					TalkingActivity.this.endButton.setVisibility(View.VISIBLE);
					TalkingActivity.this.cancelButton
							.setVisibility(View.INVISIBLE);
					break;
				case Constants.VOICE_END:
					TalkingActivity.this.quit();
					break;
				default:
					break;
				}
			} else if (intent.getAction() == Constants.ACTION_BC_STATS) {
				String[] extra = intent.getStringArrayExtra(Constants.EXTRA_BC);
				if (extra != null) {
					lossText.setText("丢包率：" + extra[0] + "%");
					thruputText.setText("吞吐量：" + extra[1] + "kbps");
					delayText.setText("时延：" + extra[2] + "ms");
				}
			}
		}
	}

	// private static final String TAG = "TalkingActivity";
	private Button ackButton;
	private Button nakButton;
	private Button endButton;
	private Button cancelButton;
	private TextView stateText;
	private boolean extraType;

	private Intent serviceIntent;

	private TalkingReceiver mReceiver;

	private TextView delayText;

	private TextView lossText;

	private TextView thruputText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_talking);
		stateText = (TextView) findViewById(R.id.stateText);
		ackButton = (Button) findViewById(R.id.ackButton);
		nakButton = (Button) findViewById(R.id.nakButton);
		endButton = (Button) findViewById(R.id.endButton);
		cancelButton = (Button) findViewById(R.id.cancelButton);
		ackButton.setOnClickListener(new AckListener());
		nakButton.setOnClickListener(new NakListener());
		endButton.setOnClickListener(new EndListener());
		cancelButton.setOnClickListener(new CancelListener());
		// 在onCreat里判断会不会不妥
		Intent intent = getIntent();
		extraType = intent.getBooleanExtra(Constants.EXTRA_TALKING, true);
		if (extraType) {
			ackButton.setVisibility(View.INVISIBLE);
			nakButton.setVisibility(View.INVISIBLE);
			cancelButton.setVisibility(View.VISIBLE);
		}
		serviceIntent = new Intent(this, CommService.class);
//		serviceIntent.setAction(Constants.ACTION_COMM);

		mReceiver = new TalkingReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.ACTION_BC);
		filter.addAction(Constants.ACTION_BC_STATS);
		registerReceiver(mReceiver, filter);

		delayText = (TextView) findViewById(R.id.delayText);
		lossText = (TextView) findViewById(R.id.lossText);
		thruputText = (TextView) findViewById(R.id.thruputText);

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void quit() {
		finish();
		onDestroy();
	}
}
