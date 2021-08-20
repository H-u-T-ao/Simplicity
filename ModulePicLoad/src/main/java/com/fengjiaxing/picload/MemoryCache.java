package com.fengjiaxing.picload;

import android.graphics.Bitmap;

/**
 * 内存缓存接口
 * <p>
 * Simplicity默认的实现类 {@link SimplicityMemoryCache}
 */
public interface MemoryCache {

    void put(String key, Bitmap bitmap);

    Bitmap getBitmap(String key);

    void clear();
}
