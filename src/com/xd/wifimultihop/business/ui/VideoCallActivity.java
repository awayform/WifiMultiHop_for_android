package com.xd.wifimultihop.business.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.android.wifi.tether.R;
import com.xd.wifimultihop.business.app.CommService;
import com.xd.wifimultihop.business.app.Constants;
import com.xd.wifimultihop.business.comm.VideoComm;

public class VideoCallActivity extends Activity {

	private class AckListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			stateText.setVisibility(View.GONE);
			serviceIntent.putExtra(Constants.EXTRA_CMD, Constants.REQ_ACK);
			serviceIntent.putExtra(Constants.EXTRA_CMD_TYPE,
					Constants.VIDEO_REQ_ACK);
			// serviceIntent.putExtra(Constants.EXTRA_IP, dstAddr);
			startService(serviceIntent);
			ackButton.setVisibility(View.INVISIBLE);
			nakButton.setVisibility(View.INVISIBLE);
			endButton.setVisibility(View.VISIBLE);
			mSurfaceView.setVisibility(View.VISIBLE);
			mImageView.setVisibility(View.VISIBLE);
			initSurfaceView();
		}

	}

	private class CancelListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			serviceIntent.putExtra(Constants.EXTRA_CMD, Constants.CANCEL);
			serviceIntent.putExtra(Constants.EXTRA_CMD_TYPE,
					Constants.VIDEO_CANCEL);
			startService(serviceIntent);
			VideoCallActivity.this.quit();
		}

	}

	private class EndListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			serviceIntent.putExtra(Constants.EXTRA_CMD, Constants.END);
			serviceIntent.putExtra(Constants.EXTRA_CMD_TYPE,
					Constants.VIDEO_END);
			startService(serviceIntent);
			VideoCallActivity.this.quit();
		}

	}

	public class MyHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			bmpDecode = (Bitmap) msg.obj;
			if (bmpDecode != null) {
				mImageView.setImageBitmap(bmpDecode);
			}
		}

	}

	private class NakListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			serviceIntent.putExtra(Constants.EXTRA_CMD, Constants.REQ_NAK);
			serviceIntent.putExtra(Constants.EXTRA_CMD_TYPE,
					Constants.VIDEO_REQ_NAK);
			// serviceIntent.putExtra(Constants.EXTRA_IP, dstAddr);
			startService(serviceIntent);
			VideoCallActivity.this.quit();
		}

	}

	private class SurfaceListener implements SurfaceHolder.Callback {

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			// 当SurfaceView/预览界面的格式和大小发生改变时，该方法被调用
			Log.i("TAG", "SurfaceHolder.Callback：Surface Changed");
			mVideoComm.myCamera.initCamera();
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder)
		// SurfaceView启动时/初次实例化，预览界面被创建时，该方法被调用。
		{
			// TODO Auto-generated method stub
			mVideoComm.myCamera.openCamera(1, mSurfaceHolder);
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			Log.i("TAG", "SurfaceHolder.Callback：Surface Destroyed");
			mVideoComm.myCamera.closeCamera();
		}
	}

	public class TalkingReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction() == Constants.ACTION_BC) {
				byte extra = intent.getByteExtra(Constants.EXTRA_BC,
						Constants.VIDEO_REQ);
				switch (extra) {
				case Constants.VIDEO_REQ_NAK:
					// 不确定可以不
					VideoCallActivity.this.quit();
					break;
				case Constants.VIDEO_REQ_ACK:
					// mRecvThread.start();
					VideoCallActivity.this.stateText.setVisibility(View.GONE);
					VideoCallActivity.this.endButton
							.setVisibility(View.VISIBLE);
					VideoCallActivity.this.cancelButton
							.setVisibility(View.INVISIBLE);
					VideoCallActivity.this.mSurfaceView
							.setVisibility(View.VISIBLE);
					mImageView.setVisibility(View.VISIBLE);
					initSurfaceView();
					break;
				case Constants.VIDEO_END:
					VideoCallActivity.this.quit();
					break;
				default:
					break;
				}
			} /*
			 * else if(intent.getAction() == Constants.ACTION_BC_STATS){
			 * String[] extra = intent.getStringArrayExtra(Constants.EXTRA_BC);
			 * if(extra != null){ lossText.setText("丢包率："+extra[0]+"%");
			 * thruputText.setText("吞吐量："+extra[1]+"kbps");
			 * delayText.setText("时延："+extra[2]+"ms"); } }
			 */
		}
	}

	private Button ackButton;
	private Button nakButton;
	private Button endButton;
	private Button cancelButton;
	private TextView stateText;
	private ImageView mImageView;
	private boolean extraType;
	private Intent serviceIntent;
	private TalkingReceiver mReceiver;
	private String dstAddr;

	public SurfaceView mSurfaceView = null; // SurfaceView对象：(视图组件)视频显示

	public SurfaceHolder mSurfaceHolder = null; // SurfaceHolder对象：(抽象接口)SurfaceView支持类

	private Bitmap bmpDecode;

	private int mScreenWidth;

	private int mScreenHeight;

	private VideoComm mVideoComm;

	public static MyHandler mHandler;

	private void initSurfaceView() {

		mSurfaceHolder = mSurfaceView.getHolder(); // 绑定SurfaceView，取得SurfaceHolder对象
		mSurfaceHolder.addCallback(new SurfaceListener());// SurfaceHolder加入回调接口
		mSurfaceHolder.setKeepScreenOn(true);
		mSurfaceHolder.setFixedSize(mScreenWidth / 3, mScreenHeight / 4); // 预览大小設置
		// mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//
		// 設置顯示器類型，setType必须设置
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_video_call);
		stateText = (TextView) findViewById(R.id.stateText);
		ackButton = (Button) findViewById(R.id.ackButton);
		nakButton = (Button) findViewById(R.id.nakButton);
		endButton = (Button) findViewById(R.id.endButton);
		cancelButton = (Button) findViewById(R.id.cancelButton);
		mSurfaceView = (SurfaceView) findViewById(R.id.mySfView);
		mImageView = (ImageView) findViewById(R.id.myImageView);
		ackButton.setOnClickListener(new AckListener());
		nakButton.setOnClickListener(new NakListener());
		endButton.setOnClickListener(new EndListener());
		cancelButton.setOnClickListener(new CancelListener());
		// 在onCreat里判断会不会不妥
		Intent intent = getIntent();
		extraType = intent.getBooleanExtra(Constants.EXTRA_VIDEOCALL, true);
		if (extraType) {
			ackButton.setVisibility(View.INVISIBLE);
			nakButton.setVisibility(View.INVISIBLE);
			cancelButton.setVisibility(View.VISIBLE);
		}
		dstAddr = intent.getStringExtra(Constants.EXTRA_IP);
		serviceIntent = new Intent(this, CommService.class);
//		serviceIntent.setAction(Constants.ACTION_COMM);

		mReceiver = new TalkingReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.ACTION_BC);
		// filter.addAction(Constants.ACTION_BC_STATS);
		registerReceiver(mReceiver, filter);

		// delayText = (TextView)findViewById(R.id.delayText);
		// lossText = (TextView)findViewById(R.id.lossText);
		// thruputText = (TextView)findViewById(R.id.thruputText);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		mScreenWidth = dm.widthPixels;// 宽度
		mScreenHeight = dm.heightPixels;// 高度
		mVideoComm = VideoComm.getInstance(this, dstAddr);
		mHandler = new MyHandler();
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
