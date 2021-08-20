package com.fengjiaxing.picload;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * 默认的内存缓存实现类，使用 {@link LruCache} 实现
 */
public class SimplicityMemoryCache implements MemoryCache {

    public final LruCache<String, Bitmap> memoryCache;

    public SimplicityMemoryCache() {
        int size = (int) Runtime.getRuntime().maxMemory() / 1024 / 8;
        memoryCache = new LruCache<>(size);
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

    /**
     * 清除内存缓存
     */
    @Override
    public void clear() {
        memoryCache.evictAll();
    }

}
