package com.xd.wifimultihop.business.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.googlecode.android.wifi.tether.R;
import com.xd.wifimultihop.business.app.CommService;
import com.xd.wifimultihop.business.app.Constants;

public class MultitalkingActivity extends Activity {

	private Button quitButton;
	private Intent serviceIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_multitalking);
		serviceIntent = new Intent(this, CommService.class);
//		serviceIntent.setAction(Constants.ACTION_COMM);
		quitButton = (Button) findViewById(R.id.quitButton);
		quitButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				serviceIntent.putExtra(Constants.EXTRA_CMD, Constants.MULTI);
				serviceIntent.putExtra(Constants.EXTRA_CMD_TYPE,
						Constants.VOICE_MULTI_QUIT);
				startService(serviceIntent);
				MultitalkingActivity.this.quit();
			}
		});
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

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
