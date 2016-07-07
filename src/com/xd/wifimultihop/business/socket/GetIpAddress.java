package com.xd.wifimultihop.business.socket;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.conn.util.InetAddressUtils;

import android.util.Log;

public class GetIpAddress {
	public static InetAddress getLocalAddressInet() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				if (intf.getDisplayName().equals("wlan0")
						|| intf.getDisplayName().equals("eth0")) {
					for (Enumeration<InetAddress> enumIpAddr = intf
							.getInetAddresses(); enumIpAddr.hasMoreElements();) {
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress()
								&& InetAddressUtils.isIPv4Address(inetAddress
										.getHostAddress().toString())) {
							return inetAddress;
						}
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("Print", ex.toString());
		}
		return null;
	}

	public static String getLocalAddressString() {
		try {

			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				if (intf.getDisplayName().equals("wlan0")
						|| intf.getDisplayName().equals("eth0")) {
					for (Enumeration<InetAddress> enumIpAddr = intf
							.getInetAddresses(); enumIpAddr.hasMoreElements();) {
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress()
								&& InetAddressUtils.isIPv4Address(inetAddress
										.getHostAddress().toString())) {
							return inetAddress.getHostAddress().toString();
						}
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("Print", ex.toString());
		}
		return null;
	}

	public static boolean isIpAddress(String address) {
		String regex = "(2[5][0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})"
				+ "\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})"
				+ "\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})"
				+ "\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(address);
		return m.matches();
	}
}
