package com.fengjiaxing.picload;

import android.graphics.Bitmap;
import android.util.LruCache;

public class SimplicityMemoryCache implements MemoryCache {

    public final LruCache<String, Bitmap> memoryCache;

    public SimplicityMemoryCache() {
        memoryCache = new LruCache<>((int) (Runtime.getRuntime().maxMemory() / 8));
    }

    /**
     * 将位图放入内存中
     *
     * @param key    位图的key
     * @param bitmap 要存储的位图
     */
    @Override
    public void put(String key, Bitmap bitmap) {
        memoryCache.put(key, bitmap);
    }

    /**
     * 从内存中读取输入流并转化为位图
     *
     * @param key 位图的key，以此搜索对应文件
     * @return 如果搜索成功，则返回位图，否则返回null
     */
    @Override
    public Bitmap getBitmap(String key) {
        return memoryCache.get(key);
    }

    @Override
    public void clear() {
        memoryCache.evictAll();
    }

}
