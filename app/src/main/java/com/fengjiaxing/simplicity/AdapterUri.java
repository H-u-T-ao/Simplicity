package com.fengjiaxing.simplicity;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fengjiaxing.hutao.HuTao;

import java.util.List;

public class AdapterUri extends RecyclerView.Adapter<AdapterUri.ViewHolder> {

    private final Activity activity;
    private final List<Uri> list;
    private final int lastLine;
    private final int totalLine;

    public AdapterUri(Activity activity, List<Uri> list) {
        this.activity = activity;
        this.list = list;
        this.lastLine = list.size() % 3;
        this.totalLine = (lastLine == 0) ? (list.size() / 3) : (list.size() / 3 + 1);
    }

    @NonNull
    @Override
    public AdapterUri.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterUri.ViewHolder holder, int position) {
        if (3 * (position + 1) <= list.size()) {
            Uri uri1 = list.get(3 * position);
            Uri uri2 = list.get(3 * position + 1);
            Uri uri3 = list.get(3 * position + 2);
            HuTao.get(activity).load(uri1).into(holder.iv1);
            HuTao.get(activity).load(uri2).into(holder.iv2);
            HuTao.get(activity).load(uri3).into(holder.iv3);
        } else {
            if (lastLine == 1) {
                Uri uri = list.get(3 * position);
                HuTao.get(activity).load(uri).into(holder.iv1);
                holder.iv2.setBackgroundColor(Color.TRANSPARENT);
                holder.iv3.setBackgroundColor(Color.TRANSPARENT);
            } else if (lastLine == 2) {
                Uri uri1 = list.get(3 * position);
                Uri uri2 = list.get(3 * position + 1);
                HuTao.get(activity).load(uri1).into(holder.iv1);
                HuTao.get(activity).load(uri2).into(holder.iv2);
                holder.iv3.setBackgroundColor(Color.TRANSPARENT);
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

        HuTaoImageView iv1;
        HuTaoImageView iv2;
        HuTaoImageView iv3;

        public ViewHolder(@NonNull View view) {
            super(view);
            iv1 = view.findViewById(R.id.hu_iv_item_list_1);
            iv2 = view.findViewById(R.id.hu_iv_item_list_2);
            iv3 = view.findViewById(R.id.hu_iv_item_list_3);
        }
    }

}
