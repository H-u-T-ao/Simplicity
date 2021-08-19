package com.fengjiaxing.simplicity.Adapter;

import android.app.Activity;
import android.app.ActivityOptions;
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

import java.util.ArrayList;

public class AdapterUri extends RecyclerView.Adapter<AdapterUri.ViewHolder> {

    private final Activity activity;
    private final ArrayList<Uri> list;

    private RequestBuilder.CompressConfig compressConfig;

    public AdapterUri(Activity activity, ArrayList<Uri> list) {
        this.activity = activity;
        this.list = list;
    }

    @NonNull
    @Override
    public AdapterUri.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterUri.ViewHolder holder, int position) {
        Uri uri1 = null;
        Uri uri2 = null;
        Uri uri3 = null;
        Uri uri4 = null;
        int index1 = 4 * position;
        int index2 = 4 * position + 1;
        int index3 = 4 * position + 2;
        int index4 = 4 * position + 3;
        if (index1 < list.size()) {
            uri1 = list.get(4 * position);
        }
        if (index2 < list.size()) {
            uri2 = list.get(4 * position + 1);
        }
        if (index3 < list.size()) {
            uri3 = list.get(4 * position + 2);
        }
        if (index4 < list.size()) {
            uri4 = list.get(4 * position + 3);
        }
        loadPic(uri1, index1, holder.iv1);
        loadPic(uri2, index2, holder.iv2);
        loadPic(uri3, index3, holder.iv3);
        loadPic(uri4, index4, holder.iv4);
    }

    @Override
    public int getItemCount() {
        int s = list.size();
        return (s % 4 == 0) ? (s / 4) : (s / 4 + 1);
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

    /**
     * RecyclerView加载图片的方法
     *
     * @param uri   图片的Uri
     * @param index 图片的索引
     * @param iv    图片所属的ImageSelectView
     */
    private void loadPic(Uri uri, int index, ImageSelectView iv) {
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
            iv.setUri(uri);
            iv.setOnNormalClickListener(() -> {
                Intent intent = new Intent(activity, ImagePreviewActivity.class);
                intent.putParcelableArrayListExtra("list", list);
                intent.putExtra("index", index);
                activity.startActivity(intent,
                        ActivityOptions
                                .makeSceneTransitionAnimation(
                                        activity,
                                        iv,
                                        "name")
                                .toBundle());
            });
            iv.setOnLongClickListener(v -> {
                boolean selectable = ImageSelectView.getSelectable();
                if (!selectable) {
                    ImageSelectView.setSelectable(true);
                    boolean b = iv.selected();
                    if (b) {
                        Toast.makeText(activity, "返回以退出选择模式", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(activity, "已达到选择的最大数量", Toast.LENGTH_LONG).show();
                    }
                }
                return true;
            });
            iv.setEnabled(true);
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
