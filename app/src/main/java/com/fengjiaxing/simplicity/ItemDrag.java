package com.fengjiaxing.simplicity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class ItemDrag extends ItemTouchHelper.Callback {

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        return makeMovementFlags(dragFlags, 0);
    }

    private int from;
    private int to;

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        int from = viewHolder.getAdapterPosition();
        if (this.from == -1) {
            this.from = from;
        }
        to = target.getAdapterPosition();
        if (swapData != null) {
            swapData.moving(from, to);
        }
        return true;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        if (swapData != null) {
            swapData.onClearView(from, to);
        }
        from = -1;
    }

    interface OnSwapData {
        void moving(int from, int to);

        void onClearView(int from, int to);
    }

    private OnSwapData swapData;

    void setSwapData(OnSwapData swapData) {
        this.swapData = swapData;
    }

}
