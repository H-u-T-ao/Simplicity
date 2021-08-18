package com.fengjiaxing.simplicity.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fengjiaxing.picload.RequestBuilder;
import com.fengjiaxing.picload.Simplicity;
import com.fengjiaxing.simplicity.ImagePreview.ImagePreviewActivity;
import com.fengjiaxing.simplicity.ImageSelectView;
import com.fengjiaxing.simplicity.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class AdapterUri extends RecyclerView.Adapter<AdapterUri.ViewHolder> {

    private final Activity activity;
    private final List<Uri> list;
    private final int lastLine;
    private final int totalLine;

    private RequestBuilder.CompressConfig compressConfig;

    public AdapterUri(Activity activity, List<Uri> list) {
        this.activity = activity;
        this.list = list;
        this.lastLine = list.size() % 4;
        this.totalLine = (lastLine == 0) ? (list.size() / 4) : (list.size() / 4 + 1);
    }

    @NonNull
    @Override
    public AdapterUri.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterUri.ViewHolder holder, int position) {
        if (4 * (position + 1) <= list.size()) {
            Uri uri1 = list.get(4 * position);
            Uri uri2 = list.get(4 * position + 1);
            Uri uri3 = list.get(4 * position + 2);
            Uri uri4 = list.get(4 * position + 3);
            loadPic(uri1, holder.iv1);
            loadPic(uri2, holder.iv2);
            loadPic(uri3, holder.iv3);
            loadPic(uri4, holder.iv4);
        } else {
            if (lastLine == 1) {
                Uri uri1 = list.get(4 * position);
                loadPic(uri1, holder.iv1);
                loadPic(null, holder.iv2);
                loadPic(null, holder.iv3);
                loadPic(null, holder.iv4);
            } else if (lastLine == 2) {
                Uri uri1 = list.get(4 * position);
                Uri uri2 = list.get(4 * position + 1);
                loadPic(uri1, holder.iv1);
                loadPic(uri2, holder.iv2);
                loadPic(null, holder.iv3);
                loadPic(null, holder.iv4);
            } else if (lastLine == 3) {
                Uri uri1 = list.get(4 * position);
                Uri uri2 = list.get(4 * position + 1);
                Uri uri3 = list.get(4 * position + 2);
                loadPic(uri1, holder.iv1);
                loadPic(uri2, holder.iv2);
                loadPic(uri3, holder.iv3);
                loadPic(null, holder.iv4);
            }
        }
    }

    @Override
    public int getItemCount() {
        return totalLine;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageSelectView iv1;
        ImageSelectView iv2;
        ImageSelectView iv3;
        ImageSelectView iv4;

        public ViewHolder(@NonNull View view) {
            super(view);
            iv1 = view.findViewById(R.id.hu_iv_item_list_1);
            iv2 = view.findViewById(R.id.hu_iv_item_list_2);
            iv3 = view.findViewById(R.id.hu_iv_item_list_3);
            iv4 = view.findViewById(R.id.hu_iv_item_list_4);
        }
    }

    private void loadPic(Uri uri, ImageSelectView iv) {
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
            iv.setObj(uri);
            iv.setOnNormalClickListener(() -> {
                Intent intent = new Intent(activity, ImagePreviewActivity.class);
                ArrayList<Uri> uriList = new ArrayList<>();
                uriList.add(uri);
                intent.putParcelableArrayListExtra("list", uriList);
                intent.putExtra("index", 0);
                activity.startActivity(intent);
            });
            iv.setOnLongClickListener(v -> {
                boolean selectable = ImageSelectView.getSelectable();
                if (!selectable) {
                    ImageSelectView.setSelectable(true);
                    iv.select();
                    Toast.makeText(activity, "返回以退出编辑模式", Toast.LENGTH_LONG).show();
                }
                return true;
            });
            iv.setEnabled(true);
        }
    }

    public void setCompressConfig(RequestBuilder.CompressConfig compressConfig) {
        this.compressConfig = compressConfig;
    }

}
