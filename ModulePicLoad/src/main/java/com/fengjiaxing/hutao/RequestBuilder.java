package com.fengjiaxing.hutao;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import static com.fengjiaxing.hutao.Utils.*;

public class RequestBuilder {

    private final HuTao huTao;
    private final RequestData data;

    private Drawable errorDrawable;

    RequestBuilder(HuTao huTao, Uri uri) {
        this.huTao = huTao;
        this.data = new RequestData(uri);
    }

    RequestBuilder(HuTao huTao, int resourceId) {
        this.huTao = huTao;
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
        Bitmap bitmap = BitmapFactory.decodeResource(huTao.context.getResources(), resourceId);
        this.errorDrawable = new BitmapDrawable(bitmap);
        return this;
    }

    public void into(ImageView iv) {
        checkMain();

        data.iv = iv;
        data.errorDrawable = this.errorDrawable;

        if (iv == null) {
            throw new NullPointerException("目标ImageView不应为空");
        }

        huTao.prepareToExecute(data);
    }

}
