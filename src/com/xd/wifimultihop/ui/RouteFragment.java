package com.xd.wifimultihop.ui;


import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.googlecode.android.wifi.tether.TetherApplication;
import com.xd.adhocroute1s.AdhocRouteApp;
import com.xd.adhocroute1s.data.Interface;
import com.xd.adhocroute1s.data.OlsrDataDump;
import com.xd.adhocroute1s.data.Route;
import com.xd.adhocroute1s.route.RouteAdapter;
import com.xd.adhocroute1s.route.RouteRefresh;
import com.xd.adhocroute1s.route.RouteRefresh.Callback;
import com.xd.adhocroute1s.route.RouteServices;
import com.xd.adhocroute1s.utils.ConfigHelper;
import com.xd.wifimultihop.R;

public class RouteFragment extends Fragment implements OnClickListener {

	public static final String ACTION_DIALOG_SHOW_BROADCASTRECEIVER = "action.dialog.startroute.show";
	public static final String ACTION_DIALOG_ROUTE_HIDE_BROADCASTRECEIVER = "action.dialog.startroute.hide";
	public static final int INTERFACE_NOT_EXIST = 0x08;
	private ImageButton olsrd_switch;
	private List<Route> routeTables = new ArrayList<Route>();
	private AdhocRouteApp application;
	private ListView lvRoute;
	private View emptyListView;
	private Timer timer;
	private TextView tvinfo;
	private RouteAdapter adapter;
	private ProgressDialog tipDialog;
	private View view;
	
	private Handler handler = null;
	
	private static class UIHandler extends Handler{
		WeakReference<RouteFragment> outerReference;
		
		public UIHandler(RouteFragment outer) {
			outerReference = new WeakReference<RouteFragment>(outer);;
		}
		public void handleMessage(android.os.Message msg) {
			RouteFragment routeFragment = outerReference.get();
			if (routeFragment == null) return;
			switch (msg.what) {
			case INTERFACE_NOT_EXIST:
				routeFragment.application.showToastMsg(R.string.toast_interface_set_not_exist);
				break;
			default:
				break;
			}
		}
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view  = inflater.inflate(R.layout.fragment_route, null);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		application = ((TetherApplication)getActivity().getApplication()).adhocRouteApp;
		handler = new UIHandler(this);
		
		registerDialogBroadcastReceiver();
		initUI();
		setSwitchState();
		adapter = new RouteAdapter(routeTables, getActivity());
		lvRoute.setAdapter(adapter);
		timer = new Timer();
		timer.schedule(new RefreshTimeTask(), 0, 2000);
	}
	
	private void initUI() {
		lvRoute = (ListView) view.findViewById(R.id.lv_route);
		emptyListView = view.findViewById(R.id.empty_list_view);
		olsrd_switch = (ImageButton) view.findViewById(R.id.ib_olsrd);
		tvinfo = (TextView) view.findViewById(R.id.tv_info);
		olsrd_switch.setOnClickListener(this);
	}
	
	private void setSwitchState() {
		if (application.service == null) {
			// service为null说明首次启动手机
			application.stopProcess(RouteServices.CMD_OLSR_CONTAIN);
//			application.adhocHelper.exitNet();
			AdhocRouteApp.appState = false;
			olsrd_switch.setImageResource(R.drawable.power_off_icon);
		} else {
			AdhocRouteApp.appState = true;
			olsrd_switch.setImageResource(R.drawable.power_on_icon);
		}
	}

	private class RefreshTimeTask extends TimerTask {
		@Override
		public void run() {
			application.routeRefresh.refreshRoute(new Callback() {
				@Override
				public void onSuccess(OlsrDataDump olsrDataDump) {
					List<Interface> interfaces = (List<Interface>)olsrDataDump.interfaces;
					if (interfaces != null && interfaces.size() != 0) {
						Interface inface = interfaces.get(0);
						tvinfo.setText("IP：" + inface.ipv4Address + "\n"
									 + "网卡：" + inface.name + "\n"
									 + "mac地址：" + inface.macAddress
										);
					}
					List<Route> routes = (List<Route>)olsrDataDump.routes;
					if (routes.size() == 0) {
						emptyListView.setVisibility(View.VISIBLE);
						lvRoute.setVisibility(View.GONE);
					} else {
						lvRoute.setVisibility(View.VISIBLE);
						emptyListView.setVisibility(View.GONE);
					}
					adapter.update(routes);
				}
				
				@Override
				public void onException(int exception) {
					if (exception == RouteRefresh.REFRESH_UNSTARTED) {
						// 路由未开启
						adapter.update(new ArrayList<Route>());
						lvRoute.setVisibility(View.GONE);
						emptyListView.setVisibility(View.GONE);
						tvinfo.setText(R.string.adhoc_not_started);
					}
				}
			});
		}
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.ib_olsrd) {
			if (!AdhocRouteApp.appState) { 
				showDialog();
				application.executorService.execute(new Runnable() {
					@Override
					public void run() {
						// 建网
						
//						if (application.adhocHelper.openWifiAndConnect()) {
							if (!ConfigHelper.configAPP(getActivity())) {
								handler.sendEmptyMessage(INTERFACE_NOT_EXIST);
								// 关闭进度条
								tipDialog.dismiss();
//								application.adhocHelper.exitNet();
							} else {
								application.startService();
							}
//						} else {
//							// 建网失败
//							tipDialog.dismiss();
//							application.adhocHelper.exitNet();
//						}
					}
				});
 			} else{
				// 关闭路由
				application.stopService();
				AdhocRouteApp.appState = false;
				olsrd_switch.setImageResource(R.drawable.power_off_icon);
			}
		}
	}
	
	private void showDialog() {
		tipDialog = new ProgressDialog(getActivity());
		tipDialog.setTitle(R.string.adhoc_start_dialog_title);
		tipDialog.setMessage(getString(R.string.adhoc_start_dialog_message));
		tipDialog.setCanceledOnTouchOutside(false);
		tipDialog.setCancelable(false);
		tipDialog.show();
	}

	private void registerDialogBroadcastReceiver() {
		IntentFilter filter = new IntentFilter();  
        filter.addAction(ACTION_DIALOG_ROUTE_HIDE_BROADCASTRECEIVER);
        getActivity().registerReceiver(dialogShowHideReceiver, filter);
	}
	
	private void unregisterDialogBroadcastReceiver() {
        getActivity().unregisterReceiver(dialogShowHideReceiver);
	}
	private BroadcastReceiver dialogShowHideReceiver = new BroadcastReceiver(){  
		  
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
        	if (action.equals(ACTION_DIALOG_SHOW_BROADCASTRECEIVER)){
        		// Omitted
        	} else if (action.equals(ACTION_DIALOG_ROUTE_HIDE_BROADCASTRECEIVER)) {
        		boolean isStarted = intent.getBooleanExtra("isStarted", false);
        		if (tipDialog != null) {
        			tipDialog.dismiss();
        		}
        		if (isStarted) {
        			application.showToastMsg(R.string.toast_adhoc_start_succeed);
        			olsrd_switch.setImageResource(R.drawable.power_on_icon);
        		} else {
        			application.showToastMsg(R.string.toast_adhoc_start_failed);
        			olsrd_switch.setImageResource(R.drawable.power_off_icon);
//        			application.adhocHelper.exitNet();
        		}
        	}
        }
    };

    public void onDestroyView() {
    	super.onDestroyView();
    	timer.cancel();
		unregisterDialogBroadcastReceiver();
    };
	
	
}
