package com.fengjiaxing.simplicity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.TextView;

import com.fengjiaxing.simplicity.Adapter.AdapterShow;
import com.fengjiaxing.simplicity.ImagePreview.ImagePreviewActivity;

import java.util.ArrayList;
import java.util.List;

public class SummaryActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        TextView tvActionBar = findViewById(R.id.tv_show_action_bar);
        RecyclerView list = findViewById(R.id.rv_show_list);

        List<ImageSelectView> ivs = ImageSelectView.getList();

        List<Drawable> data = new ArrayList<>();

        for (int i = 0; i < ivs.size(); i++) {
            Drawable drawable = ivs.get(i).getDrawable();
            data.add(drawable);
        }

        tvActionBar.setText("已选择" + ivs.size() + "项");

        AdapterShow adapter = new AdapterShow(this, data);

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