package com.fengjiaxing.simplicity.ImagePreview;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.fengjiaxing.simplicity.MainActivity;
import com.fengjiaxing.simplicity.R;

import java.util.List;

public class ImagePreviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        Intent intent = getIntent();
        List<Uri> list = intent.getParcelableArrayListExtra("list");
        int beginIndex = intent.getIntExtra("index", 0);

        ImagePreviewView imagePreviewView = findViewById(R.id.isv_image_show);

        imagePreviewView.setBitmapUriList(list);
        imagePreviewView.setBeginIndex(beginIndex);
        imagePreviewView.setAnimSpeed(1);
        imagePreviewView.setRefreshRate(MainActivity.refreshRate);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setTransparent(this);
    }

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