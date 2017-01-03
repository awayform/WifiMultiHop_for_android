package com.xd.wifimultihop.ui;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.android.wifi.tether.R;
import com.xd.wifimultihop.business.app.CommService;
import com.xd.wifimultihop.business.app.Constants;
import com.xd.wifimultihop.business.socket.GetIpAddress;
import com.xd.wifimultihop.business.ui.ChatActivity;
import com.xd.wifimultihop.business.ui.DialNoActivity;
import com.xd.wifimultihop.business.ui.ExploreActivity;
import com.xd.wifimultihop.business.ui.FileChooseActivity;
import com.xd.wifimultihop.business.ui.MultitalkingActivity;

public class BusinessFragment extends Fragment {

	private static final String TAG = "BusinessFragment";
	private Integer[] mThumbIds = { R.drawable.icon_business_telephone, R.drawable.icon_business_voip,
			R.drawable.icon_business_message, R.drawable.icon_business_multi_telephone, 
			R.drawable.icon_business_file, R.drawable.icon_business_browser};

	private String[] mLable = { "语音", "视频", "文本通信", "群组通信", "文件传输", "网页浏览" };

	private View view;
	private GridView gridview;
	private ArrayList<AppsItemInfo> list;
	private Intent serviceIntent;
	private MainReceiver mReceiver;
	private AlertDialog flagDialog;
	private CommService.MyBinder getBinder;
	private boolean dialogShow;
	
	
	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			Log.i(TAG, "Service connected");
			getBinder = (CommService.MyBinder) service;
			getBinder.setPauseFlag(false);
			if (!dialogShow) {
				String getIp = getBinder.getNewMsg();
				if (getIp != null) {
					openMsgDialog(getIp);
				}
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.i(TAG, "Service disconnected");
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_business, null);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mReceiver = new MainReceiver();
		gridview = (GridView) view.findViewById(R.id.grid_view);
		setItems();
		gridview.setAdapter(new ImageAdapter());
		gridview.setOnItemClickListener(new ClickListener());
		startConnService();
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivity().registerReceiver(mReceiver, new IntentFilter(Constants.ACTION_BC));
		getActivity().bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);
		Log.i(TAG, "BusinessFragment bind CommService");
	}
	
	private void startConnService() {
		serviceIntent = new Intent(getActivity(), CommService.class);
//		serviceIntent.setAction(Constants.ACTION_COMM);
		serviceIntent.putExtra(Constants.EXTRA_CMD, Constants.SERVICE_CREAT);
		getActivity().startService(serviceIntent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Application里没有关闭Application的回调方法,只能在这里关？
		getActivity().stopService(serviceIntent);
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		getBinder.setPauseFlag(true);
		getActivity().unbindService(conn);
		// 解决receiver not registered错误
		try {
			getActivity().unregisterReceiver(mReceiver);
		} catch (IllegalArgumentException e) {
			if (e.getMessage().contains("Receiver not registered")) {
				// Ignore this exception. This is exactly what is desired
			} else {
				// unexpected, re-throw
				throw e;
			}
		}
	}
	
	private void setItems() {
		list = new ArrayList<AppsItemInfo>();
		for (int i = 0; i < mThumbIds.length; i++) {
			AppsItemInfo businessItem = new AppsItemInfo();
			// 设置图片
			ImageView vi = new ImageView(getActivity());
			vi.setImageResource(mThumbIds[i]);
			businessItem.setIcon(vi.getDrawable());
			// 设置标签
			businessItem.setLabel(mLable[i]);

			list.add(businessItem);
		}
	}

	private class AppsItemInfo {

		private Drawable icon; // 存放图片
		private String label; // 存放标签

		public Drawable getIcon() {
			return icon;
		}

		public void setIcon(Drawable icon) {
			this.icon = icon;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

	}

	public class ImageAdapter extends BaseAdapter {
		LayoutInflater inflater = LayoutInflater.from(BusinessFragment.this
				.getActivity());

		public int getCount() {
			return list.size();
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder;
			if (convertView == null) {
				// 使用View的对象itemView与R.layout.apps关联
				convertView = inflater.inflate(R.layout.item_business, null);
				holder = new ViewHolder();
				holder.icon = (ImageView) convertView
						.findViewById(R.id.apps_image);
				holder.label = (TextView) convertView
						.findViewById(R.id.apps_textview);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.icon.setImageDrawable(list.get(position).getIcon());
			holder.label.setText(list.get(position).getLabel().toString());

			return convertView;
		}

	}

	static class ViewHolder {
		TextView label;
		ImageView icon;
	}

	private class MainReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(Constants.ACTION_BC)) {
				if (intent.getByteExtra(Constants.EXTRA_BC, (byte) -1) == Constants.MSG) {
					openMsgDialog(intent.getStringExtra(Constants.EXTRA_IP));
				}
			}
		}
	}
	
	private void openMsgDialog(final String address) {
		dialogShow = true;
		flagDialog = new AlertDialog.Builder(getActivity()).create();
		flagDialog.setCancelable(false);
		flagDialog.show();
		Window window = flagDialog.getWindow();
		window.setContentView(R.layout.dialog_layout);
		TextView srcText = (TextView) window.findViewById(R.id.srcText);
		srcText.append(address);
		ImageButton dialogButton = (ImageButton) window
				.findViewById(R.id.dialogButton);
		dialogButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(BusinessFragment.this.getActivity(),
						ChatActivity.class);
				intent.putExtra(Constants.EXTRA_IP, address);
				startActivity(intent);
				flagDialog.cancel();
				dialogShow = false;
			}
		});
		ImageButton closeButton = (ImageButton) window
				.findViewById(R.id.closeButton);
		closeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				flagDialog.cancel();
				dialogShow = false;
				String getIp = getBinder.getNewMsg();
				if (getIp != null) {
					openMsgDialog(getIp);
				}
			}
		});
	}
	
	private class ClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			if(GetIpAddress.getLocalAddressInet() == null) {
				Toast.makeText(BusinessFragment.this.getActivity(), "尚未加入到网络中，请先组网在使用此功能", 0).show();
				return;
			}
			Intent intent = new Intent();
			switch (arg2) {
			case 0:
				intent.setClass(getActivity(), DialNoActivity.class);
				intent.putExtra(Constants.EXTRA_TYPE, Constants.VOICE_REQ);
				break;
			case 1:
				intent.setClass(getActivity(), DialNoActivity.class);
				intent.putExtra(Constants.EXTRA_TYPE, Constants.VIDEO_REQ);
				break;
			case 2:
				intent.setClass(getActivity(), DialNoActivity.class);
				intent.putExtra(Constants.EXTRA_TYPE, Constants.MSG_REQ);
				break;
			case 3:
				intent.setClass(getActivity(), MultitalkingActivity.class);
				serviceIntent.putExtra(Constants.EXTRA_CMD, Constants.MULTI);
				serviceIntent.putExtra(Constants.EXTRA_CMD_TYPE, Constants.VOICE_MULTI);
				getActivity().startService(serviceIntent);
				break;
			case 4:
				intent.setClass(getActivity(), FileChooseActivity.class);
				break;
			case 5:
				// 浏览网页
//				intent.setClass(getActivity(), ExploreActivity.class);
//				break;
				// 在红米2A上有bug，要规避。由于语音视频等业务对IP的监听可能出问题
				Toast.makeText(BusinessFragment.this.getActivity(), "浏览网页", 0).show();
				return;
			}
			
			if (intent!=null) {
				startActivity(intent);
			}
		}
	}
}
