package com.fengjiaxing.picload;

import android.graphics.Bitmap;

/**
 * 根据key在内存缓存中获取图片的默认策略
 */
class MemoryCacheRequestHandler implements RequestHandler {

    private static final String SOURCE = "MEMORY_CACHE";

    @Override
    public Bitmap load(Simplicity simplicity, RequestData data) {
        MemoryCache memoryCache = simplicity.memoryCache;
        return memoryCache.getBitmap(data.key);
    }

    @Override
    public String loadSource() {
        return SOURCE;
    }

}
