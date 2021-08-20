package com.fengjiaxing.picload;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import static com.fengjiaxing.picload.Utils.createMd5Key;

/**
 * 要请求的图片的信息
 */
public class RequestData {

    public Uri uri;
    public int resourceId;

    public String key;

    Drawable errorDrawable;
    private RequestBuilder.CompressConfig compressConfig;

    public ImageView iv;
    private CallBack callBack;

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
