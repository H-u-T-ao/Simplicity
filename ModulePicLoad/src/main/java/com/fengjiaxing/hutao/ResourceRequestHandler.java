package com.fengjiaxing.hutao;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ResourceRequestHandler implements RequestHandler{

    private static final String SOURCE = "RESOURCE";

    @Override
    public Bitmap load(HuTao huTao, RequestData data) {
        int resourceId = data.resourceId;
        return BitmapFactory.decodeResource(huTao.context.getResources(), resourceId);
    }

    @Override
    public String loadSource() {
        return SOURCE;
    }
}
