package com.fengjiaxing.simplicity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fengjiaxing.picload.RequestBuilder;
import com.fengjiaxing.picload.Simplicity;

import java.util.ArrayList;

public class AdapterMain extends RecyclerView.Adapter<AdapterMain.ViewHolder> {

    private final MainActivity activity;
    static ArrayList<Uri> list;

    private RequestBuilder.CompressConfig compressConfig;

    public AdapterMain(MainActivity activity, ArrayList<Uri> list) {
        this.activity = activity;
        AdapterMain.list = list;
    }

    @NonNull
    @Override
    public AdapterMain.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_main, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterMain.ViewHolder holder, int position) {
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
            iv1 = view.findViewById(R.id.siv_item_list_1);
            iv2 = view.findViewById(R.id.siv_item_list_2);
            iv3 = view.findViewById(R.id.siv_item_list_3);
            iv4 = view.findViewById(R.id.siv_item_list_4);
        }
    }

    /**
     * RecyclerView?????????????????????
     *
     * @param uri   ?????????Uri
     * @param index ???????????????
     * @param iv    ???????????????ImageSelectView
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
            iv.setOnSelectedListener(new ImageSelectView.OnSelectedListener() {
                @Override
                public void onSelected(ImageSelectView view) {
                    String str = "??????(?????????" + ImageSelectView.getSelectedCount() + "???)";
                    activity.btn.setText(str);
                }

                @Override
                public void onUnSelected(ImageSelectView view) {
                    String str = "??????(?????????" + ImageSelectView.getSelectedCount() + "???)";
                    activity.btn.setText(str);
                }
            });
            iv.setOnNormalClickListener(() -> {
                Intent intent = new Intent(activity, PreviewActivity.class);
                intent.putParcelableArrayListExtra("list", null);
                intent.putExtra("useList", false);
                intent.putExtra("index", index);
                activity.startActivity(intent,
                        ActivityOptions
                                .makeSceneTransitionAnimation(activity, iv, "name")
                                .toBundle());
            });
            iv.setOnLongClickListener(v -> {
                boolean selectable = ImageSelectView.getSelectable();
                if (!selectable) {
                    ImageSelectView.setSelectable(true);
                    boolean b = iv.selected();
                    if (b) {
                        ToastUtil.showToast(activity, "???????????????????????????");
                    } else {
                        ToastUtil.showToast(activity, "??????????????????????????????");
                    }
                }
                return true;
            });
            iv.setEnabled(true);
        }
    }

    /**
     * ???Simplicity?????????????????????????????????
     *
     * @param compressConfig Simplicity??????????????????
     */
    public void setCompressConfig(RequestBuilder.CompressConfig compressConfig) {
        this.compressConfig = compressConfig;
    }

}
