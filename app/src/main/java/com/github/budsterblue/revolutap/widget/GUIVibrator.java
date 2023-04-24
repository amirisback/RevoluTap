package com.github.budsterblue.revolutap.widget;

import android.content.Context;
import android.os.Build;
import android.os.Vibrator;

import com.github.budsterblue.revolutap.R;
import com.github.budsterblue.revolutap.util.Tools;
import com.github.budsterblue.revolutap.util.ToolsTracker;

public class GUIVibrator {
	
	private Vibrator v;
	private int vibrateMiss;
	private int vibrateTap;
	private int vibrateHold;
	private int holdsCount;
	
	// This is called in GUIHandler's constructor, which is called by GUIGame's onCreate 
	public GUIVibrator() {
		try {
			vibrateMiss = Integer.parseInt(Tools.getSetting(R.string.vibrateMiss, R.string.vibrateMissDefault));
			vibrateTap = Integer.parseInt(Tools.getSetting(R.string.vibrateTap, R.string.vibrateTapDefault));
			vibrateHold = Integer.parseInt(Tools.getSetting(R.string.vibrateHold, R.string.vibrateHoldDefault));
			holdsCount = 0;
			v = (Vibrator)Tools.c.getSystemService(Context.VIBRATOR_SERVICE);
		} catch (Exception e) {
			ToolsTracker.error("GUIVibrator.init", e, "SDK version: " + Build.VERSION.SDK_INT);
		}
	}
	
	// Call this in GUIHandler's releaseVibrator(), which is called by GUIGame's onDestroy
	public void release() {
		try {
			pause();
		} catch (Exception e) {
			ToolsTracker.error("GUIVibrator.release", e, "");
		}
	}
	
	public void endHold() {
		try {
			holdsCount--;
			if (holdsCount <= 0) {
				holdsCount = 0;
				if (v != null) {
					v.cancel();
				}
			}
		} catch (Exception e) {
			ToolsTracker.error("GUIVibrator.endHold", e, "");
		}
	}
	
	public void pause() {
		try {
			holdsCount = 0;
			if (v != null) {
				v.cancel();
			}
		} catch (Exception e) {
			ToolsTracker.error("GUIVibrator.pause", e, "");
		}
	}
	
	public void vibrateTap() {
		try {
			switch(vibrateTap) {
				case 0:
					break;
				case 1:
					if (v != null) {
						v.vibrate(15);
					}
					break;
				case 2:
					if (v != null) {
						v.vibrate(30);
					}
					break;
				default:
					break;
			}
		} catch (Exception e) {
			ToolsTracker.error("GUIVibrator.vibrateTap", e, "");
		}
	}
	
	public void vibrateHold(boolean hasStartedVibrating) {
		try {
			if (!hasStartedVibrating) {
				holdsCount++;
			}
			if (vibrateHold == 1){
				v.vibrate(10000);
			}
		} catch (Exception e) {
			ToolsTracker.error("GUIVibrator.vibrateHold", e, "");
		}
	}
	
	public void vibrateMiss() {
		try {
			switch (vibrateMiss) {
				case 0:
					break;
				case 1:
					v.vibrate(25);
					break;
				case 2:
					v.vibrate(50);
					break;
				default:
					break;
			}
		} catch (Exception e) {
			ToolsTracker.error("GUIVibrator.vibrateMiss", e, "");
		}
	}
}
