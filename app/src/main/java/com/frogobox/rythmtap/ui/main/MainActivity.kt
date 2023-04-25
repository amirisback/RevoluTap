package com.frogobox.rythmtap.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.media.AudioManager
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.TextView
import com.frogobox.rythmtap.R
import com.frogobox.rythmtap.common.core.BaseActivity
import com.frogobox.rythmtap.common.ext.setOnClickListenerExt
import com.frogobox.rythmtap.common.ext.vibrate
import com.frogobox.rythmtap.databinding.ActivityMainBinding
import com.frogobox.rythmtap.ui.filechooser.FileChooserActivity
import com.frogobox.rythmtap.ui.game.GameActivity
import com.frogobox.rythmtap.ui.settings.SettingsActivity
import com.frogobox.rythmtap.util.Tools
import com.frogobox.rythmtap.util.ToolsSaveFile
import com.frogobox.sdk.ext.startActivityExt
import java.util.Locale

class MainActivity : BaseActivity<ActivityMainBinding>() {

    // Private variables
    private val title = ""
    private var largeText = false
    private val largeTextCountries = arrayOf("ko", "zh", "ru", "ja", "tr")

    companion object {
        private const val SELECT_MUSIC = 123
        private const val SELECT_SONG_PACK = 122
        private const val MENU_FONT = "fonts/Square.ttf"
        private var defaultLocale: Locale? = null
        private val viewIds = intArrayOf(
            R.id.start,
            R.id.select_song,
            R.id.settings,
            R.id.btn_download,
            R.id.difficulty,
            R.id.gameMode
        )
    }

    override fun setupViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_MUSIC && resultCode == RESULT_OK) {
            if (Tools.getBooleanSetting(R.string.autoStart, R.string.autoStartDefault)) {
                Tools.setContext(this)
                GameActivity(this, title).startGameCheck()
            }
        }
        if (requestCode == SELECT_SONG_PACK && resultCode == RESULT_OK) {
            ToolsSaveFile.installSongPackFromIntent(this, data)
        }
    }

    // Update displayed language
    private fun updateLanguage() {
        if (defaultLocale == null) {
            defaultLocale = this.resources.configuration.locale
        }
        val languageToLoad = Tools.getSetting(R.string.language, R.string.languageDefault)
        if (languageToLoad == "default") {
            val config = Configuration()
            config.locale = defaultLocale
            this.resources.updateConfiguration(config, null)
        } else {
            val locale = Locale(languageToLoad)
            Locale.setDefault(locale)
            val config = Configuration()
            config.locale = locale
            this.resources.updateConfiguration(config, null)
        }

        // For non-roman alphabets
        val language = this.resources.configuration.locale.language
        largeText = false
        for (country in largeTextCountries) {
            if (language.startsWith(country)) {
                largeText = true
                break
            }
        }
    }

    // Update layout images
    private fun updateLayout() {
        updateLanguage()

        // Menu items
        formatMenuItem(binding.start, R.string.Menu_start)
        formatMenuItem(binding.selectSong, R.string.Menu_select_song)
        formatMenuItem(binding.settings, R.string.Menu_settings)
        binding.btnDownload?.let { formatMenuItem(it, R.string.Menu_download_songs) }
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
        if (Tools.getBooleanSetting(
                R.string.additionalVibrations,
                R.string.additionalVibrationsDefault
            )
        ) {
            vibrate(this, 300)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun formatMenuItem(tv: TextView, text: Int) {
        val tf = Typeface.createFromAsset(assets, MENU_FONT)
        var textSize = 40f
        if (largeText) {
            textSize += 6f
        }
        if (Tools.tablet) {
            textSize += 26f
        }
        //textSize = Tools.scale(textSize);
        if (largeText) {
            //tv.setTypeface(tf, Typeface.BOLD);
            tv.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD)
        } else {
            tv.typeface = tf
        }
        tv.textSize = textSize
        tv.setTextColor(Color.BLACK)
        tv.setShadowLayer(5f, 0f, 0f, Color.WHITE)
        tv.gravity = Gravity.CENTER
        // We do this instead of ColorStateList since ColorStateList doesn't deal with shadows
        tv.onFocusChangeListener = OnFocusChangeListener { v: View?, hasFocus: Boolean ->
            if (hasFocus) {
                tv.setTextColor(Color.WHITE)
                tv.setShadowLayer(9f, 0f, 0f, Color.BLACK)
            } else {
                tv.setTextColor(Color.BLACK)
                tv.setShadowLayer(7f, 0f, 0f, Color.WHITE)
            }
        }
        tv.setOnTouchListener { v: View?, e: MotionEvent ->
            if (e.action == MotionEvent.ACTION_DOWN) {
                tv.setTextColor(Color.WHITE)
                tv.setShadowLayer(9f, 0f, 0f, Color.BLACK)
            } else if (e.action == MotionEvent.ACTION_UP) {
                tv.setTextColor(Color.BLACK)
                tv.setShadowLayer(7f, 0f, 0f, Color.WHITE)
            }
            false
        }
        tv.setText(text)
    }

    private fun setupLayout() {
        volumeControlStream = AudioManager.STREAM_MUSIC // To control media volume at all times
        updateLayout()
        setupUI()

        // Setup navigation for TVs/keyboard
        setupDpadNavigation()
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
                GameActivity(this@MainActivity, title).startGameCheck()
            }

            // Select Song button
            selectSong.setOnClickListenerExt {
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

    private fun setupDpadNavigation() {
        for (i in viewIds.indices) {
            val view = findViewById<View>(viewIds[i])
            view.isFocusable = true
            var upIndex = i - 1
            var downIndex = i + 1
            if (i == 0) {
                upIndex = viewIds.size - 1
            } else if (i == viewIds.size - 1) {
                downIndex = 0
            }
            view.nextFocusUpId = viewIds[upIndex]
            view.nextFocusLeftId = viewIds[upIndex]
            view.nextFocusDownId = viewIds[downIndex]
            view.nextFocusRightId = viewIds[downIndex]
        }
        setupInitialFocus()
    }

    private fun setupInitialFocus() {
        val firstView = findViewById<View>(viewIds[0])
        firstView?.requestFocus()
    }

    public override fun onResume() {
        super.onResume()
        setupInitialFocus()
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

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun toggleAutoPlay() {
        var autoPlay = Tools.getBooleanSetting(R.string.autoPlay, R.string.autoPlayDefault)
        autoPlay = !autoPlay
        Tools.putSetting(R.string.autoPlay, (if (autoPlay) 1 else 0).toString())
        updateAutoPlay()
    }

    private fun updateAutoPlay() {
        // Header font
        val tf = Typeface.createFromAsset(assets, MENU_FONT)
        var textSize = 25f
        if (largeText) {
            textSize += 3f
        }
        if (Tools.tablet) {
            textSize += 20f
        }

        // AutoPlay header
        binding.autoPlay.apply {
            if (largeText) {
                //autoPlay.setTypeface(tf, Typeface.BOLD);
                setTypeface(Typeface.SANS_SERIF, Typeface.BOLD)
            } else {
                typeface = tf
            }
            if (Tools.getBooleanSetting(R.string.autoPlay, R.string.autoPlayDefault)) {
                paintFlags = 0
                //setText(Tools.getString(R.string.Menu_auto));
            } else {
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                //setText("        ");
            }
            textSize = textSize
        }

    }

    // Ugly, won't fix
    private fun nextDifficulty() {
        var difficulty =
            Tools.getSetting(R.string.difficultyLevel, R.string.difficultyLevelDefault).toInt()
        difficulty++
        if (difficulty > 4) difficulty = 0
        Tools.putSetting(R.string.difficultyLevel, Integer.toString(difficulty))
        updateDifficulty()
    }

    private fun updateDifficulty() {
        // Header font
        val tf = Typeface.createFromAsset(assets, MENU_FONT)
        var textSize = 25f
        if (largeText) {
            textSize += 3f
        }
        if (Tools.tablet) {
            textSize += 20f
        }
        //textSize = Tools.scale(textSize);

        // Difficulty header
        binding.difficulty.apply {
            if (largeText) {
                //difficulty.setTypeface(tf, Typeface.BOLD);
                setTypeface(Typeface.SANS_SERIF, Typeface.BOLD)
            } else {
                typeface = tf
            }
            textSize = textSize
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