package com.frogobox.rythmtap.ui.game;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.frogobox.rythmtap.model.DataNotesData;
import com.frogobox.rythmtap.model.DataParser;
import com.frogobox.rythmtap.service.MusicService;
import com.frogobox.rythmtap.util.Randomizer;
import com.frogobox.rythmtap.util.Tools;
import com.frogobox.rythmtap.util.ToolsTracker;
import com.frogobox.rythmtap.widget.GUIGame;
import com.frogobox.rythmtap.R;

import java.io.File;

public class GameActivity implements Runnable {
    private final Activity activity;
    private final String title;

    private int defaultDifficulty;
    private int availableDifficulty;
    private String smFilePath;
    private ProgressDialog loadingDialog;

    public static DataParser dp;

    // Thread
    private String errorMessage = "";

    public GameActivity(Activity activity, String title) {
        this.activity = activity;
        this.title = title;

        defaultDifficulty = 0;
        availableDifficulty = 0;
        smFilePath = null;
        loadingDialog = null;
    }

    // Title Bar
    public void setTitle() {
        try {
            if (loadingDialog != null && loadingDialog.isShowing()) loadingDialog.cancel();
            activity.setTitle(title);
        } catch (IllegalArgumentException e) {
            ToolsTracker.error("MenuStartGame.setTitle", e, smFilePath);
            if (Tools.getBooleanSetting(R.string.debugLogCat, R.string.debugLogCatDefault)) {
                Tools.toast(Tools.getString(R.string.Tools_window_error));
            }
        }
    }

    public void showLoadingDialog() {
        String smFileName = smFilePath;
        if (smFilePath.contains("/")) {
            smFileName = smFilePath.substring(smFilePath.lastIndexOf('/') + 1);
        }
        activity.setTitle(Tools.getString(R.string.MenuStartGame_loading_title) + smFileName);
        loadingDialog = ProgressDialog.show(activity, null, Tools.getString(R.string.MenuStartGame_loading_progress_bar) + smFileName, true);
    }

    private void showFailParseMsg() {
        Tools.error(Tools.getString(R.string.MenuStartGame_fail_parse) + smFilePath + Tools.getString(R.string.Tools_error_msg) + errorMessage, Tools.cancel_action);
    }

    private void startGameActivity() {
        setTitle();
        activity.startActivity(new Intent(activity, GUIGame.class));
    }

    private void checkOGG() {
        String musicFilePath = dp.df.getMusic().getPath();
        if (Tools.isOGGFile(musicFilePath) &&
                !Tools.getBooleanSetting(R.string.ignoreOGGWarning, R.string.ignoreOGGWarningDefault) &&
                Integer.parseInt(Tools.getSetting(R.string.manualOGGOffset, R.string.manualOGGOffsetDefault)) == 0
        ) {
            // OGG warning
            DialogInterface.OnClickListener start_action = (dialog, id) -> {
                dialog.cancel();
                startGameActivity();
            };

            DialogInterface.OnClickListener cancel_action = (dialog, id) -> {
                dialog.cancel();
                setTitle();
            };

            Tools.warning(Tools.getString(R.string.MenuStartGame_ogg_warning), start_action, cancel_action, R.string.ignoreOGGWarning);
            ToolsTracker.info("OGG song loaded");
        } else {
            startGameActivity();
        }
    }

    private void checkDifficulty() {
        // Different difficulty warning
        if (availableDifficulty != defaultDifficulty &&
                !Tools.getBooleanSetting(R.string.ignoreDifficultyWarning, R.string.ignoreDifficultyWarningDefault)
        ) {
            DialogInterface.OnClickListener start_action = (dialog, id) -> {
                dialog.cancel();
                checkOGG();
            };

            DialogInterface.OnClickListener cancel_action = (dialog, id) -> {
                dialog.cancel();
                setTitle();
            };

            Tools.warning(
                    Tools.getString(R.string.MenuStartGame_difficulty_selected) +
                            DataNotesData.Difficulty.values()[defaultDifficulty].toString() +
                            Tools.getString(R.string.MenuStartGame_difficulty_closest) +
                            DataNotesData.Difficulty.values()[availableDifficulty].toString() +
                            Tools.getString(R.string.MenuStartGame_difficulty_continue),
                    start_action,
                    cancel_action,
                    R.string.ignoreDifficultyWarning
            );
        } else {
            checkOGG();
        }
    }

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            //case -1:
            if (msg.what == 0) {
                checkDifficulty();
            } else {
                showFailParseMsg();
                setTitle();
            }
        }
    };

    public void run() {
        try {
            // Parse stepfile
            dp = new DataParser(smFilePath);
            defaultDifficulty = Integer.parseInt(Tools.getSetting(R.string.difficultyLevel, R.string.difficultyLevelDefault));
            for (int i = dp.df.notesData.size(); i > 0; i--) {
                availableDifficulty = dp.df.notesData.get(i - 1).getDifficulty().ordinal();
                if (availableDifficulty <= defaultDifficulty) {
                    dp.setNotesDataIndex(i - 1);break;
                }
            }

            // Load notes
            boolean jumps = Tools.getBooleanSetting(R.string.jumps, R.string.jumpsDefault);
            boolean holds = Tools.getBooleanSetting(R.string.holds, R.string.holdsDefault);
            boolean randomize = Integer.parseInt(Tools.getSetting(R.string.randomize, R.string.randomizeDefault)) != Randomizer.OFF;
            dp.loadNotes(jumps, holds, randomize);

            // Sanity checks
            if (!dp.hasNext()) {
                throw new Exception(Tools.getString(R.string.MenuStartGame_notes_error));
            }
            String musicFilePath = dp.df.getMusic().getPath();
            if (dp.df.getMusic() == null ||
                    dp.df.getMusic().getPath().length() == 0 ||
                    !dp.df.getMusic().exists() ||
                    !dp.df.getMusic().canRead()
            ) {
                throw new Exception(Tools.getString(R.string.MenuStartGame_music_error));
            }
            // Ensure it's a valid/supported song format
            try {
                new MusicService(musicFilePath);
            } catch (Exception e) {
                throw new Exception(Tools.getString(R.string.MenuStartGame_music_error));
            }
            handler.sendEmptyMessage(0); // Done parsing
        } catch (Exception e) {
            ToolsTracker.error("MenuStartGame.run", e, smFilePath);
            errorMessage = e.getMessage();
            handler.sendEmptyMessage(-1); // Fail
        }
    }

    // Start Game Checks
    private void startGame() {
        // Set the screen dimensions
        // Have to be called outside of an onCreate for some reason...
        Tools.setTopbarHeight(activity);
        Tools.setScreenDimensions();
        // Run thread
        showLoadingDialog();
        new Thread(this).start();
    }

    public void startGameCheck() {
        smFilePath = Tools.getSetting(R.string.smFilePath, R.string.smFilePathDefault);

        if (smFilePath.length() < 2) {
            Tools.warning(Tools.getString(R.string.MenuStartGame_select_music), Tools.cancel_action, -1);
        } else if (!(new File(smFilePath).exists())) {
            Tools.error(Tools.getString(R.string.MenuStartGame_missing_sm) + smFilePath + Tools.getString(R.string.MenuStartGame_choose_new), Tools.cancel_action);
        } else {
            startGame();
        }
    }

}
