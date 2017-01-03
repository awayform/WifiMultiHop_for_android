package com.xd.adhocroute1s.route;

import java.io.File;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.googlecode.android.wifi.tether.R;
import com.googlecode.android.wifi.tether.TetherApplication;
import com.xd.adhocroute1s.AdhocRouteApp;
import com.xd.wifimultihop.ui.MainActivity;

public class RouteServices extends Service {
	public static final String CMD_OLSR_CONTAIN = "app_bin/wifiroute";
	private static final int ID_FORGROUND = 2;
	private String olsrdPath;
	private String olsrdConfPath;
	private String olsrStartCmd = "";
	private AdhocRouteApp application = null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		application = ((TetherApplication)getApplication()).adhocRouteApp;
//		runForground();
		olsrdPath = new File(getDir("bin", Context.MODE_PRIVATE), "wifiroute").getAbsolutePath();
		olsrdConfPath = new File(getFilesDir(), "wifiroute.conf").getAbsolutePath();
		olsrStartCmd = olsrdPath + " -f " +  olsrdConfPath; /* + " -i " + inface*/
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		application.serviceStarted(this);
		flags = START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}

	@SuppressWarnings("unused")
	private void runForground(){
		// 创建一个启动其他Activity的Intent  
        Intent intent = new Intent(this, MainActivity.class);  
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);  
        Notification notification = new Notification.Builder(this)  
            .setTicker("路由程序正在运行")
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("Adhoc路由")  
            .setContentText("路由程序正在运行...")  
            .setWhen(System.currentTimeMillis())  
            .setContentIntent(pi)
            .getNotification();
        startForeground(ID_FORGROUND, notification);
	}
	
	@Override
	public void onDestroy() {
		application.serviceDestroy();
		super.onDestroy();
	}
	
	public void startOLSR() {
		application.startProcess(olsrStartCmd);
	}
	
	public void stopOLSR() {
		application.stopProcess(CMD_OLSR_CONTAIN);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
}