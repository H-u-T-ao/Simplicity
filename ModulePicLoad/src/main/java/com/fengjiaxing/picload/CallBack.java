package com.fengjiaxing.picload;

/**
 * 当获取图片后要执行自定义操作时，
 * 可以实现此接口并调用 {@link RequestBuilder#into(CallBack)} 来执行一系列自定义操作
 * <p>
 * 其中传入的 BitmapHunter 对象中的信息，详见 {@link BitmapHunter}
 */
public interface CallBack {

    void success(BitmapHunter hunter);

    void fail(BitmapHunter hunter);

}
