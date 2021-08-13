package com.fengjiaxing.picload;

import android.graphics.Bitmap;

class MemoryCacheRequestHandler implements RequestHandler{

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
