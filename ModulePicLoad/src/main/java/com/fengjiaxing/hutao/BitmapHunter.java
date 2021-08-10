package com.fengjiaxing.hutao;

import android.graphics.Bitmap;
import android.os.Handler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;

import static com.fengjiaxing.hutao.Dispatcher.*;

public class BitmapHunter implements Runnable {

    final HuTao huTao;
    private final MemoryCacheRequestHandler memoryCacheRequestHandler;
    private final ResourceRequestHandler resourceRequestHandler;
    private final List<RequestHandler> requestHandlerList;
    private final Handler dispatcherHandler;
    final RequestData data;
    Future<?> future;
    private Bitmap result;
    private String from;

    private Exception exception;

    public BitmapHunter(HuTao huTao, Dispatcher dispatcher, RequestData data) {
        this.huTao = huTao;
        this.dispatcherHandler = dispatcher.handler;
        this.data = data;
        memoryCacheRequestHandler = huTao.memoryCacheRequestHandler;
        resourceRequestHandler = huTao.resourceRequestHandler;
        requestHandlerList = huTao.requestHandlerList;
    }

    @Override
    public void run() {
        try {
            result = hunt();
        } catch (Exception e) {
            this.exception = e;
            e.printStackTrace();
        }
        dispatcherHandler.sendMessage(dispatcherHandler.obtainMessage(TASK_COMPLETE, this));
    }

    private Bitmap hunt() throws IOException {
        Bitmap bitmap;
        bitmap = hunt(memoryCacheRequestHandler);
        if (bitmap != null) {
            return bitmap;
        }
        bitmap = hunt(resourceRequestHandler);
        if (bitmap != null) {
            return bitmap;
        }
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
        return null;
    }

    private Bitmap hunt(RequestHandler requestHandler) throws IOException {
        Bitmap bitmap = requestHandler.load(huTao, data);
        if (bitmap != null) {
            from = requestHandler.loadSource();
            if (!(requestHandler instanceof MemoryCacheRequestHandler)) {
                huTao.memoryCache.put(data.key, bitmap);
            }
            return bitmap;
        } else {
            return null;
        }
    }

    public Bitmap getResult() {
        return result;
    }

    public String getFrom() {
        return from;
    }

    public Exception getException() {
        return exception;
    }

}
