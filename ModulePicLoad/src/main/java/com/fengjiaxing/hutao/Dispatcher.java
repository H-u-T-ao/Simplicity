package com.fengjiaxing.hutao;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static com.fengjiaxing.hutao.HuTao.*;

public class Dispatcher {

    private static final int TASK_QUEUE = 1;
    static final int TASK_COMPLETE = 2;

    private final Handler mainHandler;
    private final DispatcherThread dispatcherThread;
    final Handler handler;
    private final ExecutorService service;
    private final int maxNumber;

    private final AtomicInteger hunting;

    private final List<BitmapHunter> list;

    public Dispatcher(Handler mainHandler, ExecutorService service, int maxNumber, int mode) {
        this.mainHandler = mainHandler;
        this.dispatcherThread = new DispatcherThread();
        dispatcherThread.start();
        this.handler = new DispatcherHandler(dispatcherThread.getLooper(), this);
        this.service = service;
        this.maxNumber = maxNumber;
        this.hunting = new AtomicInteger(0);
        this.list = new ArrayList<>();
        handler.sendMessage(handler.obtainMessage(mode));
    }

    public void execute(RequestData data) {
        handler.sendMessage(handler.obtainMessage(TASK_QUEUE, data));
    }

    private void queue(RequestData data) {
        BitmapHunter hunter = new BitmapHunter(huTao, huTao.dispatcher, data);
        list.add(hunter);
    }

    private void LastInFirstOutDispatcher() {
        int h = hunting.get();
        if (h < maxNumber) {
            int s = list.size();
            if (s != 0) {
                boolean b = hunting.compareAndSet(h, ++h);
                if (b) {
                    BitmapHunter hunter = list.get(s - 1);
                    hunter.future = service.submit(hunter);
                    list.remove(hunter);
                }
            }
        }
        handler.sendMessage(handler.obtainMessage(LIFO));
    }

    private void FirstInFirstOutDispatcher() {
        int h = hunting.get();
        if (h < maxNumber) {
            int s = list.size();
            if (s != 0) {
                boolean b = hunting.compareAndSet(h, ++h);
                if (b) {
                    BitmapHunter hunter = list.get(0);
                    hunter.future = service.submit(hunter);
                    list.remove(hunter);
                }
            }
        }
        handler.sendMessage(handler.obtainMessage(FIFO));
    }

    private void complete(BitmapHunter hunter) {
        Bitmap result = hunter.getResult();
        if (result != null) {
            mainHandler.sendMessage(mainHandler.obtainMessage(REQUEST_SUCCESS, hunter));
        } else {
            mainHandler.sendMessage(mainHandler.obtainMessage(REQUEST_FAIL, hunter));
        }
        boolean b;
        do {
            int h = hunting.get();
            b = hunting.compareAndSet(h, --h);
        } while (!b);
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
                case FIFO:
                    dispatcher.FirstInFirstOutDispatcher();
                    break;
                case LIFO:
                    dispatcher.LastInFirstOutDispatcher();
                    break;
                case TASK_QUEUE:
                    RequestData data = (RequestData) msg.obj;
                    dispatcher.queue(data);
                    break;
                case TASK_COMPLETE:
                    BitmapHunter hunter = (BitmapHunter) msg.obj;
                    dispatcher.complete(hunter);
                    break;
            }
        }

    }

}
