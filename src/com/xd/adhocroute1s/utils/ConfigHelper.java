package com.xd.adhocroute1s.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.googlecode.android.wifi.tether.TetherApplication;
import com.xd.adhocroute1s.AdhocRouteApp;
import com.xd.adhocroute1s.route.RouteConfig;

public class ConfigHelper {
	public static final String TAG = "AdhocRoute -> NativeHelper";
	public static File app_bin;
	private static String olsrdFilePath;

	/**
	 * 配置运行时参数配置，将其写入文件
	 * @param context
	 * @return true表示配置成功 false表示配置失败
	 */
	public static boolean configAPP(Context context) {
		setup(context);
		unzipAssets(context);
		if (updateConfig(context)) return true;
		return false;
	}

	public static void setup(Context context) {
		app_bin = context.getDir("bin", Context.MODE_PRIVATE).getAbsoluteFile();
		olsrdFilePath = new File(app_bin, "wifiroute").getAbsolutePath();
	}

	public static boolean unzipAssets(Context context) {
		boolean result = true;
		try {
			AssetManager am = context.getAssets();
			final String[] assetList = am.list("");

			for (String asset : assetList) {
				if (asset.equals("images") || asset.equals("sounds") || asset.equals("webkit") || asset.equals("databases") || asset.equals("kioskmode"))
					continue;
				int BUFFER = 2048;
				final File file = new File(ConfigHelper.app_bin, asset);
				final InputStream assetIS = am.open(asset);
				if (file.exists()) {
					file.delete();
					// 没必要都删除了，只需要每次更新配置文件即可,后面修改
					Log.i(AdhocRouteApp.TAG, "NativeHelper.unzipDebiFiles() deleting " + file.getAbsolutePath());
				}
				FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
				int count;
				byte[] data = new byte[BUFFER];
				while ((count = assetIS.read(data, 0, BUFFER)) != -1) {
					dest.write(data, 0, count);
				}
				dest.flush();
				dest.close();
				assetIS.close();
			}
		} catch (IOException e) {
			result = false;
			Log.e(AdhocRouteApp.TAG, "Can't unzip", e);
		}
		chmod("0775", new File(olsrdFilePath));
		return result;
	}
	
	public static void chmod(String modestr, File path) {
		Log.i(TAG, "chmod " + modestr + " " + path.getAbsolutePath());
		try {
			Class<?> fileUtils = Class.forName("android.os.FileUtils");
			Method setPermissions = fileUtils.getMethod("setPermissions", String.class, int.class, int.class, int.class);
			int mode = Integer.parseInt(modestr, 8);
			int a = (Integer) setPermissions.invoke(null, path.getAbsolutePath(), mode, -1, -1);
			if (a != 0) {
				Log.i(TAG,"ERROR: android.os.FileUtils.setPermissions() returned " + a + " for '" + path + "'");
			}
		} catch (ClassNotFoundException e) {
			Log.i(TAG, "android.os.FileUtils.setPermissions() failed:", e);
		} catch (IllegalAccessException e) {
			Log.i(TAG, "android.os.FileUtils.setPermissions() failed:", e);
		} catch (InvocationTargetException e) {
			Log.i(TAG, "android.os.FileUtils.setPermissions() failed:", e);
		} catch (NoSuchMethodException e) {
			Log.i(TAG, "android.os.FileUtils.setPermissions() failed:", e);
		}
	}

	// 每次启动的时候去检查设置信息，修改配置文件
	public static boolean updateConfig(Context context) {
		AdhocRouteApp app = ((TetherApplication) context.getApplicationContext()).adhocRouteApp;
		// 网卡
		String inface = app.preferenceUtils.getString("interface", "wlan0");
		// 外网
		boolean isWanEnabled = app.preferenceUtils.getBoolean("is_wan_enabled", false);
		String wanSubnet = app.preferenceUtils.getString("wan_subnet", "");
		String wanMask = app.preferenceUtils.getString("wan_mask", "");
		// 网关
		boolean staticGatewayEnabled = app.preferenceUtils.getBoolean("is_static_gateway_enabled", false);
		boolean dynamicCheckGateway = app.preferenceUtils.getBoolean("is_dyncheck_gateway_enabled", false);
		// 动态监测网关
		String dynGatewayPath = ConfigHelper.app_bin.getAbsolutePath() + "/wifiroute_dyn_gw";

		// 网卡信息
		if (!app.coretask.networkInterfaceExists(inface)) {
			return false;
		}
		
		String baseConfig = ConfigHelper.app_bin.getAbsolutePath() + "/wifiroute.conf";
		// config里面已经有了基础的配置信息
		RouteConfig config = null;
		try {
			config = new RouteConfig(baseConfig);
		} catch (FileNotFoundException e) {
			File olsrdConfFile = new File(app_bin,"/wifiroute.conf");
			try {
				config = new RouteConfig(olsrdConfFile.getAbsolutePath());
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			Log.e(TAG, "file <baseConfig> not found :");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Hna消息(外网和静态网关均在这里设置)
		if (isWanEnabled || staticGatewayEnabled) {
			RouteConfig.HnaInfo hnaInfo = new RouteConfig.HnaInfo();
			if (isWanEnabled) {
				if (!wanSubnet.isEmpty() && !wanMask.isEmpty()) {
					hnaInfo.addWan(wanSubnet, wanMask);
				}
			}
			if (staticGatewayEnabled) {
				hnaInfo.addWan("0.0.0.0", "0.0.0.0");
			}
			config.addComplexConfigInfo(hnaInfo);
		}
		
		// 是否开启动态检测网关
		if (dynamicCheckGateway) {
			RouteConfig.PluginInfo dynamicCheckGatewayPluginInfo = new RouteConfig.PluginInfo(dynGatewayPath);
			config.addComplexConfigInfo(dynamicCheckGatewayPluginInfo);
		}
		
		// 设置网卡信息
		RouteConfig.InterfaceInfo infaceInfo = new RouteConfig.InterfaceInfo(inface);
		config.addComplexConfigInfo(infaceInfo);
		
		// 到目前为止 已经把所有的配置信息转化为对应成这个类Config
		try {
			FileOutputStream olsrdConf = context.openFileOutput("wifiroute.conf", 0);
			config.write(olsrdConf);
			olsrdConf.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

}
