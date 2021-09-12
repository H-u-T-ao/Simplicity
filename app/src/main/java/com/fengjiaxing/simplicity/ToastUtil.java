package com.fengjiaxing.simplicity;

import android.content.Context;
import android.widget.Toast;

/**
 * 吐司的工具类，用于避免用户多次呼出吐司时吐司排队显示影响体验
 * */
public class ToastUtil {

    private static Toast toast;
    private static final int SHORT = Toast.LENGTH_SHORT;

    private static String lastText;

    private static long lastTime;

    public static void showToast(Context context, String message) {
        if (message == null) return;
        if (System.currentTimeMillis() - lastTime > 2000L) {
            toast = Toast.makeText(context.getApplicationContext(), message, SHORT);
            toast.show();
            lastText = message;
            lastTime = System.currentTimeMillis();
        } else {
            if (!message.equals(lastText)) {
                toast.cancel();
                toast = Toast.makeText(context.getApplicationContext(), message, SHORT);
                toast.show();
                lastText = message;
                lastTime = System.currentTimeMillis();
            }
        }
    }

}
