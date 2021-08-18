package com.fengjiaxing.picload;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

public class RequestBuilder {

    private final Simplicity simplicity;
    private final RequestData data;

    private Drawable errorDrawable;
    private CompressConfig compressConfig;

    RequestBuilder(Simplicity simplicity, Uri uri) {
        this.simplicity = simplicity;
        this.data = new RequestData(uri);
    }

    RequestBuilder(Simplicity simplicity, int resourceId) {
        this.simplicity = simplicity;
        this.data = new RequestData(resourceId);
    }

    public RequestBuilder setErrorDrawable(Drawable drawable) {
        if (drawable == null) {
            throw new NullPointerException("设置的加载错误显示图片不应为空指针");
        }
        this.errorDrawable = drawable;
        return this;
    }

    public RequestBuilder setErrorDrawable(int resourceId) {
        if (resourceId <= 0) {
            throw new NullPointerException("设置的加载错误显示图片的资源ID不应为非正数");
        }
        Bitmap bitmap = BitmapFactory.decodeResource(simplicity.context.getResources(), resourceId);
        this.errorDrawable = new BitmapDrawable(bitmap);
        return this;
    }

    public RequestBuilder setCompressConfig(CompressConfig compressConfig) {
        if (compressConfig == null) {
            throw new NullPointerException("设置的压缩位图配置不应为空指针");
        }
        this.compressConfig = compressConfig;
        return this;
    }

    public void into(ImageView iv) {
        Utils.checkMain();

        if (iv == null) {
            throw new NullPointerException("目标ImageView不应为空");
        }

        if (data.uri == null && data.resourceId == 0) {
            iv.setTag(null);
            iv.setImageDrawable(null);
        } else {
            String tag = data.uri != null ?
                    data.uri.toString() : Integer.toString(data.resourceId);
            iv.setTag(tag);
            iv.setImageDrawable(null);
            data.iv = iv;
            data.errorDrawable = this.errorDrawable;
            data.setCompressConfig(this.compressConfig);
            simplicity.prepareToExecute(data);
        }
    }

    public void into(CallBack callBack) {
        Utils.checkMain();
        if (callBack == null) {
            throw new NullPointerException("回调不应为空");
        }
        if (data.uri != null || data.resourceId != 0) {
            data.errorDrawable = this.errorDrawable;
            data.setCompressConfig(this.compressConfig);
            data.setCallBack(callBack);
            simplicity.prepareToExecute(data);
        }
    }

    public interface CompressConfig {
        Bitmap Compress(Bitmap bitmap);
    }

}
