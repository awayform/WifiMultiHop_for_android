package com.xd.adhocroute1s.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * 对SharedPreference的简单封装
 * @author qhyuan1992
 *
 */
public class PreferenceUtils {
	
	private SharedPreferences sp;

	public PreferenceUtils(Context context) {
		sp = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public String getString(String key, String defValue) {
		return sp.getString(key, defValue);
	}
	public boolean getBoolean(String key, boolean defValue) {
		return sp.getBoolean(key, defValue);
	}
	public float getFloat(String key, float defValue) {
		return sp.getFloat(key, defValue);
	}
	public int getInt(String key, int defValue) {
		return sp.getInt(key, defValue);
	}
}
