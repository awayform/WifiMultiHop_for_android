package com.xd.wifimultihop.business.ui;

import java.text.DecimalFormat;

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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.android.wifi.tether.R;
import com.xd.wifimultihop.business.app.CommService;
import com.xd.wifimultihop.business.app.Constants;

public class FileTransferActivity extends Activity {
	private class AckListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			FileTransferActivity.this.stateText.setText("Receiving...");
			serviceIntent.putExtra(Constants.EXTRA_CMD, Constants.REQ_ACK);
			serviceIntent.putExtra(Constants.EXTRA_CMD_TYPE,
					Constants.FILE_REQ_ACK);
			startService(serviceIntent);
			ackButton.setVisibility(View.INVISIBLE);
			nakButton.setVisibility(View.INVISIBLE);
			pauseButton.setVisibility(View.VISIBLE);
			stopButton.setVisibility(View.VISIBLE);
			progessText.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.VISIBLE);
			speedText.setVisibility(View.VISIBLE);
		}

	}

	private class CancelListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			serviceIntent.putExtra(Constants.EXTRA_CMD, Constants.CANCEL);
			serviceIntent.putExtra(Constants.EXTRA_CMD_TYPE,
					Constants.FILE_CANCEL);
			startService(serviceIntent);
			FileTransferActivity.this.quit();
		}

	}

	public class FileTransferReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction() == Constants.ACTION_BC) {
				byte extra = intent.getByteExtra(Constants.EXTRA_BC,
						Constants.FILE_REQ);
				switch (extra) {
				case Constants.FILE_REQ_NAK:
					FileTransferActivity.this.quit();
					break;
				case Constants.FILE_REQ_ACK:
					stateText.setText("Sending...");
					pauseButton.setVisibility(View.VISIBLE);
					stopButton.setVisibility(View.VISIBLE);
					progessText.setVisibility(View.VISIBLE);
					progressBar.setVisibility(View.VISIBLE);
					speedText.setVisibility(View.VISIBLE);
					FileTransferActivity.this.cancelButton
							.setVisibility(View.INVISIBLE);
					break;
				case Constants.FILE_END:
					FileTransferActivity.this.quit();
					break;
				case Constants.FILE_PAUSE:
					stateText.append("Paused");
					pauseButton.setClickable(false);
					break;
				case Constants.FILE_RESUME:
					if (fileSrc) {
						stateText.setText("Sending...");
					} else {
						stateText.setText("Receiving...");
					}
					pauseButton.setClickable(true);
					break;
				default:
					break;
				}
			} else if (intent.getAction() == Constants.ACTION_BC_STATS) {
				int progress = intent.getIntExtra(Constants.EXTRA_FILEPROGESS,
						0);
				float speed = intent
						.getFloatExtra(Constants.EXTRA_FILESPEED, 0);

				if (intent.getBooleanExtra(Constants.EXTRA_FILEDONE, false)) {
					stateText.append("Finish");
					pauseButton.setClickable(false);
					stopButton.setText("退出");
					Toast.makeText(FileTransferActivity.this, "文件传输完成",
							Toast.LENGTH_SHORT).show();
					if (speed >= 1024) {
						speedText.setText("平均速度：" + df.format(speed / 1024)
								+ "MB/s");
					} else {
						speedText.setText("平均速度：" + df.format(speed) + "KB/s");
					}
				} else {
					if (speed >= 1024) {
						speedText.setText("速度：" + df.format(speed / 1024)
								+ "MB/s");
					} else {
						speedText.setText("速度：" + df.format(speed) + "KB/s");
					}
				}
				progressBar.setProgress(progress);
			}
		}
	}

	private class NakListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			serviceIntent.putExtra(Constants.EXTRA_CMD, Constants.REQ_NAK);
			serviceIntent.putExtra(Constants.EXTRA_CMD_TYPE,
					Constants.FILE_REQ_NAK);
			startService(serviceIntent);
			FileTransferActivity.this.quit();
		}

	}

	private class PauseListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			serviceIntent.putExtra(Constants.EXTRA_CMD, Constants.PAUSE);

			if (!pauseFlag) {
				pauseFlag = true;
				pauseButton.setText("继续");
				stateText.append("Paused");
				serviceIntent.putExtra(Constants.EXTRA_CMD_TYPE,
						Constants.FILE_PAUSE);
			} else {
				pauseFlag = false;
				pauseButton.setText("暂停");
				if (fileSrc) {
					stateText.setText("Sending...");
				} else {
					stateText.setText("Receiving...");
				}
				serviceIntent.putExtra(Constants.EXTRA_CMD_TYPE,
						Constants.FILE_RESUME);
			}

			startService(serviceIntent);
		}

	}

	private class StopListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			serviceIntent.putExtra(Constants.EXTRA_CMD, Constants.END);
			serviceIntent
					.putExtra(Constants.EXTRA_CMD_TYPE, Constants.FILE_END);
			startService(serviceIntent);
			FileTransferActivity.this.quit();
		}

	}

	// private static final String TAG = "FileTransferActivity";
	private Button ackButton;
	private Button nakButton;
	private Button cancelButton;
	private Button pauseButton;
	private Button stopButton;
	private TextView stateText;
	private TextView fileText;
	private TextView sizeText;
	private TextView progessText;
	private TextView speedText;
	private ProgressBar progressBar;

	private boolean extraType;

	private Intent serviceIntent;

	private FileTransferReceiver mReceiver;

	private DecimalFormat df;

	private boolean pauseFlag;

	private boolean fileSrc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_transfer);
		stateText = (TextView) findViewById(R.id.stateText);
		fileText = (TextView) findViewById(R.id.fileText);
		sizeText = (TextView) findViewById(R.id.sizeText);
		progessText = (TextView) findViewById(R.id.progessText);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		speedText = (TextView) findViewById(R.id.speedText);
		ackButton = (Button) findViewById(R.id.ackButton);
		nakButton = (Button) findViewById(R.id.nakButton);
		pauseButton = (Button) findViewById(R.id.buttonPause);
		stopButton = (Button) findViewById(R.id.buttonStop);
		cancelButton = (Button) findViewById(R.id.cancelButton);
		ackButton.setOnClickListener(new AckListener());
		nakButton.setOnClickListener(new NakListener());
		pauseButton.setOnClickListener(new PauseListener());
		stopButton.setOnClickListener(new StopListener());
		cancelButton.setOnClickListener(new CancelListener());
		// 在onCreat里判断会不会不妥
		Intent intent = getIntent();
		extraType = intent.getBooleanExtra(Constants.EXTRA_FILETRANSFER, true);
		if (extraType) {
			ackButton.setVisibility(View.INVISIBLE);
			nakButton.setVisibility(View.INVISIBLE);
			cancelButton.setVisibility(View.VISIBLE);
		} else {
			// dstAddr = intent.getStringExtra(Constants.EXTRA_IP);
		}
		fileSrc = extraType;
		fileText.setText("文件："
				+ intent.getStringExtra(Constants.EXTRA_FILENAME));
		long size = intent.getLongExtra(Constants.EXTRA_FILESIZE, -1);
		df = new DecimalFormat(".00");
		// 1048576,1024
		if (size >= 1000000L) {
			sizeText.setText("大小：" + df.format(size / 1000000.0) + "MB");
		} else if (size >= 1000L) {
			sizeText.setText("大小：" + df.format(size / 1000.0) + "KB");
		} else {
			sizeText.setText("大小：" + size + "B");
		}
		serviceIntent = new Intent(this, CommService.class);
//		serviceIntent.setAction(Constants.ACTION_COMM);

		mReceiver = new FileTransferReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.ACTION_BC);
		filter.addAction(Constants.ACTION_BC_STATS);
		registerReceiver(mReceiver, filter);

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
