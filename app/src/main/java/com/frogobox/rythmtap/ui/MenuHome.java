package com.frogobox.rythmtap.ui;

import androidx.annotation.NonNull;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.frogobox.rythmtap.util.Tools;
import com.frogobox.rythmtap.util.ToolsSaveFile;
import com.frogobox.rythmtap.R;

import java.util.Locale;


public class MenuHome extends Activity {
	
	private static final int SELECT_MUSIC = 123;
	private static final int SELECT_SONG_PACK = 122;
	private static final String MENU_FONT = "fonts/Square.ttf";

	// Private variables
	private final String title = "";
	private boolean largeText = false;
	private final String[] largeTextCountries= {"ko", "zh", "ru", "ja", "tr"};
	private static Locale defaultLocale;
	private Vibrator v;
	
	// Activity Result
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if (requestCode == SELECT_MUSIC && resultCode == RESULT_OK) {
			if (Tools.getBooleanSetting(R.string.autoStart, R.string.autoStartDefault)) {
				Tools.setContext(this);
				new MenuStartGame(this, title).startGameCheck();
			}
		}
		if (requestCode == SELECT_SONG_PACK && resultCode == Activity.RESULT_OK) {
				ToolsSaveFile.installSongPackFromIntent(this, data);
		}
	}
	
	// Update displayed language
	public void updateLanguage() {
		if (defaultLocale == null) {
			defaultLocale = this.getResources().getConfiguration().locale;
		}
		
		String languageToLoad = Tools.getSetting(R.string.language, R.string.languageDefault);
		if (languageToLoad.equals("default")) {
			Configuration config = new Configuration();
			config.locale = defaultLocale;
			this.getResources().updateConfiguration(config, null);
		} else {
			Locale locale = new Locale(languageToLoad);
			Locale.setDefault(locale);
			Configuration config = new Configuration();
			config.locale = locale;
			this.getResources().updateConfiguration(config, null);
		}
		
		// For non-roman alphabets
		String language = this.getResources().getConfiguration().locale.getLanguage();
		largeText = false;
		for (String country : largeTextCountries) {
			if (language.startsWith(country)) {
				largeText = true;
				break;
			}
		}
	}
	
	// Update layout images
	private void updateLayout() {
		updateLanguage();
		
		// Menu items
		formatMenuItem(findViewById(R.id.start), R.string.Menu_start);
		formatMenuItem(findViewById(R.id.select_song), R.string.Menu_select_song);
		formatMenuItem(findViewById(R.id.settings), R.string.Menu_settings);
		formatMenuItem(findViewById(R.id.download_songs), R.string.Menu_download_songs);
		
		updateDifficulty();
		updateAutoPlay();
		updateGameMode();
	}
	
	// Main screen
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Tools.setContext(this);
		
		// Startup checks
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		if (Tools.getBooleanSetting(R.string.resetSettings, R.string.resetSettingsDefault)) {
			Tools.resetSettings();
		}
		Tools.setScreenDimensions();
		setupLayout();

		if (Tools.getBooleanSetting(R.string.installSamples, R.string.installSamplesDefault)) {
			// Make folders and install sample songs
			Tools.installSampleSongs(this);
			Tools.putSetting(R.string.installSamples, "0");
		}
		
		if (Tools.getBooleanSetting(R.string.additionalVibrations, R.string.additionalVibrationsDefault)) {
			v = ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE));
			v.vibrate(300); // ready to rumble!
		}
	}
	
	@SuppressLint("ClickableViewAccessibility")
	private void formatMenuItem(final TextView tv, int text) {
		Typeface tf = Typeface.createFromAsset(getAssets(), MENU_FONT);
		float textSize = 40f;
		if (largeText) {
			textSize += 6f;
		}
		if (Tools.tablet) {
			textSize += 26;
		}
		//textSize = Tools.scale(textSize);
		if (largeText) {
			//tv.setTypeface(tf, Typeface.BOLD);
			tv.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
		} else {
			tv.setTypeface(tf);
		}
		tv.setTextSize(textSize);
		tv.setTextColor(Color.BLACK);
		tv.setShadowLayer(5f, 0, 0, Color.WHITE);
		tv.setGravity(Gravity.CENTER);
		// We do this instead of ColorStateList since ColorStateList doesn't deal with shadows
		tv.setOnFocusChangeListener((v, hasFocus) -> {
			if (hasFocus) {
				tv.setTextColor(Color.WHITE);
				tv.setShadowLayer(9f, 0, 0, Color.BLACK);
			} else {
				tv.setTextColor(Color.BLACK);
				tv.setShadowLayer(7f, 0, 0, Color.WHITE);
			}
		});
		tv.setOnTouchListener((v, e) -> {
			if (e.getAction() == MotionEvent.ACTION_DOWN) {
				tv.setTextColor(Color.WHITE);
				tv.setShadowLayer(9f, 0, 0, Color.BLACK);
			} else if (e.getAction() == MotionEvent.ACTION_UP) {
				tv.setTextColor(Color.BLACK);
				tv.setShadowLayer(7f, 0, 0, Color.WHITE);
			}
			return false;
		});
		tv.setText(text);
	}
	
	private void vibrate() {
		if (v != null) {
			v.vibrate(20);
		}
	}
	
	private void setupLayout() {
		setContentView(R.layout.main);
		setVolumeControlStream(AudioManager.STREAM_MUSIC); // To control media volume at all times

		updateLayout();
		
		// Difficulty button
		final TextView difficulty = findViewById(R.id.difficulty);
		difficulty.setOnClickListener(v -> {
			vibrate();
			nextDifficulty();
		});
		difficulty.setOnFocusChangeListener((v, hasFocus) -> {
			if (hasFocus) {
				difficulty.setBackgroundColor(Color.BLACK);
			} else {
				// Using the image increases the view's height and shifts the menu a bit,
				// so let's just forget about the background
				//difficulty.setBackgroundResource(R.drawable.difficulty_header);
				difficulty.setBackgroundColor(Color.TRANSPARENT);
			}
		});
		
		// AutoPlay button
		TextView autoPlay = findViewById(R.id.autoPlay);
		autoPlay.setTextColor(Color.RED);
		autoPlay.setShadowLayer(7f, 0, 0, Color.WHITE);
		autoPlay.setOnClickListener(v -> {
			vibrate();
			toggleAutoPlay();
		});
		
		// Game Mode
		final ImageView gameMode = findViewById(R.id.gameMode);
		gameMode.setOnClickListener(v -> {
			vibrate();
			toggleGameMode();
		});
		
		int maxHeight = Tools.button_h * 2 / 3;
		gameMode.setAdjustViewBounds(true);
		gameMode.setMaxHeight(maxHeight);
		
		gameMode.setOnFocusChangeListener((v, hasFocus) -> {
			if (hasFocus) {
				gameMode.setBackgroundColor(Color.BLACK);
			} else {
				gameMode.setBackgroundColor(Color.TRANSPARENT);
			}
		});
		
		// Start button
		TextView start_b = findViewById(R.id.start);
		start_b.setOnClickListener(v -> {
			vibrate();
			new MenuStartGame(MenuHome.this, title).startGameCheck();
		});
		
		// Select Song button
		TextView select_song_b = findViewById(R.id.select_song);
		select_song_b.setOnClickListener(v -> {
			vibrate();
			Intent i = new Intent();
			i.setClass(MenuHome.this, MenuFileChooser.class);
			startActivityForResult(i, SELECT_MUSIC);
		});

		// Settings button
		TextView settings_b = findViewById(R.id.settings);
		settings_b.setOnClickListener(v -> {
			vibrate();
			Intent i = new Intent();
			i.setClass(MenuHome.this, MenuSettings.class);
			startActivity(i);
		});

		// Download Songs button
		TextView download_songs_b = findViewById(R.id.download_songs);
		download_songs_b.setOnClickListener(v -> {
			vibrate();
			Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("*/*");
			startActivityForResult(intent, SELECT_SONG_PACK);
		});
		
		// Setup navigation for TVs/keyboard
		setupDpadNavigation();
	}
	
	private static final int[] viewIds = {
		R.id.start,
		R.id.select_song,
		R.id.settings,
		R.id.download_songs,
		R.id.difficulty,
		R.id.gameMode
	};
	
	private void setupDpadNavigation() {
		for (int i = 0; i < viewIds.length; i++) {
			View view = findViewById(viewIds[i]);
			view.setFocusable(true);
			
			int upIndex = i - 1;
			int downIndex = i + 1;
			if (i == 0) {
				upIndex = viewIds.length - 1;
			} else if (i == viewIds.length - 1) {
				downIndex = 0;
			}
			view.setNextFocusUpId(viewIds[upIndex]);
			view.setNextFocusLeftId(viewIds[upIndex]);
			view.setNextFocusDownId(viewIds[downIndex]);
			view.setNextFocusRightId(viewIds[downIndex]);
		}
		
		setupInitialFocus();
	}
	
	public void setupInitialFocus() {
		View firstView = findViewById(viewIds[0]);
		if (firstView != null) {
			firstView.requestFocus();
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		setupInitialFocus();
	}
	
	@Override
	public void onWindowFocusChanged (boolean hasFocus) {
		if (hasFocus) {
			Tools.setContext(this);
			updateLayout();
		}
	}
	
	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		updateLayout();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void toggleAutoPlay() {
		boolean autoPlay = Tools.getBooleanSetting(R.string.autoPlay, R.string.autoPlayDefault);
		autoPlay = !autoPlay;
		Tools.putSetting(R.string.autoPlay, String.valueOf(autoPlay ? 1 : 0));
		updateAutoPlay();
	}
	
	private void updateAutoPlay() {
		// Header font
		Typeface tf = Typeface.createFromAsset(getAssets(), MENU_FONT);
		float textSize = 25f;
		if (largeText) {
			textSize += 3f;
		}
		if (Tools.tablet) {
			textSize += 20;
		}
		
		// AutoPlay header
		TextView autoPlay = findViewById(R.id.autoPlay);
		if (largeText) {
			//autoPlay.setTypeface(tf, Typeface.BOLD);
			autoPlay.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
		} else {
			autoPlay.setTypeface(tf);
		}
		if (Tools.getBooleanSetting(R.string.autoPlay, R.string.autoPlayDefault)) {
			autoPlay.setPaintFlags(0);
			//autoPlay.setText(Tools.getString(R.string.Menu_auto));
		} else {
			autoPlay.setPaintFlags(autoPlay.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			//autoPlay.setText("        ");
		}
		autoPlay.setTextSize(textSize);
	}
	
	// Ugly, won't fix
	private void nextDifficulty() {
		int difficulty = Integer.parseInt(Tools.getSetting(R.string.difficultyLevel, R.string.difficultyLevelDefault));
		difficulty++;
		if (difficulty > 4) difficulty = 0;
		Tools.putSetting(R.string.difficultyLevel, Integer.toString(difficulty));
		updateDifficulty();
	}
	
	private void updateDifficulty() {
		// Header font
		Typeface tf = Typeface.createFromAsset(getAssets(), MENU_FONT);
		float textSize = 25f;
		if (largeText) {
			textSize += 3f;
		}
		if (Tools.tablet) {
			textSize += 20;
		}
		//textSize = Tools.scale(textSize);
		
		// Difficulty header
		TextView difficulty = findViewById(R.id.difficulty);
		if (largeText) {
			//difficulty.setTypeface(tf, Typeface.BOLD);
			difficulty.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
		} else {
			difficulty.setTypeface(tf);
		}
		difficulty.setTextSize(textSize);
		switch (Integer.parseInt(
				Tools.getSetting(R.string.difficultyLevel, R.string.difficultyLevelDefault)
				)) {
			case 0:
				difficulty.setText(" " + Tools.getString(R.string.Difficulty_beginner).toLowerCase());
				difficulty.setTextColor(Color.rgb(255, 132, 0)); // orange
				break;
			case 1:
				difficulty.setText(" " + Tools.getString(R.string.Difficulty_easy).toLowerCase());
				difficulty.setTextColor(Color.rgb(0, 185, 255)); // light blue
				break;
			case 2:
				difficulty.setText(" " + Tools.getString(R.string.Difficulty_medium).toLowerCase());
				difficulty.setTextColor(Color.rgb(255, 0, 0)); // red
				break;
			case 3:
				difficulty.setText(" " + Tools.getString(R.string.Difficulty_hard).toLowerCase());
				difficulty.setTextColor(Color.rgb(32, 185, 32)); // green
				break;
			case 4:
				difficulty.setText(" " + Tools.getString(R.string.Difficulty_challenge).toLowerCase());
				difficulty.setTextColor(Color.rgb(14, 122, 230)); // dark blue
				break;
		}
	}

	private void toggleGameMode() {
		int gameMode = Integer.parseInt(Tools.getSetting(R.string.gameMode, R.string.gameModeDefault));
		gameMode += 1;
		if (gameMode == 2) gameMode = 0;
		Tools.putSetting(R.string.gameMode, Integer.toString(gameMode));
		updateGameMode();
	}

	private void updateGameMode() {
		Tools.updateGameMode();
		ImageView gameMode = findViewById(R.id.gameMode);
		switch(Tools.gameMode) {
			case Tools.REVERSE:
				gameMode.setImageResource(R.drawable.mode_step_down);
				break;
			case Tools.STANDARD:
				gameMode.setImageResource(R.drawable.mode_step_up);
				break;
		}
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/*
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { // Backward compatibility
			backgroundDataUncheck();
			return true;
		}
		*/
		switch (keyCode) {
			case KeyEvent.KEYCODE_MENU:
				//changeDifficulty();
				Intent i = new Intent();
				i.setClass(MenuHome.this, MenuSettings.class);
				startActivity(i);
				return true;
			case KeyEvent.KEYCODE_SEARCH:
				Tools.startWebsiteActivity(Tools.getString(R.string.Url_downloads));
				return true;
			default:
				return super.onKeyDown(keyCode, event);
		}
	}

}
