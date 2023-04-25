package com.frogobox.rythmtap.ui.filechooser;

import androidx.annotation.NonNull;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.frogobox.rythmtap.util.Tools;
import com.frogobox.rythmtap.util.ToolsTracker;
import com.frogobox.rythmtap.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class FileArrayAdapter extends RecyclerView.Adapter<FileArrayAdapter.ViewHolder> {

    private ArrayList<FileItem> mItems;
    private final LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private ItemLongClickListener mLongClickListener;
    private final boolean showSongBanners = Tools.getBooleanSetting(R.string.showSongBanners, R.string.showSongBannersDefault);

    // data is passed into the constructor
    FileArrayAdapter(Context context, ArrayList<FileItem> items) {
        this.mInflater = LayoutInflater.from(context);
        this.mItems = items;
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_file_choose, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView and ImageView in each row
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final FileItem i = mItems.get(position);
        if (i != null) {
            ImageView iv = holder.itemView.findViewById(R.id.iconview);
            iv.setImageResource(R.drawable.revolutap_banner_bg);
            iv.getLayoutParams().width = LinearLayout.LayoutParams.WRAP_CONTENT;
            String s = i.getName();
            String bannerpath = null;

            if (i.isDirectory()) {
                File f = new File(i.getPath());
                if (showSongBanners) {
                    if (f.isDirectory()) { // && f.getParent() != null && !f.getParent().equals(Tools.getAppDir())
                        File[] files = f.listFiles();
                        if (files != null) {
                            if (Tools.checkStepfileDir(i.getFile()) != null) {
                                for (File fi : files) {
                                    String fs = fi.getPath();
                                    if (fs.contains("bn.") || fs.contains("banner.")) {
                                        bannerpath = fs;
                                        break;
                                    }
                                }
                                //get image from .sm file (lots of parsing)
                                if (bannerpath == null) {
                                    for (File fi : files) {
                                        String fs = fi.getPath();
                                        if (fs.contains(".sm")) {
                                            final File smfile = new File(fs);

                                            if (smfile.exists()) {
                                                try {
                                                    FileInputStream is = new FileInputStream(smfile);
                                                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                                                    String line;
                                                    line = reader.readLine();
                                                    while (line != null) {
                                                        if (line.contains("#BANNER")) {
                                                            String bannerbg = line.replace("#BANNER:", "").replace(";", "").replace("../", "");
                                                            bannerpath = f + "/" + bannerbg;
                                                            ToolsTracker.info(bannerpath);
                                                            break;
                                                        }
                                                        line = reader.readLine();
                                                    }
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            break;
                                        }
                                    }
                                }

                            } else {
                                // TODO: Consolidate duplicated logic
                                for (File fi : files) {
                                    String fs = fi.getPath();
                                    if (fs.contains(".png") || fs.contains(".jpg") || fs.contains(".jpeg") || fs.contains(".bmp")) {
                                        bannerpath = fs;
                                        break;
                                    }
                                }

                            }
                        }
                    } //else if (f.getParent() != null && f.getParent().equals(Tools.getAppDir())) {
                        //iv.setImageResource(R.drawable.ic_folder_filled_black);
                    //}

                    // Apply banner to ImageView
                    iv.getLayoutParams().width = (int) (Tools.screen_w / 2.25);
                    if (bannerpath != null) {
                        File imgFile = new File(bannerpath);
                        if (imgFile.exists() && imgFile.isFile()) {
                            iv.setImageBitmap(BitmapFactory.decodeFile(imgFile.getAbsolutePath()));
                        }
                    }
                } else {
                    if (Tools.checkStepfileDir(i.getFile()) != null) {
                        iv.setImageResource(R.drawable.icon_small);
                    } else {
                        iv.setImageResource(R.drawable.ic_folder_filled_black);
                    }
                }

            } else if (Tools.isStepfile(i.getPath())) {
                if (Tools.isSMFile(s)) {
                    iv.setImageResource(R.drawable.icon_sm);
                } else if (Tools.isDWIFile(s)) {
                    iv.setImageResource(R.drawable.icon_dwi);
                } else {
                    iv.setImageResource(R.drawable.ic_warning_filled_black);
                }
            } else if (Tools.isStepfilePack(s)) {
                iv.setImageResource(R.drawable.ic_folder_zip_filled_black);
            } else if (Tools.isLink(s)) {
                iv.setImageResource(R.drawable.ic_link_filled_black);
            } else if (Tools.isText(s)) {
                iv.setImageResource(R.drawable.ic_text_snippet_filled_black);
            } else {
                iv.setImageResource(R.drawable.ic_warning_filled_black);
            }
            TextView tv = holder.itemView.findViewById(R.id.textview);
            if (tv != null) {
                tv.setText(i.getName());
                tv.setBackgroundColor(Color.TRANSPARENT);
                if (!showSongBanners){
                    tv.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void updateData(ArrayList<FileItem> items) {
        this.mItems = items;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            if (mLongClickListener != null) mLongClickListener.onItemLongClick(view, getAdapterPosition());
            return true;
        }
    }

    // convenience method for getting data at click position
    public FileItem getItem (int i) {
        return mItems.get(i);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    // allows long clicks events to be caught
    void setLongClickListener(ItemLongClickListener itemLongClickListener) {
        this.mLongClickListener = itemLongClickListener;
    }

    // parent activity will implement this method to respond to long click events
    public interface ItemLongClickListener {
        void onItemLongClick(View view, int position);
    }

}
