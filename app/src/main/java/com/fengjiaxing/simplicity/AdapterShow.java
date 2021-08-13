package com.fengjiaxing.simplicity;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdapterShow extends RecyclerView.Adapter<AdapterShow.ViewHolder> {

    private final Activity activity;
    private final List<Drawable> list;
    private final int lastLine;
    private final int totalLine;

    public AdapterShow(Activity activity, List<Drawable> list) {
        this.activity = activity;
        this.list = list;
        lastLine = list.size() % 4;
        this.totalLine = (lastLine == 0) ? (list.size() / 4) : (list.size() / 4 + 1);
    }

    @NonNull
    @Override
    public AdapterShow.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterShow.ViewHolder holder, int position) {
        if (4 * (position + 1) <= list.size()) {
            Drawable drawable1 = list.get(4 * position);
            Drawable drawable2 = list.get(4 * position + 1);
            Drawable drawable3 = list.get(4 * position + 2);
            Drawable drawable4 = list.get(4 * position + 3);
            holder.iv1.setImageDrawable(drawable1);
            holder.iv2.setImageDrawable(drawable2);
            holder.iv3.setImageDrawable(drawable3);
            holder.iv4.setImageDrawable(drawable4);
        } else {
            if (lastLine == 1) {
                Drawable drawable1 = list.get(4 * position);
                holder.iv1.setImageDrawable(drawable1);
                holder.iv2.setImageDrawable(null);
                holder.iv3.setImageDrawable(null);
                holder.iv4.setImageDrawable(null);
            } else if (lastLine == 2) {
                Drawable drawable1 = list.get(4 * position);
                Drawable drawable2 = list.get(4 * position + 1);
                holder.iv1.setImageDrawable(drawable1);
                holder.iv2.setImageDrawable(drawable2);
                holder.iv3.setImageDrawable(null);
                holder.iv4.setImageDrawable(null);
            } else if (lastLine == 3) {
                Drawable drawable1 = list.get(4 * position);
                Drawable drawable2 = list.get(4 * position + 1);
                Drawable drawable3 = list.get(4 * position + 2);
                holder.iv1.setImageDrawable(drawable1);
                holder.iv2.setImageDrawable(drawable2);
                holder.iv3.setImageDrawable(drawable3);
                holder.iv4.setImageDrawable(null);
            }
        }
        holder.iv1.setEnabled(false);
        holder.iv2.setEnabled(false);
        holder.iv3.setEnabled(false);
        holder.iv4.setEnabled(false);
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
}
