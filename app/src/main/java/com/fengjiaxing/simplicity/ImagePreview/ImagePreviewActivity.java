package com.fengjiaxing.simplicity.ImagePreview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
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

import com.fengjiaxing.simplicity.MainActivity;
import com.fengjiaxing.simplicity.R;

import java.util.List;

/**
 * 装载预览图片的View {@link ImagePreviewView} 的活动
 */
public class ImagePreviewActivity extends AppCompatActivity {

    private TextView tvIndex;

    String str;

    private final Handler handler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            tvIndex.setText(str);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        Intent intent = getIntent();
        // 获取Uri列表
        List<Uri> list = intent.getParcelableArrayListExtra("list");
        // 获取中心图片的索引
        int beginIndex = intent.getIntExtra("index", 0);

        int total = list.size();

        tvIndex = findViewById(R.id.tv_image_index);
        ImagePreviewView imagePreviewView = findViewById(R.id.isv_image_show);

        str = "" + (beginIndex + 1) + " / " + total;
        tvIndex.setText(str);

        imagePreviewView.setOnPictureChangeListener(new ImagePreviewView.OnPictureChangeListener() {
            @Override
            public void onChangeLast(int index) {
                str = "" + (index + 1) + " / " + total;
                handler.sendEmptyMessage(0);
            }

            @Override
            public void onChangeNext(int index) {
                str = "" + (index + 1) + " / " + total;
                handler.sendEmptyMessage(0);
            }
        });

        // 设置Uri列表
        imagePreviewView.setBitmapUriList(list);
        // 设置中心图片索引
        imagePreviewView.setBeginIndex(beginIndex);
        // 设置动画速度
        imagePreviewView.setAnimSpeed(1);
        // 设置刷新率
        imagePreviewView.setRefreshRate(MainActivity.refreshRate);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setTransparent(this);
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