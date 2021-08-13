package com.fengjiaxing.picload;

import android.graphics.Bitmap;
import android.os.Handler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;

public class BitmapHunter implements Runnable {

    final Simplicity simplicity;
    private final MemoryCacheRequestHandler memoryCacheRequestHandler;
    private final ResourceRequestHandler resourceRequestHandler;
    private final List<RequestHandler> requestHandlerList;
    private final Handler dispatcherHandler;
    final RequestData data;
    Future<?> future;
    private Bitmap result;
    private String from;

    private Exception exception;

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

    private Bitmap hunt() throws IOException {
        Bitmap bitmap;
        bitmap = hunt(memoryCacheRequestHandler);
        if (bitmap != null) {
            return bitmap;
        }
        if (data.resourceId != 0) {
            bitmap = hunt(resourceRequestHandler);
            if (bitmap != null) {
                return bitmap;
            }
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
        Bitmap bitmap = requestHandler.load(simplicity, data);
        if (bitmap != null) {
            from = requestHandler.loadSource();
            if (!(requestHandler instanceof MemoryCacheRequestHandler)) {
                RequestBuilder.CompressConfig compressConfig = data.compressConfig;
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
