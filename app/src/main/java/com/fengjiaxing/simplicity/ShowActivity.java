package com.fengjiaxing.simplicity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.TextView;

import com.fengjiaxing.picload.SimplicityCompressConfig;

import java.util.List;

public class ShowActivity extends AppCompatActivity {

    private TextView tvActionBar;

    private RecyclerView list;

    private AdapterShow adapter;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        tvActionBar = findViewById(R.id.tv_show_action_bar);
        list = findViewById(R.id.rv_show_list);

        List<Drawable> data = SimplicityImageView.getDrawableList();

        adapter = new AdapterShow(this, data);

        tvActionBar.setText("已选择" + data.size() + "项");

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