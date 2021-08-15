package com.fengjiaxing.simplicity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class ImageActivity extends AppCompatActivity {

    private ImageShowView imageShowView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        imageShowView = findViewById(R.id.isv_image_show);
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.ic_pic_corner);
        imageShowView.setBitmap(b);
        imageShowView.setOnCrossBoundary(new ImageShowView.OnCrossBoundaryListener() {
            @Override
            public void crossLeft() {
            }

            @Override
            public void crossRight() {
            }
        });
    }

    int i = 0;
    int j = 0;

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