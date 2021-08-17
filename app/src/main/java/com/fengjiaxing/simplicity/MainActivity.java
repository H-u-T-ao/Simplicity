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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Display;
import android.widget.Button;

import com.fengjiaxing.picload.Simplicity;
import com.fengjiaxing.picload.SimplicityCompressConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    public static float refreshRate;

    private RecyclerView list;
    private Button btnSelectNum;

    private AdapterFile adapter;

    private final List<File> data = new ArrayList<>();

    private int num;

    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();

        list = findViewById(R.id.rv_main_list);
        btnSelectNum = findViewById(R.id.btn_main_select_num);

        btnSelectNum.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ShowActivity.class);
            startActivity(intent);
        });

//        String url = "https://image.9game.cn/2020/10/11/180771437.jpg";
//        Uri uri = Uri.parse(url);
//
//        List<Uri> data = new ArrayList<>();
//        for (int i = 0; i < 499; i++) {
//            data.add(uri);
//        }

        String path = "/storage/emulated/0/DCIM/Camera";
        getFilesAllName(path);

        SimplicityImageView.setMax(99);

        adapter = new AdapterFile(this, data);
//        adapter = new AdapterUri(this, data);
        adapter.setCompressConfig(new SimplicityCompressConfig(1024 * 1024));

        list.setHasFixedSize(true);
        list.setFocusable(false);
        list.setFocusableInTouchMode(false);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        list.setLayoutManager(linearLayoutManager);
        list.setAdapter(adapter);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                if (num != SimplicityImageView.getSelectedCount()) {
                    num = SimplicityImageView.getSelectedCount();
                    runOnUiThread(() -> btnSelectNum.setText("完成(已选择" + num + "项)"));
                }
            }
        }, 0, 1);
    }

    @Override
    protected void onStart() {
        super.onStart();
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

    private void getFilesAllName(String path) {
        File file = new File(path);
        File[] files = file.listFiles();
        if (files != null) {
            for (File value : files) {
                if (checkIsImageFile(value.getPath())) {
                    data.add(value);
                }
            }
        }

    }

    private static boolean checkIsImageFile(String fName) {
        boolean isImageFile = false;
        //获取拓展名
        String fileEnd = fName.substring(fName.lastIndexOf(".") + 1).toLowerCase();
        if (fileEnd.equals("jpg") || fileEnd.equals("png") || fileEnd.equals("gif")
                || fileEnd.equals("jpeg") || fileEnd.equals("bmp")) {
            isImageFile = true;
        }
        return isImageFile;
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED))
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1024);
        }
    }

}