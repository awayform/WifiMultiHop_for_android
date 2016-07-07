package com.xd.wifimultihop.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PrefUtils {

	private Context context;
	private SharedPreferences sp;
	private Editor editor;
	
	public PrefUtils(Context context){
		this.context = context;
		sp = this.context.getSharedPreferences("routeppp", Context.MODE_PRIVATE);
		editor = sp.edit();
	}
	public void restoreString(String key, String value){
		editor.putString(key, value);
		editor.commit();
	}
	public void restoreInt(String key, int value){
		editor.putInt(key, value);
		editor.commit();
	}
	public void restoreBoolean(String key, boolean value){
		editor.putBoolean(key, value);
		editor.commit();
	}
	public boolean getBoolean(String key){
		return sp.getBoolean(key, false);
	}
	public boolean getBooleanTrue(String key){
		return sp.getBoolean(key, true);
	}
	public String getString(String key){
		return sp.getString(key, "");
	}
	public int getInt(String key){
		return sp.getInt(key, 0);
	}
	public void removeKey(String key){
		editor.remove(key);
		editor.commit();
	}
	
}
