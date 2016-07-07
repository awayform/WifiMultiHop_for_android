package com.xd.wifimultihop.business.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ServiceBroadcastReceiver extends BroadcastReceiver {

	public ServiceBroadcastReceiver() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		if (action.equals(Constants.ACTION_BC_SERVICE)) {
			switch (intent.getByteExtra(Constants.EXTRA_CMD, (byte) -1)) {
			case Constants.BUSY:
				Toast.makeText(context, "对方正忙，请求被拒绝", Toast.LENGTH_SHORT)
						.show();
				break;
			case Constants.FILE_REQ:
				Toast.makeText(context, "收到文件传输请求，但是没有可用的SD卡或手机存储，请插入后重试！",
						Toast.LENGTH_LONG).show();
				break;
			case Constants.REQ_NAK:
				Toast.makeText(context, "请求被拒绝", Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}
	}

}
