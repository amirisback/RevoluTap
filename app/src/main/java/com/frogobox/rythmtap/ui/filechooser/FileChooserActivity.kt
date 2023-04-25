package com.frogobox.rythmtap.ui.filechooser

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.frogobox.rythmtap.R
import com.frogobox.rythmtap.common.core.BaseBindActivity
import com.frogobox.rythmtap.databinding.ActivityFileChooseBinding
import com.frogobox.rythmtap.ui.filechooser.FileAdapter.ItemClickListener
import com.frogobox.rythmtap.ui.filechooser.FileAdapter.ItemLongClickListener
import com.frogobox.rythmtap.util.Tools
import com.frogobox.rythmtap.util.ToolsTracker
import com.frogobox.rythmtap.util.ToolsUnzipper
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.Objects
import java.util.Scanner

class FileChooserActivity : BaseBindActivity<ActivityFileChooseBinding>(), ItemClickListener, ItemLongClickListener {

    private var adapter: FileAdapter? = null
    private var cwd: File? = null
    private var selectedFilePath: String? = null
    private var useShortDirNames = false
    private var dividerItemDecoration: DividerItemDecoration? = null

    override fun setupViewBinding(): ActivityFileChooseBinding {
        return ActivityFileChooseBinding.inflate(layoutInflater)
    }

    override fun onCreateExt(savedInstanceState: Bundle?) {
        super.onCreateExt(savedInstanceState)
        setupHideSystemUI()
        Tools.setContext(this)
        // set up the RecyclerView
        val layoutManager = LinearLayoutManager(this)
        binding.chooseRecycler.layoutManager = layoutManager
        registerForContextMenu(binding.chooseRecycler)
        dividerItemDecoration = DividerItemDecoration(binding.chooseRecycler.context, layoutManager.orientation)
        //Objects.requireNonNull(this.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        adapter = null
        selectedFilePath = null

        // Get last dir
        var prefLastDir = Tools.getSetting(R.string.lastDir, R.string.lastDirDefault)
        useShortDirNames =
            Tools.getBooleanSetting(R.string.useShortDirNames, R.string.useShortDirNamesDefault)
        if (prefLastDir == "") {
            prefLastDir = Tools.getPacksDir()
        }
        cwd = File(prefLastDir)
        if (prefLastDir.isNotEmpty() &&
            cwd!!.exists() && cwd!!.parentFile != null &&
            cwd!!.path != Tools.getPacksDir()
        ) {
            cwd = cwd!!.parentFile
        } else {
            val browseLocationOrder = arrayOf(
                prefLastDir,
                Tools.getPacksDir()
            )
            for (path in browseLocationOrder) {
                if (path != null) {
                    cwd = File(path)
                    if (cwd!!.canRead() && cwd!!.isDirectory) {
                        break
                    }
                }
            }
        }
        ToolsTracker.data("Opened file browser", "cwd", cwd!!.absolutePath)
        refresh()
    }

    // Show files
    private fun shortDirName(s: String): String {
        var stripped = s
        // "[name] song"
        if (s[0] == '[' && s.indexOf(']') != -1 && s.indexOf(']') < s.length - 1) {
            stripped = s.substring(s.indexOf(']') + 1).trim { it <= ' ' }
            // "(name) song"
        } else if (s[0] == '(' && s.indexOf(')') != -1 && s.indexOf(')') < s.length - 1) {
            stripped = s.substring(s.indexOf(')') + 1).trim { it <= ' ' }
            // "#### song"
        } else if (Character.isDigit(s[0])) {
            var i = 0
            while (i < s.length && s[i] != ' ' && !Character.isLetter(s[i])) {
                i++
            }
            if (i < s.length - 1) {
                stripped = s.substring(i).trim { it <= ' ' }
            }
        }
        return if (stripped.length > 0) {
            stripped
        } else {
            s
        }
    }

    //private void ls(File dir) {
    private fun refresh() {
        val dir = cwd ?: return

        // remove /data/user/0/com.frogobox.rythmtap/files from title
        if (dir.absolutePath == Tools.getPacksDir()) {
            title = dir.absolutePath.replace(Tools.getAppDir() + "/", "")
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        } else {
            title = dir.absolutePath.replace(Tools.getPacksDir() + "/", "")
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        // Get lists
        val l = dir.listFiles()
        val dl = ArrayList<FileItem>()
        val fl = ArrayList<FileItem>()

        // Populate list
        if (l != null) {
            for (f in l) {
                var s = f.name
                if (!s.startsWith(".")) {
                    if (f.isDirectory) {
                        if (useShortDirNames) s = shortDirName(s)
                        dl.add(FileItem(s, f.absolutePath, true, f))
                    } else if (Tools.isStepfile(s) || Tools.isLink(s) || Tools.isStepfilePack(s) || Tools.isText(
                            s
                        )
                    ) {
                        fl.add(FileItem(s, f.absolutePath, false, f))
                    }
                }
            }
        }
        dl.sort()
        fl.sort()
        dl.addAll(fl) // Add file list to end of directories list

        // Display
        if (adapter == null) {
            adapter = FileAdapter(this, dl)
            adapter!!.setClickListener(this)
            adapter!!.setLongClickListener(this)
            binding.chooseRecycler.addItemDecoration(dividerItemDecoration!!)
            binding.chooseRecycler.adapter = adapter
        } else {
            adapter!!.updateData(dl)
        }
    }

    override fun onItemClick(view: View, position: Int) {
        val i = adapter!!.getItem(position)
        i?.let { onFileClick(it) }
    }

    override fun onItemLongClick(view: View, position: Int) {
        val i = adapter!!.getItem(position)!!
        selectedFilePath = i.path
        val delete_action = DialogInterface.OnClickListener { dialog: DialogInterface, id: Int ->
            try {
                deleteFile(File(selectedFilePath))
            } catch (e: Exception) {
                ToolsTracker.error("MenuFileChooser.deleteFile", e, selectedFilePath)
                Tools.error(
                    Tools.getString(R.string.MenuFilechooser_file_delete_error) +
                            selectedFilePath +
                            Tools.getString(R.string.Tools_error_msg) +
                            e.message,
                    Tools.cancel_action
                )
            }
            adapter!!.removeItem(position)
            dialog.cancel()
        }
        Tools.alert(
            Tools.getString(R.string.MenuFilechooser_file_delete),
            R.drawable.ic_delete_forever_filled_black,
            Tools.getString(R.string.MenuFilechooser_file_delete_confirm) +
                    i.name,
            Tools.getString(R.string.Button_yes),
            delete_action,
            Tools.getString(R.string.Button_no),
            Tools.cancel_action,
            -1
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onFileClick(FileItem("", cwd!!.parent, true, null))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun parseURL(url: File): String? {
        var sc: Scanner? = null
        try {
            sc = Scanner(url)
            var buffer: String
            while (sc.hasNextLine()) {
                buffer = sc.nextLine()
                if (buffer.contains("URL=")) {
                    sc.close()
                    return buffer.substring(buffer.indexOf("URL=") + 4).trim { it <= ' ' }
                }
            }
        } catch (e: Exception) {
            ToolsTracker.error("MenuFileChooser.parseURL", e, url.absolutePath)
        }
        sc?.close()
        return null
    }

    private fun selectStepfile(smFilePath: String?) {
        // Save preferences
        // smFilePath = path
        Tools.putSetting(R.string.smFilePath, smFilePath)
        Tools.putSetting(R.string.lastDir, cwd!!.path)
        val smFileName: String
        smFileName = if (smFilePath!!.lastIndexOf('/') != -1) {
            smFilePath.substring(smFilePath.lastIndexOf('/') + 1)
        } else {
            smFilePath
        }
        if (!Tools.getBooleanSetting(R.string.autoStart, R.string.autoStartDefault)) {
            Tools.toast(
                Tools.getString(R.string.MenuFilechooser_selected_stepfile) +
                        smFileName +
                        Tools.getString(R.string.MenuFilechooser_start_info)
            )
        }
        setResult(RESULT_OK)
        finish()
    }

    private fun displayTextFile(i: FileItem) {
        /*using html, because otherwise the text size is too big. I'm not sure why;
		the New User Notes box is plaintext and its text size is fine. */
        //TODO make this into an activity?
        try {
            val msg = StringBuilder()
            msg.append("<small>") //<font size=\"2\"> doesn't work
            val r = BufferedReader(FileReader(i.file))
            while (true) {
                val s = r.readLine() ?: break
                msg.append(s)
                msg.append("<br/>")
            }
            r.close()
            msg.append("</small>")
            Tools.note(
                i.name, R.drawable.icon_small, Html.fromHtml(msg.toString()),
                Tools.getString(R.string.Button_close), null, null, null, -1
            )
        } catch (e: Exception) {
            ToolsTracker.error("MenuFileChooser.displayTextFile", e, i.path)
            Tools.warning(
                Tools.getString(R.string.MenuFilechooser_file_open_error) +
                        i.name +
                        Tools.getString(R.string.Tools_error_msg) +
                        e.message,
                Tools.cancel_action,
                -1
            )
        }
    }

    private fun onFileClick(i: FileItem) {
        selectedFilePath = i.path
        // Directory
        if (i.isDirectory) {
            val f = File(selectedFilePath)
            //if (f.canRead() && !f.getAbsolutePath().equals(Tools.getAppDir().replace("/files", ""))) { // this is a hack, but it works
            if (f.canRead()) { // && !f.getAbsolutePath().equals(Tools.getAppDir())
                cwd = f
                // weird if else logic to prevent showing song folder as song is loading
                val path: String? = if (Tools.getBooleanSetting(
                        R.string.stepfileFolderCheck,
                        R.string.stepfileFolderCheckDefault
                    )
                ) {
                    Tools.checkStepfileDir(f)
                } else {
                    null
                }
                if (path != null) {
                    selectStepfile(path)
                } else {
                    refresh()
                }
            } else {
                Tools.toast(
                    Tools.getString(R.string.MenuFilechooser_list_error) +
                            i.path +
                            Tools.getString(R.string.Tools_permissions_error)
                )
            }
            return
            // URL
        } else if (Tools.isLink(selectedFilePath)) {
            val link = parseURL(i.file)
            Tools.toast("Opening link:\n$link")
            if (link == null || link.length < 2) {
                Tools.toast(
                    Tools.getString(R.string.MenuFilechooser_url_error)
                )
            } else {
                val webBrowser = Intent(Intent.ACTION_VIEW)
                webBrowser.data = Uri.parse(link)
                startActivity(webBrowser)
            }
            // Stepfile
        } else if (Tools.isStepfile(selectedFilePath)) {
            selectStepfile(selectedFilePath)
            // Stepfile pack?
        } else if (Tools.isStepfilePack(selectedFilePath)) {
            ToolsUnzipper(this, selectedFilePath, false).unzip()
            //Text file?
        } else if (Tools.isText(selectedFilePath)) {
            displayTextFile(i)
        } else {
            Tools.toast(
                Tools.getString(R.string.MenuFilechooser_file_extension_error)
            )
        }
        refresh()
    }

    // File deletion
    @Throws(SecurityException::class)
    private fun deleteFile(f: File) {
        if (f.isDirectory) {
            for (nf in Objects.requireNonNull(f.listFiles())) {
                deleteFile(nf)
            }
        }
        if (!f.delete()) {
            throw SecurityException(f.path)
        }
    }
}