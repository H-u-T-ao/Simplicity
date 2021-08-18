package com.fengjiaxing.simplicity;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            // This process is dedicated to LeakCanary for
//            // heap analysis.
//            // You should not init your app in this process.
//            return;
//        }
//
//        LeakCanary.install(this);

    }

    public static Context getApplication(){
        return context;
    }

}
