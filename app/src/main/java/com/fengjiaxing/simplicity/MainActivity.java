package com.fengjiaxing.simplicity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.Toast;

import com.fengjiaxing.picload.SimplicityCompressConfig;
import com.fengjiaxing.simplicity.Adapter.AdapterUri;
import com.fengjiaxing.simplicity.ImagePreview.ImagePreviewActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 主活动MainActivity
 */
public class MainActivity extends AppCompatActivity {

    public static float refreshRate;

    private Button btnSelectNum;

    private final ArrayList<Uri> data = new ArrayList<>();

    private int num;

    private Timer timer;

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
        btnSelectNum = findViewById(R.id.btn_main_select_num);

        String path = "/storage/emulated/0/DCIM/Camera";
        getFilesAllName(path);
        AdapterUri adapter = new AdapterUri(this, data);

        ImageSelectView.setMax(99);

        SimplicityCompressConfig compressConfig =
                new SimplicityCompressConfig(1024 * 1024);
        adapter.setCompressConfig(compressConfig);

        list.setHasFixedSize(true);
        list.setFocusable(false);
        list.setFocusableInTouchMode(false);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        list.setLayoutManager(linearLayoutManager);
        list.setAdapter(adapter);

        btnSelectNum.setOnClickListener(v -> {
            List<Uri> uriList = ImageSelectView.getList();
            ArrayList<Uri> copy = new ArrayList<>(uriList);
            Intent intent = new Intent(this, ImagePreviewActivity.class);
            if (uriList.size() == 0) {
                intent.putParcelableArrayListExtra("list", data);
            } else {
                intent.putParcelableArrayListExtra("list", copy);
            }
            intent.putExtra("index", 0);
            startActivity(intent);
        });

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                if (num != ImageSelectView.getSelectedCount()) {
                    num = ImageSelectView.getSelectedCount();
                    runOnUiThread(() -> btnSelectNum.setText("完成(已选择" + num + "项)"));
                }
            }
        }, 0, 1);
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
        timer.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // SimplicityImageView.clearList();
    }

    /**
     * 获取指定路径的所有文件
     */
    private void getFilesAllName(String path) {
        File file = new File(path);
        File[] files = file.listFiles();
        if (files != null) {
            for (File value : files) {
                if (checkIsImageFile(value.getPath())) {
                    data.add(Uri.fromFile(value));
                }
            }
        }
    }

    /**
     * 检查文件是不是图片文件（限于jpg, png, gif, jpeg, bmp）
     */
    private static boolean checkIsImageFile(String fName) {
        boolean isImageFile = false;
        //获取文件拓展名
        String fileEnd = fName.substring(fName.lastIndexOf(".") + 1).toLowerCase();
        if (fileEnd.equals("jpg") || fileEnd.equals("png") || fileEnd.equals("gif")
                || fileEnd.equals("jpeg") || fileEnd.equals("bmp")) {
            isImageFile = true;
        }
        return isImageFile;
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
                Toast.makeText(this, "啊这，不给权限用不了的啊", Toast.LENGTH_LONG).show();
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