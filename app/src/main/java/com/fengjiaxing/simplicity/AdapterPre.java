package com.fengjiaxing.simplicity;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fengjiaxing.picload.RequestBuilder;
import com.fengjiaxing.picload.Simplicity;

import java.util.ArrayList;

public class AdapterPre extends RecyclerView.Adapter<AdapterPre.ViewHolder> {

    private final PreviewActivity activity;
    private  final ArrayList<Uri> list;
    private final PreviewView previewView;

    private RequestBuilder.CompressConfig compressConfig;

    public AdapterPre(PreviewActivity activity, ArrayList<Uri> list) {
        this.activity = activity;
        this.list = list;
        this.previewView = activity.previewView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_preview, parent, false);
        return new ViewHolder(view);
    }

    private RelativeLayout selected;

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Uri uri = list.get(position);
        ImageView iv = holder.iv;
        RelativeLayout rl = holder.rl;
        if (activity.selectedPosition != position) {
            rl.setVisibility(View.GONE);
        } else {
            if (selected != null) {
                selected.setVisibility(View.GONE);
            }
            rl.setVisibility(View.VISIBLE);
            selected = rl;
        }
        loadPic(uri, iv);
        iv.setOnClickListener(v -> previewView.setIndex(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout rl;
        ImageView iv;

        public ViewHolder(@NonNull View view) {
            super(view);
            rl = view.findViewById(R.id.rl_item_list_bg);
            iv = view.findViewById(R.id.iv_item_list_pre);
        }
    }

    /**
     * RecyclerView加载图片的方法
     *
     * @param uri 图片的Uri
     * @param iv  图片所属的ImageView
     */
    private void loadPic(Uri uri, ImageView iv) {
        if (uri == null) {
            Simplicity.get(activity)
                    .load((Uri) null)
                    .into(iv);
            iv.setEnabled(false);
        } else {
            if (compressConfig != null) {
                Simplicity.get(activity)
                        .load(uri)
                        .setCompressConfig(compressConfig)
                        .setErrorDrawable(R.drawable.load_fail)
                        .into(iv);
            } else {
                Simplicity.get(activity)
                        .load(uri)
                        .setErrorDrawable(R.drawable.load_fail)
                        .into(iv);
            }
        }
    }

    /**
     * 给Simplicity设置图片压缩配置的方法
     *
     * @param compressConfig Simplicity图片压缩配置
     */
    public void setCompressConfig(RequestBuilder.CompressConfig compressConfig) {
        this.compressConfig = compressConfig;
    }

}
