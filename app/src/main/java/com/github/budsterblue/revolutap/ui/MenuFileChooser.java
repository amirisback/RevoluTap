package com.github.budsterblue.revolutap.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.budsterblue.revolutap.R;
import com.github.budsterblue.revolutap.util.Tools;
import com.github.budsterblue.revolutap.util.ToolsTracker;
import com.github.budsterblue.revolutap.util.ToolsUnzipper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Scanner;

public class MenuFileChooser extends AppCompatActivity implements MenuFileArrayAdapter.ItemClickListener, MenuFileArrayAdapter.ItemLongClickListener {
    private MenuFileArrayAdapter adapter;
    private File cwd;
    private String selectedFilePath;
    private boolean useShortDirNames;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private DividerItemDecoration dividerItemDecoration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose);
        Tools.setContext(this);
        // set up the RecyclerView
        recyclerView = findViewById(R.id.choose_recycler);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        registerForContextMenu(recyclerView);
        dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        //Objects.requireNonNull(this.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        adapter = null;
        selectedFilePath = null;

        // Get last dir
        String prefLastDir = Tools.getSetting(R.string.lastDir, R.string.lastDirDefault);
        useShortDirNames = Tools.getBooleanSetting(R.string.useShortDirNames, R.string.useShortDirNamesDefault);

        if (prefLastDir.equals("")) {
            prefLastDir = Tools.getPacksDir();
        }
        cwd = new File(prefLastDir);
        if (prefLastDir.length() > 0 &&
                cwd.exists() &&
                cwd.getParentFile() != null &&
                !cwd.getPath().equals(Tools.getPacksDir())
        ) {
            cwd = cwd.getParentFile();
        } else {
            String[] browseLocationOrder = {
                    prefLastDir,
                    Tools.getPacksDir(),
            };
            for (String path: browseLocationOrder) {
                if (path != null) {
                    cwd = new File(path);
                    if (cwd.canRead() && cwd.isDirectory()) {
                        break;
                    }
                }
            }
        }
        ToolsTracker.data("Opened file browser", "cwd", cwd.getAbsolutePath());
        refresh();
    }

    // Show files
    private String shortDirName(String s) {
        String stripped = s;
        // "[name] song"
        if (s.charAt(0) == '[' && s.indexOf(']') != -1 && s.indexOf(']') < s.length() - 1) {
            stripped = s.substring(s.indexOf(']') + 1).trim();
            // "(name) song"
        } else if (s.charAt(0) == '(' && s.indexOf(')') != -1 && s.indexOf(')') < s.length() - 1) {
            stripped = s.substring(s.indexOf(')') + 1).trim();
            // "#### song"
        } else if (Character.isDigit(s.charAt(0))) {
            int i = 0;
            while (i < s.length() && s.charAt(i) != ' ' && !Character.isLetter(s.charAt(i))) {
                i++;
            }
            if (i < (s.length() - 1)) {
                stripped = s.substring(i).trim();
            }
        }
        if (stripped.length() > 0) {
            return stripped;
        } else {
            return s;
        }
    }

    //private void ls(File dir) {
    public void refresh() {
        File dir = cwd;
        if (dir == null) return;

        // remove /data/user/0/com.github.budsterblue.revolutap/files from title
        if (dir.getAbsolutePath().equals(Tools.getPacksDir())){
            setTitle(dir.getAbsolutePath().replace(Tools.getAppDir() + "/", ""));
            Objects.requireNonNull(this.getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        } else {
            setTitle(dir.getAbsolutePath().replace(Tools.getPacksDir() + "/", ""));
            Objects.requireNonNull(this.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        }
        // Get lists
        File[] l = dir.listFiles();
        ArrayList<MenuFileItem> dl = new ArrayList<>();
        ArrayList<MenuFileItem> fl = new ArrayList<>();

        // Populate list
        if (l != null){
            for (File f : l) {
                String s = f.getName();
                if (!s.startsWith(".")) {
                    if (f.isDirectory()) {
                        if (useShortDirNames) s = shortDirName(s);
                        dl.add(new MenuFileItem(s, f.getAbsolutePath(), true, f));
                    } else if (Tools.isStepfile(s) || Tools.isLink(s) || Tools.isStepfilePack(s) || Tools.isText(s)) {
                        fl.add(new MenuFileItem(s, f.getAbsolutePath(), false, f));
                    }
                }
            }
        }
        Collections.sort(dl);
        Collections.sort(fl);
        dl.addAll(fl); // Add file list to end of directories list

        // Display
        if (adapter == null) {
            adapter = new MenuFileArrayAdapter(this, dl);
            adapter.setClickListener(this);
            adapter.setLongClickListener(this);
            recyclerView.addItemDecoration(dividerItemDecoration);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateData(dl);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        MenuFileItem i = adapter.getItem(position);
        if (i != null) {
            onFileClick(i);
        }
    }

    @Override
    public void onItemLongClick(View view, int position) {
        MenuFileItem i = adapter.getItem(position);
        assert i != null;
        selectedFilePath = i.getPath();

        DialogInterface.OnClickListener delete_action = (dialog, id) -> {
            try {
                deleteFile(new File(selectedFilePath));
            } catch (Exception e) {
                ToolsTracker.error("MenuFileChooser.deleteFile", e, selectedFilePath);
                Tools.error(
                        Tools.getString(R.string.MenuFilechooser_file_delete_error) +
                                selectedFilePath +
                                Tools.getString(R.string.Tools_error_msg) +
                                e.getMessage(),
                        Tools.cancel_action);
            }
            adapter.removeItem(position);
            dialog.cancel();
        };

        Tools.alert(
                Tools.getString(R.string.MenuFilechooser_file_delete),
                R.drawable.ic_delete_forever_filled_black,
                Tools.getString(R.string.MenuFilechooser_file_delete_confirm) +
                        i.getName(),
                Tools.getString(R.string.Button_yes),
                delete_action,
                Tools.getString(R.string.Button_no),
                Tools.cancel_action,
                -1
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onFileClick(new MenuFileItem("", cwd.getParent(), true, null));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String parseURL(File url) {
        Scanner sc = null;
        try {
            sc = new Scanner(url);
            String buffer;
            while (sc.hasNextLine()) {
                buffer = sc.nextLine();
                if (buffer.contains("URL=")) {
                    sc.close();
                    return (buffer.substring(buffer.indexOf("URL=") + 4)).trim();
                }
            }
        } catch (Exception e) {
            ToolsTracker.error("MenuFileChooser.parseURL", e, url.getAbsolutePath());
        }
        if (sc != null) sc.close();
        return null;
    }

    private void selectStepfile(String smFilePath) {
        // Save preferences
        // smFilePath = path
        Tools.putSetting(R.string.smFilePath, smFilePath);
        Tools.putSetting(R.string.lastDir, cwd.getPath());

        String smFileName;
        if (smFilePath.lastIndexOf('/') != -1) {
            smFileName = smFilePath.substring(smFilePath.lastIndexOf('/') + 1);
        } else {
            smFileName = smFilePath;
        }


        if (!Tools.getBooleanSetting(R.string.autoStart, R.string.autoStartDefault)) {
            Tools.toast(
                    Tools.getString(R.string.MenuFilechooser_selected_stepfile) +
                            smFileName +
                            Tools.getString(R.string.MenuFilechooser_start_info)
            );
        }
        setResult(RESULT_OK);
        finish();
    }

    private void displayTextFile(MenuFileItem i) {
		/*using html, because otherwise the text size is too big. I'm not sure why;
		the New User Notes box is plaintext and its text size is fine. */
        //TODO make this into an activity?
        try {
            StringBuilder msg = new StringBuilder();
            msg.append("<small>"); //<font size=\"2\"> doesn't work
            BufferedReader r = new BufferedReader(new FileReader(i.getFile()));
            while (true) {
                String s = r.readLine();
                if (s == null) break;
                msg.append(s);
                msg.append("<br/>");
            }
            r.close();
            msg.append("</small>");
            Tools.note(i.getName(), R.drawable.icon_small, Html.fromHtml(msg.toString()),
                    Tools.getString(R.string.Button_close), null, null, null, -1);
        } catch (Exception e) {
            ToolsTracker.error("MenuFileChooser.displayTextFile", e, i.getPath());
            Tools.warning(
                    Tools.getString(R.string.MenuFilechooser_file_open_error) +
                            i.getName() +
                            Tools.getString(R.string.Tools_error_msg) +
                            e.getMessage(),
                    Tools.cancel_action,
                    -1
            );

        }
    }

    private void onFileClick(MenuFileItem i) {
        selectedFilePath = i.getPath();
        // Directory
        if (i.isDirectory()) {
            File f = new File(selectedFilePath);
            //if (f.canRead() && !f.getAbsolutePath().equals(Tools.getAppDir().replace("/files", ""))) { // this is a hack, but it works
            if (f.canRead()) { // && !f.getAbsolutePath().equals(Tools.getAppDir())
                cwd = f;
                String path;
                // weird if else logic to prevent showing song folder as song is loading
                if (Tools.getBooleanSetting(R.string.stepfileFolderCheck, R.string.stepfileFolderCheckDefault)) {
                    path = Tools.checkStepfileDir(f);
                } else {
                    path = null;
                }

                if (path != null) {
                    selectStepfile(path);
                } else {
                    refresh();
                }

            } else {
                Tools.toast(
                        Tools.getString(R.string.MenuFilechooser_list_error) +
                                i.getPath() +
                                Tools.getString(R.string.Tools_permissions_error)
                );
            }
            return;
            // URL
        } else if (Tools.isLink(selectedFilePath)) {
            String link = parseURL(i.getFile());
            Tools.toast("Opening link:\n" + link);
            if (link == null || link.length() < 2) {
                Tools.toast(
                        Tools.getString(R.string.MenuFilechooser_url_error)
                );
            } else {
                Intent webBrowser = new Intent(Intent.ACTION_VIEW);
                webBrowser.setData(Uri.parse(link));
                startActivity(webBrowser);
            }
            // Stepfile
        } else if (Tools.isStepfile(selectedFilePath)) {
            selectStepfile(selectedFilePath);
            // Stepfile pack?
        } else if (Tools.isStepfilePack(selectedFilePath)) {
            new ToolsUnzipper(this, selectedFilePath, false).unzip();
            //Text file?
        } else if (Tools.isText(selectedFilePath)) {
            displayTextFile(i);
        } else {
            Tools.toast(
                    Tools.getString(R.string.MenuFilechooser_file_extension_error)
            );
        }
        refresh();
    }

    // File deletion
    private void deleteFile(File f) throws SecurityException {
        if (f.isDirectory()) {
            for (File nf : Objects.requireNonNull(f.listFiles())) {
                deleteFile(nf);
            }
        }
        if (!f.delete()) {
            throw new SecurityException(f.getPath());
        }
    }
}
