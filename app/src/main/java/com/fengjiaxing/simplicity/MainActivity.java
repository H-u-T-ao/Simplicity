package com.fengjiaxing.simplicity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.widget.Button;

import com.fengjiaxing.picload.SimplicityCompressConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * 主活动MainActivity
 */
public class MainActivity extends AppCompatActivity {

    static float refreshRate;

    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 申请权限
        requestPermission();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                init();
            }
        } else {
            init();
        }
    }

    /**
     * 初始化MainActivity
     */
    private void init() {
        RecyclerView list = findViewById(R.id.rv_main_list);
        btn = findViewById(R.id.btn_main_select_num);

        ArrayList<Uri> data = PictureGetter.get(this);
        AdapterMain adapter = new AdapterMain(this, data);

        ImageSelectView.setMax(99);

        SimplicityCompressConfig compressConfig =
                new SimplicityCompressConfig(256, 256);
        adapter.setCompressConfig(compressConfig);

        list.setHasFixedSize(true);
        list.setFocusable(false);
        list.setFocusableInTouchMode(false);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        list.setLayoutManager(linearLayoutManager);
        list.setAdapter(adapter);

        btn.setOnClickListener(v -> {
            List<Uri> uriList = ImageSelectView.getList();
            if (uriList.size() == 0) {
                ToastUtil.showToast(this, "请长按进行选择");
            } else {
                ArrayList<Uri> copy = new ArrayList<>(uriList);
                Intent intent = new Intent(this, PreviewActivity.class);
                intent.putParcelableArrayListExtra("list", copy);
                intent.putExtra("useList", true);
                intent.putExtra("index", 0);
                startActivity(intent,
                        ActivityOptions
                                .makeSceneTransitionAnimation(this, btn, "name")
                                .toBundle());
            }
        });
    }

    /**
     * 重定向返回键
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            boolean selectable = ImageSelectView.getSelectable();
            // 如果正在选择模式下
            if (selectable) {
                ImageSelectView.clearList();
                ImageSelectView.setSelectable(false);
                ToastUtil.showToast(this, "退出选择模式");
                return true;
            }
            return super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 获取设备屏幕刷新率
        Display display = getWindowManager().getDefaultDisplay();
        refreshRate = display.getRefreshRate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 申请权限回调的结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1024) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                init();
            } else {
                ToastUtil.showToast(this, "啊这，不给权限用不了的啊");
                finish();
            }
        }
    }

    /**
     * 申请权限
     */
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED))
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1024);
        }
    }

}