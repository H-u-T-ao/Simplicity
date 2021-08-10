package com.fengjiaxing.hutao;

import android.graphics.Bitmap;

class MemoryCacheRequestHandler implements RequestHandler{

    private static final String SOURCE = "MEMORY_CACHE";

    @Override
    public Bitmap load(HuTao huTao, RequestData data) {
        MemoryCache memoryCache = huTao.memoryCache;
        return memoryCache.getBitmap(data.key);
    }

    @Override
    public String loadSource() {
        return SOURCE;
    }

}
