package com.fengjiaxing.hutao;

import android.os.Looper;
import android.util.Log;

import java.security.MessageDigest;

import static com.fengjiaxing.hutao.HuTao.*;

public class Utils {

    public static void log(BitmapHunter hunter, boolean b) {
        String s = b ? "加载成功 - " : "加载失败 - ";
        RequestData data = hunter.data;
        String hunterInfo;
        if (data.uri != null) {
            hunterInfo = "URI:" + data.uri.toString();
        } else {
            hunterInfo = "RESOURCE_ID:" + data.resourceId;
        }
        Log.d(TAG, s + hunterInfo + " - SOURCE:" + hunter.getFrom());
    }

    public static boolean isMain() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    public static void checkMain() {
        if (!isMain()) {
            throw new IllegalStateException("方法应该在主线程上执行");
        }
    }

    public static void checkNotMain() {
        if (isMain()) {
            throw new IllegalStateException("方法不应该主线程上执行");
        }
    }

    public static String createMd5Key(String str) {
        MessageDigest messageDigest;
        String encryptStr = "";
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            byte[] digest = messageDigest.digest(str.getBytes());
            StringBuilder strBuilder = new StringBuilder();
            String tempStr;
            for (byte b : digest) {
                tempStr = (Integer.toHexString(b & 0xff));
                if (tempStr.length() == 1) {
                    strBuilder.append("0").append(tempStr);
                } else {
                    strBuilder.append(tempStr);
                }
            }
            encryptStr = strBuilder.toString().toLowerCase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptStr;
    }

}
