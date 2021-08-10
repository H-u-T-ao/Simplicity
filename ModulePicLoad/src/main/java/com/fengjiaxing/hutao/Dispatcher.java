package com.fengjiaxing.hutao;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutorService;

import static com.fengjiaxing.hutao.HuTao.*;

public class Dispatcher {

    private static final int TASK_SUBMIT = 1;
    static final int TASK_COMPLETE = 2;

    private final Handler mainHandler;
    private final DispatcherThread dispatcherThread;
    final Handler handler;
    private final ExecutorService service;

    public Dispatcher(Handler mainHandler, ExecutorService service) {
        this.mainHandler = mainHandler;
        this.dispatcherThread = new DispatcherThread();
        dispatcherThread.start();
        this.handler = new DispatcherHandler(dispatcherThread.getLooper(), this);
        this.service = service;
    }

    public void execute(RequestData data) {
        handler.sendMessage(handler.obtainMessage(TASK_SUBMIT, data));
    }

    private void beginToGet(RequestData data) {
        BitmapHunter hunter = new BitmapHunter(huTao, huTao.dispatcher, data);
        hunter.future = service.submit(hunter);
    }

    private void complete(BitmapHunter hunter) {
        Bitmap result = hunter.getResult();
        if (result != null) {
            mainHandler.sendMessage(mainHandler.obtainMessage(REQUEST_SUCCESS, hunter));
        } else {
            mainHandler.sendMessage(mainHandler.obtainMessage(REQUEST_FAIL, hunter));
        }
    }

    static class DispatcherThread extends HandlerThread {

        public DispatcherThread() {
            super(THREAD_NAME_DISPATCHER, THREAD_PRIORITY_BACKGROUND);
        }
    }

    private static class DispatcherHandler extends Handler {

        private final Dispatcher dispatcher;

        public DispatcherHandler(@NonNull Looper looper, Dispatcher dispatcher) {
            super(looper);
            this.dispatcher = dispatcher;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case TASK_SUBMIT:
                    RequestData data = (RequestData) msg.obj;
                    dispatcher.beginToGet(data);
                    break;
                case TASK_COMPLETE:
                    BitmapHunter hunter = (BitmapHunter) msg.obj;
                    dispatcher.complete(hunter);
            }
        }

    }

}
