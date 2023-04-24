package com.github.budsterblue.revolutap.util;

import android.app.Activity;
import android.content.Intent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class ToolsSaveFile {
    public static File createFileFromInputStream(InputStream inputStream, String fileName) {

        try {
            File f = new File(fileName);
            OutputStream outputStream = new FileOutputStream(f);
            byte[] buffer = new byte[1024];
            int length;

            while((length=inputStream.read(buffer)) > 0) {
                outputStream.write(buffer,0,length);
            }

            outputStream.close();
            inputStream.close();

            return f;
        } catch (IOException e) {
            ToolsTracker.error("ToolsSaveFile.createFileFromInputStream", e, fileName);
        }

        return null;
    }

    public static void installSongPackFromIntent(Activity a, Intent intent) {
        try {
            if (intent != null && intent.getData() != null) {
                InputStream inputStream = a.getApplicationContext().getContentResolver().openInputStream(intent.getData());
                if (inputStream != null) {
                    new ToolsUnzipper(a, Objects.requireNonNull(ToolsSaveFile.createFileFromInputStream(inputStream,
                            Tools.getAppDir() + "/imported_songpack.zip")).getAbsolutePath(), false).unzip();
                }
            }
        } catch (FileNotFoundException e) {
            ToolsTracker.error("ToolsSaveFile.installSongPackFromIntent", e, intent.toString());
        }
    }
}
