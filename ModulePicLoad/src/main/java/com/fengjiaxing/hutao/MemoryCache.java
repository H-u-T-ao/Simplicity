package com.fengjiaxing.hutao;

import android.graphics.Bitmap;

public interface MemoryCache {

    void put(String key, Bitmap bitmap);

    Bitmap getBitmap(String key);

    void clear();
}