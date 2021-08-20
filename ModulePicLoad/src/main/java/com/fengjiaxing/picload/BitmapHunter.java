package com.fengjiaxing.picload;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;

/**
 * 获取图片的类，实现Runnable接口，调用其run方法获取图片
 */
public class BitmapHunter implements Runnable {

    final Simplicity simplicity;
    private final MemoryCacheRequestHandler memoryCacheRequestHandler;
    private final ResourceRequestHandler resourceRequestHandler;
    private final List<RequestHandler> requestHandlerList;
    private final Handler dispatcherHandler;

    /**
     * 要获取的图片的基本信息 {@link RequestData}
     */
    public final RequestData data;

    Future<?> future;

    /**
     * 图片请求结果
     */
    private Bitmap result;

    /**
     * 图片请求的来源
     */
    private String from;

    /**
     * 捕获到的异常
     */
    public Exception exception;

    public BitmapHunter(Simplicity simplicity, Dispatcher dispatcher, RequestData data) {
        this.simplicity = simplicity;
        this.dispatcherHandler = dispatcher.handler;
        this.data = data;
        memoryCacheRequestHandler = simplicity.memoryCacheRequestHandler;
        resourceRequestHandler = simplicity.resourceRequestHandler;
        requestHandlerList = simplicity.requestHandlerList;
    }

    @Override
    public void run() {
        try {
            result = hunt();
        } catch (Exception e) {
            this.exception = e;
            // e.printStackTrace();
        }
        dispatcherHandler.sendMessage(dispatcherHandler.obtainMessage(Dispatcher.TASK_COMPLETE, this));
    }

    /**
     * 获取图片
     * <p>
     * 先从内存缓存中获取，如果内存缓存获取成功，则直接返回。
     * <p>
     * 如果内存缓存获取失败且调用 {@link Simplicity#load(int)} 传入resourceId，
     * 则从资源文件中获取，
     * 若获取成功，则将图片存入内存缓存后返回图片。
     * <p>
     * 如果内存缓存获取失败且调用 {@link Simplicity#load(Uri)} 传入uri，
     * 则根据在Simplicity中设置好的 {@link RequestHandler} 依次获取，
     * 获取成功则存入内存缓存并返回图片。
     * <p>
     * 如果仍然获取失败，最后直接返回null
     *
     * <p>
     * 每一次更换 {@link RequestHandler} 进行获取前，都会先尝试从内存缓存获取
     * <p>
     * 获取成功时（不包括从内存缓存中获取），
     * 会检查是否配置了 {@link RequestBuilder.CompressConfig}，
     * 如果配置了，则根据 {@link RequestBuilder.CompressConfig#Compress(Bitmap)}
     * 处理完毕后再存入内存缓存
     */
    private Bitmap hunt() throws IOException {
        Bitmap bitmap;
        bitmap = hunt(memoryCacheRequestHandler);
        if (bitmap != null) {
            return bitmap;
        }
        if (data.resourceId != 0) {
            bitmap = hunt(resourceRequestHandler);
            return bitmap;
        } else if (data.uri != null) {
            for (int i = 0; i < requestHandlerList.size(); i++) {
                RequestHandler requestHandler = requestHandlerList.get(i);
                bitmap = hunt(memoryCacheRequestHandler);
                if (bitmap != null) {
                    return bitmap;
                }
                bitmap = hunt(requestHandler);
                if (bitmap != null) {
                    return bitmap;
                }
            }
        }
        return null;
    }

    /**
     * 调用传入的 {@link RequestHandler#load(Simplicity, RequestData)} 获取图片，
     * 获取成功后根据设置好的 {@link RequestBuilder.CompressConfig} 配置图片再存入内存缓存
     */
    private Bitmap hunt(RequestHandler requestHandler) throws IOException {
        Bitmap bitmap = requestHandler.load(simplicity, data);
        if (bitmap != null) {
            from = requestHandler.loadSource();
            if (!(requestHandler instanceof MemoryCacheRequestHandler)) {
                RequestBuilder.CompressConfig compressConfig = data.getCompressConfig();
                if (compressConfig != null) {
                    bitmap = compressConfig.Compress(bitmap);
                }
                simplicity.memoryCache.put(data.key, bitmap);
            }
            return bitmap;
        } else {
            return null;
        }
    }

    /**
     * 获取图片请求结果
     */
    public Bitmap getResult() {
        return result;
    }

    /**
     * 获取图片请求来源
     */
    public String getFrom() {
        return from;
    }

}
