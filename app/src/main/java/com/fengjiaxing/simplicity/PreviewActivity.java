package com.fengjiaxing.simplicity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.fengjiaxing.picload.SimplicityCompressConfig;

import java.util.ArrayList;
import java.util.Collections;

/**
 * 装载预览图片的View {@link PreviewView} 的活动
 */
public class PreviewActivity extends AppCompatActivity {

    PreviewView previewView;

    private TextView tvIndex;

    private String str;

    int selectedPosition;

    private static final int UPDATE_TEXT = -1;

    private final Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            tvIndex.setText(str);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        Intent intent = getIntent();
        // 获取Uri列表
        ArrayList<Uri> intentList = intent.getParcelableArrayListExtra("list");
        ArrayList<Uri> list;
        if (intentList == null) {
            list = AdapterMain.list;
        } else {
            list = intentList;
        }
        // 获取中心图片的索引
        int beginIndex = intent.getIntExtra("index", 0);

        boolean useList = intent.getBooleanExtra("useList", false);

        tvIndex = findViewById(R.id.tv_image_index);
        previewView = findViewById(R.id.isv_image_show);
        RecyclerView recyclerView = findViewById(R.id.list_image_preview);
        AdapterPre adapter = new AdapterPre(this, list);

        if (useList) {
            SimplicityCompressConfig compressConfig =
                    new SimplicityCompressConfig(256, 256);
            adapter.setCompressConfig(compressConfig);

            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setHasFixedSize(true);
            recyclerView.setFocusable(false);
            recyclerView.setFocusableInTouchMode(false);
            CenterLayoutManager centerLayoutManager = new CenterLayoutManager(this);
            centerLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
            recyclerView.setLayoutManager(centerLayoutManager);
            recyclerView.setAdapter(adapter);

            ItemDrag itemDrag = new ItemDrag();
            itemDrag.setSwapData(new ItemDrag.OnSwapData() {
                @Override
                public void moving(int from, int to) {
                    previewView.swapList(from, to);
                    Collections.swap(list, from, to);
                    adapter.notifyItemMoved(from, to);
                }

                @Override
                public void onClearView(int from, int to) {
                    previewView.swapFinish(from, to);
                }
            });
            ItemTouchHelper touchHelper = new ItemTouchHelper(itemDrag);
            touchHelper.attachToRecyclerView(recyclerView);
        }

        int total = list.size();

        str = "" + (beginIndex + 1) + " / " + total;
        tvIndex.setText(str);

        previewView.setOnPictureChangeListener(index -> {
            if (useList) {
                selectedPosition = index;
                recyclerView.smoothScrollToPosition(index);
                adapter.notifyItemChanged(selectedPosition);
            }
            str = "" + (index + 1) + " / " + total;
            handler.sendMessage(handler.obtainMessage(UPDATE_TEXT));
        });

        // 设置Uri列表
        previewView.setBitmapUriList(list);
        // 设置中心图片索引
        previewView.setBeginIndex(beginIndex);
        // 设置预加载图片数
        previewView.setPreSize(4);
        // 设置动画速度
        previewView.setAnimSpeed(1);
        // 设置刷新率
        previewView.setRefreshRate((int) MainActivity.refreshRate);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setTransparent(this);
    }

    private static class CenterLayoutManager extends LinearLayoutManager {

        public CenterLayoutManager(Context context) {
            super(context);
        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
            RecyclerView.SmoothScroller smoothScroller = new CenterSmoothScroller(recyclerView.getContext());
            smoothScroller.setTargetPosition(position);
            startSmoothScroll(smoothScroller);
        }

    }

    private static class CenterSmoothScroller extends LinearSmoothScroller {

        public CenterSmoothScroller(Context context) {
            super(context);
        }

        @Override
        public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
            return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
        }
    }

    /**
     * 修改状态栏为透明状态，沉浸式预览图片
     */
    private static void setTransparent(Activity activity) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
    }

}