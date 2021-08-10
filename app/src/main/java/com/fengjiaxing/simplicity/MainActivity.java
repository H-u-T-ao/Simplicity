package com.fengjiaxing.simplicity;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.fengjiaxing.hutao.HuTao;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView iv = findViewById(R.id.iv);

        // Picasso.get().load(R.drawable.ic_pic).into(iv);

        String str = "https://p2.music.126.net/6y-UleORITEDbvrOLV0Q8A==/5639395138885805.jpg";
        String str1 = "android.resource://com.fengjiaxing.simplicity/raw/" + R.drawable.ic_pic;
        Uri uri = Uri.parse(str1);

        // HuTao.get(this).load(R.drawable.ic_pic).into(iv);
        HuTao.get(this).load(uri).into(iv);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(() -> HuTao.get(MainActivity.this).load(uri).into(iv));
            }
        }).start();
    }
}