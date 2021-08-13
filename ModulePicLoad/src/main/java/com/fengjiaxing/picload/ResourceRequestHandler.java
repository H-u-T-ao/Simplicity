package com.fengjiaxing.picload;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ResourceRequestHandler implements RequestHandler{

    private static final String SOURCE = "RESOURCE";

    @Override
    public Bitmap load(Simplicity simplicity, RequestData data) {
        int resourceId = data.resourceId;
        return BitmapFactory.decodeResource(simplicity.context.getResources(), resourceId);
    }

    @Override
    public String loadSource() {
        return SOURCE;
    }
}
