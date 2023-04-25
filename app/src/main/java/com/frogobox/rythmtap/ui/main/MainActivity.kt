package com.frogobox.rythmtap.ui.main

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Paint
import android.media.AudioManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import com.frogobox.rythmtap.R
import com.frogobox.rythmtap.common.core.BaseBindActivity
import com.frogobox.rythmtap.common.ext.setOnClickListenerExt
import com.frogobox.rythmtap.common.ext.vibrate
import com.frogobox.rythmtap.databinding.ActivityMainBinding
import com.frogobox.rythmtap.ui.filechooser.FileChooserActivity
import com.frogobox.rythmtap.ui.game.InitGameActivity
import com.frogobox.rythmtap.ui.settings.SettingsActivity
import com.frogobox.rythmtap.util.Tools
import com.frogobox.rythmtap.util.ToolsSaveFile
import com.frogobox.sdk.ext.startActivityExt
import java.util.Locale

class MainActivity : BaseBindActivity<ActivityMainBinding>() {

    // Private variables
    private val title = ""

    companion object {
        private const val SELECT_MUSIC = 123
        private const val SELECT_SONG_PACK = 122
    }

    override fun setupViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_MUSIC && resultCode == RESULT_OK) {
            if (Tools.getBooleanSetting(R.string.autoStart, R.string.autoStartDefault)) {
                Tools.setContext(this)
                InitGameActivity(this, title).startGameCheck()
            }
        }
        if (requestCode == SELECT_SONG_PACK && resultCode == RESULT_OK) {
            ToolsSaveFile.installSongPackFromIntent(this, data)
        }
    }

    // Update layout images
    private fun updateLayout() {
        updateDifficulty()
        updateAutoPlay()
        updateGameMode()
    }

    // Main screen
    override fun onCreateExt(savedInstanceState: Bundle?) {
        super.onCreateExt(savedInstanceState)
        setupHideSystemUI()
        // Startup checks
        Tools.setContext(this)
        if (Tools.getBooleanSetting(R.string.resetSettings, R.string.resetSettingsDefault)) {
            Tools.resetSettings()
        }
        Tools.setScreenDimensions()
        setupLayout()
        if (Tools.getBooleanSetting(R.string.installSamples, R.string.installSamplesDefault)) {
            // Make folders and install sample songs
            Tools.installSampleSongs(this)
            Tools.putSetting(R.string.installSamples, "0")
        }
        if (Tools.getBooleanSetting(R.string.additionalVibrations, R.string.additionalVibrationsDefault)) {
            vibrate(this, 300)
        }
    }

    private fun setupLayout() {
        volumeControlStream = AudioManager.STREAM_MUSIC // To control media volume at all times
        updateLayout()
        setupUI()
    }

    private fun setupUI() {
        binding.apply {

            // Difficulty
            difficulty.apply {
                setOnClickListenerExt {
                    nextDifficulty()
                }
                onFocusChangeListener = OnFocusChangeListener { v: View?, hasFocus: Boolean ->
                    if (hasFocus) {
                        setBackgroundColor(Color.BLACK)
                    } else {
                        // Using the image increases the view's height and shifts the menu a bit,
                        // so let's just forget about the background
                        //difficulty.setBackgroundResource(R.drawable.difficulty_header);
                        setBackgroundColor(Color.TRANSPARENT)
                    }
                }
            }

            // AutoPlay button
            autoPlay.apply {
                setTextColor(Color.RED)
                setShadowLayer(7f, 0f, 0f, Color.WHITE)
                setOnClickListenerExt {
                    toggleAutoPlay()
                }
            }

            // Game Mode
            gameMode.apply {
                setOnClickListenerExt {
                    toggleGameMode()
                }
                val maxHeight = Tools.button_h * 2 / 3
                this.maxHeight = maxHeight
                adjustViewBounds = true
                onFocusChangeListener = OnFocusChangeListener { v: View?, hasFocus: Boolean ->
                    if (hasFocus) {
                        setBackgroundColor(Color.BLACK)
                    } else {
                        setBackgroundColor(Color.TRANSPARENT)
                    }
                }
            }

            // Start button
            start.setOnClickListenerExt {
                InitGameActivity(
                    this@MainActivity,
                    title
                ).startGameCheck()
            }

            // Select Song button
            selectSong.setOnClickListener {
                startActivityForResult(
                    Intent(this@MainActivity, FileChooserActivity::class.java),
                    SELECT_MUSIC
                )
            }
            // Settings button
            settings.setOnClickListenerExt {
                startActivityExt<SettingsActivity>()
            }

            // Download Songs button
            btnDownload?.setOnClickListenerExt {
                startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }, SELECT_SONG_PACK)
            }

        }
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            Tools.setContext(this)
            updateLayout()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateLayout()
    }

    private fun toggleAutoPlay() {
        var autoPlay = Tools.getBooleanSetting(R.string.autoPlay, R.string.autoPlayDefault)
        autoPlay = !autoPlay
        Tools.putSetting(R.string.autoPlay, (if (autoPlay) 1 else 0).toString())
        updateAutoPlay()
    }

    private fun updateAutoPlay() {

        // AutoPlay header
        binding.autoPlay.paintFlags = if (Tools.getBooleanSetting(R.string.autoPlay, R.string.autoPlayDefault)) {
            0
        } else {
            Paint.STRIKE_THRU_TEXT_FLAG
        }

    }

    // Ugly, won't fix
    private fun nextDifficulty() {
        var difficulty =
            Tools.getSetting(R.string.difficultyLevel, R.string.difficultyLevelDefault).toInt()
        difficulty++
        if (difficulty > 4) difficulty = 0
        Tools.putSetting(R.string.difficultyLevel, difficulty.toString())
        updateDifficulty()
    }

    private fun updateDifficulty() {

        // Difficulty header
        binding.difficulty.apply {
            when (Tools.getSetting(R.string.difficultyLevel, R.string.difficultyLevelDefault).toInt()) {
                0 -> {
                    text = Tools.getString(R.string.Difficulty_beginner).lowercase()
                    setTextColor(Color.rgb(255, 132, 0)) // orange
                }

                1 -> {
                    text = Tools.getString(R.string.Difficulty_easy).lowercase()
                    setTextColor(Color.rgb(0, 185, 255)) // light blue
                }

                2 -> {
                    text = Tools.getString(R.string.Difficulty_medium).lowercase()
                    setTextColor(Color.rgb(255, 0, 0)) // red
                }

                3 -> {
                    text = Tools.getString(R.string.Difficulty_hard).lowercase()
                    setTextColor(Color.rgb(32, 185, 32)) // green
                }

                4 -> {
                    text = Tools.getString(R.string.Difficulty_challenge).lowercase(Locale.getDefault())
                    setTextColor(Color.rgb(14, 122, 230)) // dark blue
                }
            }

        }

    }

    private fun toggleGameMode() {
        var gameMode = Tools.getSetting(R.string.gameMode, R.string.gameModeDefault).toInt()
        gameMode += 1
        if (gameMode == 2) gameMode = 0
        Tools.putSetting(R.string.gameMode, Integer.toString(gameMode))
        updateGameMode()
    }

    private fun updateGameMode() {
        Tools.updateGameMode()
        when (Tools.gameMode) {
            Tools.REVERSE -> binding.gameMode.setImageResource(R.drawable.mode_step_down)
            Tools.STANDARD -> binding.gameMode.setImageResource(R.drawable.mode_step_up)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        /*
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { // Backward compatibility
			backgroundDataUncheck();
			return true;
		}
		*/
        return when (keyCode) {
            KeyEvent.KEYCODE_MENU -> {
                //changeDifficulty();
                startActivityExt<SettingsActivity>()
                true
            }

            KeyEvent.KEYCODE_SEARCH -> {
                Tools.startWebsiteActivity(Tools.getString(R.string.Url_downloads))
                true
            }

            else -> super.onKeyDown(keyCode, event)
        }
    }

}