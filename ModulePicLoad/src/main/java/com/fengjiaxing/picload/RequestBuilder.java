package com.fengjiaxing.picload;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

        data.iv = iv;
        data.errorDrawable = this.errorDrawable;
        data.compressConfig = this.compressConfig;

        if (iv == null) {
            throw new NullPointerException("目标ImageView不应为空");
        }

        simplicity.prepareToExecute(data);
    }

    public interface CompressConfig {
        Bitmap Compress(Bitmap bitmap);
    }

}
