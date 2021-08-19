package com.fengjiaxing.simplicity;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.fengjiaxing.picload.Simplicity;

public class SimplicityApplication extends Application {

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    /**
     * 在这里修改Simplicity的属性参数，
     * 详情见 {@link Simplicity} {@link Simplicity.Builder}
     */
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        Simplicity.Builder builder =
                new Simplicity.Builder(this)
                        .setMode(Simplicity.FIFO)
                        .setStealLimit(40);
        Simplicity.setSimplicityInstance(builder.build());
    }

    /**
     * 全局获取Application
     */
    public static Context getApplication() {
        return context;
    }

}
