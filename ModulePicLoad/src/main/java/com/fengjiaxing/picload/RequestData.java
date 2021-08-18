package com.fengjiaxing.picload;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import static com.fengjiaxing.picload.Utils.createMd5Key;

public class RequestData {

    public Uri uri;
    public int resourceId;

    public ImageView iv;

    Drawable errorDrawable;
    private RequestBuilder.CompressConfig compressConfig;

    private CallBack callBack;

    public String key;

    RequestData(Uri uri) {
        this.uri = uri;
    }

    RequestData(int resourceId) {
        this.resourceId = resourceId;
    }

    void setCompressConfig(RequestBuilder.CompressConfig compressConfig) {
        this.compressConfig = compressConfig;
        if (uri != null) {
            String str =
                    (compressConfig == null) ?
                            uri.toString()
                            : uri.toString() + compressConfig.toString();
            key = createMd5Key(str);
        } else if (resourceId > 0) {
            String str =
                    (compressConfig == null) ?
                            Integer.toString(resourceId)
                            : resourceId + compressConfig.toString();
            key = createMd5Key(str);
        } else {
            key = "null";
        }
    }

    RequestBuilder.CompressConfig getCompressConfig() {
        return this.compressConfig;
    }

    void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    CallBack getCallBack() {
        return callBack;
    }

}
