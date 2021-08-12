package com.fengjiaxing.simplicity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.fengjiaxing.hutao.HuTao;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView list;
    private AdapterUri adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list = findViewById(R.id.rv_main_list);

        String url = "https://www.baidu.com/img/flexible/logo/pc/result@2.png";
        Uri uri = Uri.parse(url);

        List<Uri> data = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            data.add(uri);
        }

        adapter = new AdapterUri(this, data);

        list.setHasFixedSize(true);
        list.setFocusable(false);
        list.setFocusableInTouchMode(false);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        list.setLayoutManager(linearLayoutManager);
        list.setAdapter(adapter);

    }

}