package com.fengjiaxing.hutao;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import static com.fengjiaxing.hutao.Utils.createMd5Key;

public class RequestData {

    Uri uri;
    int resourceId;

    ImageView iv;

    Drawable errorDrawable;

    public String key;

    RequestData(Uri uri) {
        this.uri = uri;

        key = createMd5Key(uri.toString());
    }

    RequestData(int resourceId) {
        this.resourceId = resourceId;

        key = createMd5Key(Integer.toString(resourceId));
    }

}
