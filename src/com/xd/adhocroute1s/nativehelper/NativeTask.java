package com.xd.adhocroute1s.nativehelper;

import android.util.Log;

/**
 * 使用代码执行命令行程序，函数使用native实现
 * @author qhyuan1992
 *
 */
public class NativeTask {

	public static final String MSG_TAG = "AdhocRoute -> NativeTask";

	static {
        try { 
            Log.i(MSG_TAG, "Trying to load libnativecommand.so");
            System.loadLibrary("nativecommand");
        }
        catch (UnsatisfiedLinkError ule) {
            Log.e(MSG_TAG, "Could not load libnativecommand.so");
        }
    }
    public static native String getProp(String name);
    public static native int runCommand(String command);
    public static native int killProcess(int parameter, String processName);
}
