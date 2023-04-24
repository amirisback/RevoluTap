package com.github.budsterblue.revolutap.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.github.budsterblue.revolutap.R;

public class Tools {
	
	public static Activity c = null;
	public static Resources res = null;
	private static SharedPreferences settings = null;
	private static SharedPreferences.Editor editor = null;

	public static boolean tablet = false;
	public static int screen_w = 0;
	public static int screen_h = 0;
	public static int screen_s = 0;
	public static int screen_r = 0;
	private static int statusBarHeight = 0;
	//private static int titleBarHeight = 0;
	
	// TODO - 64x64 pixel arrows, should these be hardcoded? 
	//public static final int button_w = 64;
	//public static final int button_h = 64;
	public static int button_w;
	public static int button_h;
	public static int health_bar_h;
	
	public static final int PITCHES = 4;
	public static final int MAX_OPA = 255;
	public static final int AUTOPLAY_WINDOW = 20;
	public static final int BUFFER = 1024; // 1kb
	public static final int BUFFER_LARGE = 1048576; // 1mb
	
	public static final int REVERSE = 0;
	public static final int STANDARD = 1;
	// Just turn on osu_rand and set to osu_mod
	public static int gameMode;
	
	public static void setContext(Activity activityContext) {
		c = activityContext;
		res = c.getResources();
		settings = PreferenceManager.getDefaultSharedPreferences(c);
		editor = settings.edit();
		editor.apply();
		updateGameMode();
	}
	
	public static void updateGameMode() {
		gameMode = Integer.parseInt(getSetting(R.string.gameMode, R.string.gameModeDefault));
	}

	public static void setTopbarHeight(Activity a) {
		Rect rect= new Rect();
		Window window = a.getWindow();
		window.getDecorView().getWindowVisibleDisplayFrame(rect);
		statusBarHeight = rect.top;
	}

	public static void setScreenDimensions() {
		//Display display = c.getWindow().getWindowManager().getDefaultDisplay();
		//screen_w = display.getWidth();
		screen_w = Resources.getSystem().getDisplayMetrics().widthPixels;
		screen_h = Resources.getSystem().getDisplayMetrics().heightPixels;
		//screen_h = display.getHeight();
		screen_s = Math.min(screen_h, screen_w);
		if (!Objects.equals(settings.getString(
				res.getString(R.string.fullscreen),
				res.getString(R.string.fullscreenDefault)
		), "1")) {
			screen_h -= statusBarHeight;
		}
		int screen_size = c.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
		if (screen_size == 4) { //Configuration.SCREENLAYOUT_SIZE_XLARGE) {
			tablet = true;
		}
		if (tablet) {
			button_w = (int)(screen_s / 5.5);
		} else {
			button_w = (int)(screen_s / 4.5);
		}
		button_h = button_w;
		health_bar_h = Tools.scale(7 + 17 + 17); // margin + height * 2, one margin less than GUIGame's HP bar
		screen_r =
			(screen_h > screen_w) ?
			(screen_w - health_bar_h - button_w) / 2 :
			(screen_h - health_bar_h - button_h) / 2;
	}
	
	public static double scale(double dimension) {
		return dimension * screen_s / 320; // 320x480 is standard
	}
	
	public static int scale(int dimension) {
		return dimension * screen_s / 320; // 320x480 is standard
	}
	
	public static float scale(float dimension) {
		return dimension * screen_s / 320; // 320x480 is standard
	}
	
	public static String getBasename(String s) {
		if (s.contains("/") && s.contains(".")) {
			return s.substring(s.lastIndexOf('/') + 1, s.lastIndexOf('.'));
		} else if (s.contains("/")) {
			return s.substring(s.lastIndexOf('/') + 1);
		} else if (s.contains(".")) {
			return s.substring(0, s.lastIndexOf('.'));
		} else {
			return s;
		}
	}
	
	public static boolean isOGGFile(String s) {
		return (s.endsWith(".ogg") || s.endsWith(".OGG"));
	}
	
	public static boolean isStepfile(String s) {
		return isSMFile(s) || isDWIFile(s);
	}
	
	public static boolean isSMFile(String s) {
		return (s.endsWith(".sm") || s.endsWith(".SM"));
	}
	
	public static boolean isDWIFile(String s) {
		return (s.endsWith(".dwi") || s.endsWith(".DWI"));
	}
	
	public static boolean isLink(String s) {
		return (s.endsWith(".url") || s.endsWith(".URL"));
	}
	
	public static boolean isText(String s) {
		return (s.endsWith(".txt") || s.endsWith(".TXT"));
	}
	
	public static boolean isSMZip(String s) {
		return (s.endsWith(".zip") ||
				s.endsWith(".ZIP") ||
				s.endsWith(".smzip") ||
				s.endsWith(".SMZIP"));
	}
	
	public static boolean isStepfilePack(String s) {
		return isSMZip(s);
	}
	
	public static String checkStepfileDir(File dir) {
		if (!dir.exists() || !dir.canRead() || !dir.isDirectory()) {
			return null;
		}
		// Favour .sm files before .dwi files
		for (File f : Objects.requireNonNull(dir.listFiles())) {
			if (Tools.isSMFile(f.getPath())) {
				return f.getPath();
			}
		}
		for (File f : Objects.requireNonNull(dir.listFiles())) {
			if (Tools.isDWIFile(f.getPath())) {
				return f.getPath();
			}
		}
		return null;
	}
	
	public static String[] getStringArray(int id) {
		return res.getStringArray(id);
	}
	
	public static String getSetting(int key, int defValue) {
		return settings.getString(res.getString(key), res.getString(defValue));
	}

	public static boolean getBooleanSetting(int key, int defValue) {
		return Objects.equals(settings.getString(res.getString(key), res.getString(defValue)), "1");
	}

	public static void putSetting(int key, String value) {
		editor.putString(res.getString(key), value);
		editor.commit();
	}
	
	public static void resetSettings() {
		editor.clear();
		editor.commit();
		editor.putString(res.getString(R.string.resetSettings), "0");
	}
	
	public static String getString(int id) {
		return res.getString(id);
	}
	
	public static void debugLogCat(String msg) {
		if (getBooleanSetting(R.string.debugLogCat, R.string.debugLogCatDefault)) {
			Log.i(c.getClass().getName(), msg);
		}
	}
	
	public static void startWebsiteActivity(String link) {
		ToolsTracker.data("Opened link", "link", link);
		Intent webBrowser = new Intent(Intent.ACTION_VIEW);
		webBrowser.setData(Uri.parse(link));
		c.startActivity(webBrowser);
	}
	
	public static void toast(String msg) {
		if (c == null) return;
		try {
			Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
		} catch (RuntimeException e) {
			// Don't know how to get a thread's parent to call toast...
			ToolsTracker.error("Tools.toast", e, c.getLocalClassName());
		}
		debugLogCat(msg);
	}
	
	public static void toast_long(String msg) {
		if (c == null) return;
		try {
			Toast.makeText(c, msg, Toast.LENGTH_LONG).show();
		} catch (RuntimeException e) {
			// Don't know how to get a thread's parent to call toast...
			ToolsTracker.error("Tools.toast_long", e, c.getLocalClassName());
		}
		debugLogCat(msg);
	}
	
	public static OnClickListener cancel_action = (dialog, id) -> dialog.cancel();
	
	private static void alert_dialog(
			String title, int icon, CharSequence msg,
			String yes_msg, OnClickListener yes_action,
			String no_msg, OnClickListener no_action,
			final int ignoreSetting, boolean checked
		) {
		alert_dialog(title, icon, msg, yes_msg, yes_action, no_msg, no_action, ignoreSetting, checked, false);
	}
	private static void alert_dialog(
			String title, int icon, CharSequence msg,
			String yes_msg, OnClickListener yes_action,
			String no_msg, OnClickListener no_action,
			final int ignoreSetting, boolean checked,
			boolean cancelable
		) {
		if (c == null) return;
		
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(c);
		alertBuilder.setCancelable(cancelable);
		alertBuilder.setTitle(title);
		alertBuilder.setIcon(icon);
		
		if (ignoreSetting != -1) {
			View notes = LayoutInflater.from(c).inflate(R.layout.notes, null);
			TextView notes_text = notes.findViewById(R.id.notes_text);
			notes_text.setText(msg);
			notes_text.setTextColor(Color.WHITE);
			if (msg.length() < 300) notes_text.setTextSize(16); // Normal size?
			
			CheckBox checkbox = notes.findViewById(R.id.checkbox);
			View.OnClickListener ignoreCheck = v -> {
				if (((CheckBox) v).isChecked()) {
					Tools.putSetting(ignoreSetting, "1");
				} else {
					Tools.putSetting(ignoreSetting, "0");
				}
			};
			if (checked) {
				checkbox.setChecked(true);
				Tools.putSetting(ignoreSetting, "1");
			}
			checkbox.setOnClickListener(ignoreCheck);
			alertBuilder.setView(notes);
		} else {
			alertBuilder.setMessage(msg);
		}
		
		if (no_action == null) {
			alertBuilder.setPositiveButton(yes_msg, yes_action);
		} else {
			alertBuilder.setPositiveButton(yes_msg, yes_action);
			alertBuilder.setNegativeButton(no_msg, no_action);
		}
		alertBuilder.show().setOwnerActivity(c);
		
		debugLogCat(msg.toString());
	}
	
	public static void note(
			String title, int icon, CharSequence msg,
			String yes_msg, OnClickListener yes_action,
			String no_msg, OnClickListener no_action,
			final int ignoreSetting
		) {
		alert_dialog(title, icon, msg, yes_msg, yes_action, no_msg, no_action, ignoreSetting, true);
	}
	
	public static void alert(
			String title, int icon, String msg,
			String yes_msg, OnClickListener yes_action,
			String no_msg, OnClickListener no_action,
			final int ignoreSetting
		) {
		alert_dialog(title, icon, msg, yes_msg, yes_action, no_msg, no_action, ignoreSetting, false);
	}
	public static void alert(
			String title, int icon, String msg,
			String yes_msg, OnClickListener yes_action,
			String no_msg, OnClickListener no_action,
			final int ignoreSetting, boolean cancelable
		) {
		alert_dialog(title, icon, msg, yes_msg, yes_action, no_msg, no_action, ignoreSetting, false, cancelable);
	}
	
	public static void warning(String msg, OnClickListener yes_action, final int ignoreSetting) {
		alert(
				res.getString(R.string.Button_warning), R.drawable.ic_warning_filled_black, msg,
				res.getString(R.string.Button_ok), yes_action,
				null, null,
				ignoreSetting
				);
	}
	public static void warning(String msg, OnClickListener yes_action, OnClickListener no_action,
		final int ignoreSetting) {
		alert(
				res.getString(R.string.Button_warning), R.drawable.ic_warning_filled_black, msg,
				res.getString(R.string.Button_yes), yes_action,
				res.getString(R.string.Button_no), no_action,
				ignoreSetting
				);
	}
	public static void warning(String msg, OnClickListener yes_action, OnClickListener no_action,
			final int ignoreSetting, boolean cancelable) {
			alert(
					res.getString(R.string.Button_warning), R.drawable.ic_warning_filled_black, msg,
					res.getString(R.string.Button_yes), yes_action,
					res.getString(R.string.Button_no), no_action,
					ignoreSetting, cancelable
					);
		}
	public static void error(String msg, OnClickListener yes_action) {
		alert(
				res.getString(R.string.Button_error), R.drawable.ic_error_filled_black, msg,
				res.getString(R.string.Button_ok), yes_action,
				null, null,
				-1
				);
	}
	public static void error(String msg, OnClickListener yes_action, OnClickListener no_action) {
		alert(
				res.getString(R.string.Button_error), R.drawable.ic_error_filled_black, msg,
				res.getString(R.string.Button_yes), yes_action,
				res.getString(R.string.Button_no), no_action,
				-1
				);
	}

	public static String getAppDir() {
		return Objects.requireNonNull(c.getExternalFilesDir(null)).getPath();
	}
	public static String getPacksDir() {
		return getAppDir() + res.getString(R.string.Tools_path_packs);
	}
	public static String getSinglesDir() {
		return getPacksDir() + res.getString(R.string.Tools_path_singles);
	}
	public static String getScreenshotsDir() {
		return getAppDir() + res.getString(R.string.Tools_path_screenshots);
	}
	public static String getNoteSkinsDir() {
		String path = getAppDir() + res.getString(R.string.Tools_path_noteskins);
		String noteskin = getSetting(R.string.noteskin, R.string.noteskinDefault);
		switch (noteskin) {
			case "default":
				path += "/default";
				break;
			case "original":
				path += "/original";
				break;
			case "custom":
				path += "/custom";
				break;
			default:
				path += "/default";
				break;
		}
		return path;
	}
	public static String getNoteSkinsDirDefault() {
		return getAppDir() + res.getString(R.string.Tools_path_noteskins) + "/default";
	}
	/*public static String getBackgroundsDir() {
		return getAppDir() + res.getString(R.string.Tools_path_backgrounds);
	}
	public static String getBackgroundRes() {
		String path = getBackgroundsDir();
		path += "/bg_blue.jpg";
		//String bgImage = getSetting(R.string.backgroundImage, R.string.backgroundImageDefault);
		//switch (bgImage) {
		//	case "blue":
		//		path += "/bg_blue.jpg";
		//		break;
		//	case "red":
		//		path += "/bg_red.jpg";
		//		break;
		//	case "white":
		//		path += "/bg_white.jpg";
		//		break;
		//	default:
		//		path += "/bg_blue.jpg";
		//		break;
		//}
		return path;
	}*/

	@SuppressLint("SimpleDateFormat")
	private static final SimpleDateFormat screenshotFormat = new SimpleDateFormat("yyyy'-'MM'-'dd'-'HHmmssSSS' '");

	public static void takeScreenshot(Bitmap b, String songTitle) {
		try {
			String screenshotDir = getScreenshotsDir();
			File dir = new File(screenshotDir);
			if (!dir.exists()) dir.mkdirs();
			String path = screenshotDir + "/" + screenshotFormat.format(new Date()) + songTitle + ".png";
			FileOutputStream fos = new FileOutputStream(path);
			b.compress(Bitmap.CompressFormat.PNG, 100, fos); // PNG is lossless
			fos.close();
			Tools.toast(Tools.getString(R.string.Tools_screenshot_saved) + path);
		} catch (Exception e) {
			ToolsTracker.error("Tools.takeScreenshot", e, e.getMessage());
		}
	}
	
	public static void installSampleSongs(Activity a) {
		String samplePath =
				getAppDir() + "/" +
			res.getString(R.string.Tools_sample_zip);
		new ToolsSampleInstaller(
				a, samplePath, R.raw.samples, R.string.ToolsSampleInstaller_installing
				).extract();
	}

	/*
	public static void installGraphics(Activity a) {
		String appPath = getAppDir();
		if (appPath != null) {
			String graphicsPath = 
				appPath + "/" +
				res.getString(R.string.Tools_graphics_zip)
				;
			new ToolsSampleInstaller(
					a, graphicsPath, R.raw.graphics, R.string.ToolsSampleInstaller_graphics
					).extract();
		}
	}
	*/
	
	// Copied from http://www.rgagnon.com/javadetails/java-0416.html
	private static byte[] createChecksum(String filename) throws Exception {
		FileInputStream in =  new FileInputStream(filename);
		byte[] buffer = new byte[BUFFER];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;
		do {
			numRead = in.read(buffer);
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);
		in.close();
		return complete.digest();
	}
	
	public static String getMD5Checksum(String filename) {
		try {
			byte[] b = createChecksum(filename);
			StringBuilder result = new StringBuilder();
			for (byte value : b) {
				result.append(Integer.toString((value & 0xff) + 0x100, 16).substring(1));
			}
			return result.toString();
		} catch (Exception e) {
			ToolsTracker.error("Tools.getMD5Checksum", e, c.getLocalClassName());
			return null;
		}
	}
}
