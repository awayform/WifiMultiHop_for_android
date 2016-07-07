package com.xd.adhocroute1s;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Application;
import android.content.Intent;
import android.widget.Toast;

import com.googlecode.android.wifi.tether.TetherApplication;
import com.xd.adhocroute1s.nativehelper.CoreTask;
import com.xd.adhocroute1s.route.RouteRefresh;
import com.xd.adhocroute1s.route.RouteServices;
import com.xd.adhocroute1s.utils.PreferenceUtils;
import com.xd.wifimultihop.ui.RouteFragment;


public class AdhocRouteApp {
	public static String TAG = "AdhocRoute -> AdhocRouteApp";
/*
	// APP state
	public final static int STATE_STOPPED = 0;
	public final static int STATE_NET_CONSTRUCTING= 1;
	public final static int STATE_NET_CONSTRUCT_FAILED = 2;
	public final static int STATE_NET_CONSTRUCT_SUCCEED = 3;
	public final static int STATE_ROUTE_RUNNING = 4;
	public final static int STATE_ROUTE_RUN_FAILED = 5;
	public final static int STATE_ROUTE_RUN_SUCCEED = 6;
	
	public static int appState = STATE_STOPPED;
*/

	private TetherApplication tetherApplication;
	
	public void init(TetherApplication app) {
		tetherApplication = app;
		coretask = new CoreTask();
		routeRefresh = new RouteRefresh(this);
		preferenceUtils = new PreferenceUtils(app);
		executorService = Executors.newFixedThreadPool(3);
	}
	// 标识自组织网络启动成功与否
	public static boolean appState = false;
	
	public RouteServices service = null;
	public ExecutorService executorService;
	public CoreTask coretask;
	public RouteRefresh routeRefresh;
	public PreferenceUtils preferenceUtils;
//	public AdhocHelper adhocHelper;
	private Toast toast;
	
	public void showToastMsg(String msg) {
        if (null == msg || "".equals(msg)) {
            return;
        }
        if (toast == null) {
            toast = Toast.makeText(tetherApplication, msg, Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
            toast.setDuration(Toast.LENGTH_SHORT);
        }
        toast.show();
    }
	
	public void showToastMsg(int msgID) {
        if (toast == null) {
            toast = Toast.makeText(tetherApplication, tetherApplication.getString(msgID), Toast.LENGTH_SHORT);
        } else {
            toast.setText(tetherApplication.getString(msgID));
            toast.setDuration(Toast.LENGTH_SHORT);
        }
        toast.show();
    }


	public void startProcess(final String proc) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				appState = true;
				CoreTask.startProcess(proc);
				appState = false;
			}
		});

		executorService.execute(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent(RouteFragment.ACTION_DIALOG_ROUTE_HIDE_BROADCASTRECEIVER);
				try {
					Thread.sleep(2500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (appState == true && coretask.isProcessRunning(RouteServices.CMD_OLSR_CONTAIN)) {
					setDNS();
					setNAT();
					intent.putExtra("isStarted", true);
				} else {
					intent.putExtra("isStarted", false);
				}
				tetherApplication.sendBroadcast(intent);
			}
		});
	}
	
	private void setNAT() {
		// 设置NAT
		if (preferenceUtils.getBoolean("open_nat", false)) {
			// 先查看是否文件里是不是“1”，不是1设置成1
			if (!coretask.isNatEnabled()) {
				coretask.setNatEnabled();
			}
			String natsubnet = preferenceUtils.getString("natsubnet", "");
			String natip = preferenceUtils.getString("natip", "");
			String natinterface = preferenceUtils.getString("natinterface", "");
			// 加之前先把有的删了，有问题，可能把不该删的删了
			if ((!natsubnet.equals("")) && (!natip.equals(""))) {
				coretask.delAllNat();
				coretask.addNatWithSrcAndIP(natsubnet, natip);
			} else if ((!natsubnet.equals("")) && (!natinterface.equals(""))) {
				coretask.delAllNat();
				coretask.addNatWithSrcAndInterface(natsubnet, natinterface);
			} else if (!natsubnet.equals("")) {
				coretask.delAllNat();
				coretask.addNatWithSrc(natsubnet);
			} else if (!natinterface.equals("")) {
				coretask.delAllNat();
				coretask.addNatWithInterface(natinterface);
			} else {
				coretask.delAllNat();
				coretask.addNat();
			}
		} else {
			coretask.delAllNat();
		}
	}
	
	private void setDNS() {
		String firstDns = preferenceUtils.getString("dns1", "8.8.8.8");
//		String secondDns = preferenceUtils.getString("dns2", "8.8.4.4");
		CoreTask.setFirstDns(firstDns);
//		CoreTask.setSecondDns(secondDns);
	}
	
	public void stopProcess(final String proc) {
		if (coretask.isProcessRunning(proc)) {
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					if (coretask.killProcess(proc)) {
						appState = false;
//						adhocHelper.exitNet();
					}
				}
			});
		}
	}

	public void serviceStarted(RouteServices s) {
		service = s;
		service.startOLSR();
	}

	public void serviceDestroy() {
		service.stopOLSR();
		service = null;
	}

	public void startService() {
		tetherApplication.startService(new Intent(tetherApplication, RouteServices.class));
	}

	public void stopService() {
		if (service != null){
			service.stopOLSR();
			service.stopSelf();
		}
	}
	
}
