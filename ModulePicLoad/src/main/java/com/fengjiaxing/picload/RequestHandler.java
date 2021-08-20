package com.fengjiaxing.picload;

import android.graphics.Bitmap;

import java.io.IOException;

/**
 * 实现此接口并在初始化Simplicity时设置自定义的RequestHandler，
 * 即可根据自定义的图片获取策略进行获取
 */
public interface RequestHandler {

    /**
     * 请求图片的抽象方法
     *
     * @param simplicity Simplicity对象
     * @param data       要请求的图片的信息
     */
    Bitmap load(Simplicity simplicity, RequestData data) throws IOException;

    /**
     * @return 请求的来源
     */
    String loadSource();

}
