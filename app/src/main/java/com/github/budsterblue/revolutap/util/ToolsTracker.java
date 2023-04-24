package com.github.budsterblue.revolutap.util;

import java.util.HashMap;

import android.util.Log;


public class ToolsTracker {
	private static final String appVersion = "";
	
	// Called by MenuHome.onDestroy

	public static void info(String event) {
		Log.i("RevoluTap info:", appVersion + "Info: " + event);
	}
	
	public static void error(String event, Throwable e, String value) {
		// logcat this
		String ev = appVersion + "Error: " + event;
		String atr = e.getMessage();
		Log.e("RevoluTap exception:", ev + " / " + atr + " / " + value);
	}
	
	public static void data(String event, String attribute, String value) {
		Log.v("RevoluTap data (str):", appVersion + "Data: " + event + " / " + attribute + " / " + value);
	}
	
	public static void data(String event, HashMap<String,String> attributes) {
		Log.v("RevoluTap data (hash):", appVersion + "Data: " + event + " / " + attributes);
	}
	
}
