package com.fengjiaxing.simplicity;

import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fengjiaxing.picload.RequestBuilder;
import com.fengjiaxing.picload.Simplicity;

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
                holder.iv2.setImageDrawable(null);
                holder.iv3.setImageDrawable(null);
                holder.iv4.setImageDrawable(null);
                holder.iv2.setEnabled(false);
                holder.iv3.setEnabled(false);
                holder.iv4.setEnabled(false);
            } else if (lastLine == 2) {
                Uri uri1 = list.get(4 * position);
                Uri uri2 = list.get(4 * position + 1);
                loadPic(uri1, holder.iv1);
                loadPic(uri2, holder.iv2);
                holder.iv3.setImageDrawable(null);
                holder.iv4.setImageDrawable(null);
                holder.iv3.setEnabled(false);
                holder.iv4.setEnabled(false);
            } else if (lastLine == 3) {
                Uri uri1 = list.get(4 * position);
                Uri uri2 = list.get(4 * position + 1);
                Uri uri3 = list.get(4 * position + 2);
                loadPic(uri1, holder.iv1);
                loadPic(uri2, holder.iv2);
                loadPic(uri3, holder.iv3);
                holder.iv4.setImageDrawable(null);
                holder.iv4.setEnabled(false);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return totalLine;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        SimplicityImageView iv1;
        SimplicityImageView iv2;
        SimplicityImageView iv3;
        SimplicityImageView iv4;

        public ViewHolder(@NonNull View view) {
            super(view);
            iv1 = view.findViewById(R.id.hu_iv_item_list_1);
            iv2 = view.findViewById(R.id.hu_iv_item_list_2);
            iv3 = view.findViewById(R.id.hu_iv_item_list_3);
            iv4 = view.findViewById(R.id.hu_iv_item_list_4);
        }
    }

    private void loadPic(Uri uri, SimplicityImageView iv) {
        if (compressConfig != null) {
            Simplicity
                    .get(activity)
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

    public void setCompressConfig(RequestBuilder.CompressConfig compressConfig) {
        this.compressConfig = compressConfig;
    }

}
