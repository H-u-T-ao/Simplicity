package com.fengjiaxing.picload;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class Dispatcher {

    private static final int TASK_QUEUE = 1;
    static final int TASK_COMPLETE = 2;

    private final Handler mainHandler;
    private final DispatcherThread dispatcherThread;
    final Handler handler;
    private final ExecutorService service;
    private final int maxNumber;
    private final int batch;

    private final AtomicInteger hunting;

    private final List<BitmapHunter> list;

    Dispatcher(Handler mainHandler, ExecutorService service, int maxNumber, int mode, int batch) {
        this.mainHandler = mainHandler;
        this.dispatcherThread = new DispatcherThread();
        dispatcherThread.start();
        this.handler = new DispatcherHandler(dispatcherThread.getLooper(), this);
        this.service = service;
        this.maxNumber = maxNumber;
        this.batch = batch;
        this.hunting = new AtomicInteger(0);
        this.list = new ArrayList<>();
        handler.sendMessage(handler.obtainMessage(mode));
    }

    void execute(RequestData data) {
        handler.sendMessage(handler.obtainMessage(TASK_QUEUE, data));
    }

    private void queue(RequestData data) {
        BitmapHunter hunter = new BitmapHunter(Simplicity.simplicity,
                this, data);
        list.add(hunter);
    }

    private void submit(int index) {
        BitmapHunter hunter = list.get(index);
        hunter.future = service.submit(hunter);
    }

    private void lastInFirstOutDispatcher() {
        int h = hunting.get();
        if (h < maxNumber) {
            int s = list.size();
            if (s != 0) {
                boolean b = hunting.compareAndSet(h, ++h);
                if (b) {
                    submit(s - 1);
                }
            }
        }
        handler.sendMessage(handler.obtainMessage(Simplicity.LIFO));
    }

    private static final int NORMAL = 0;
    private static final int BATCH = 1;

    private int fifoState = NORMAL;
    private int batchIndex;

    private void firstInFirstOutDispatcher() {
        int h = hunting.get();
        if (h < maxNumber) {
            int s = list.size();
            if (s != 0) {
                boolean b = hunting.compareAndSet(h, ++h);
                fifo(b, s);
            }
        }
        handler.sendMessage(handler.obtainMessage(Simplicity.FIFO));
    }

    private void fifo(boolean b, int s) {
        if (!b) return;
        switch (fifoState) {
            case NORMAL:
                if (s <= batch) {
                    submit(0);
                } else {
                    changeState(BATCH, s);
                }
                break;
            case BATCH:
                if (s - batchIndex <= batch) {
                    if (batchIndex < s) {
                        submit(batchIndex);
                    } else {
                        changeState(NORMAL, s);
                    }
                } else if (s - batchIndex >= batch) {
                    batchIndex = s - batch;
                    submit(batchIndex);
                }
                break;
            default:
                break;
        }
    }

    private void changeState(int fifoState, int s) {
        switch (fifoState) {
            case NORMAL:
                submit(0);
                this.fifoState = NORMAL;
                break;
            case BATCH:
                batchIndex = s - batch;
                submit(batchIndex);
                this.fifoState = BATCH;
                break;
            default:
                break;
        }
    }

    private void complete(BitmapHunter hunter) {
        if (list.remove(hunter)) {
            Bitmap result = hunter.getResult();
            if (result != null) {
                result.prepareToDraw();
                mainHandler.sendMessage(mainHandler.obtainMessage(Simplicity.REQUEST_SUCCESS, hunter));
            } else {
                mainHandler.sendMessage(mainHandler.obtainMessage(Simplicity.REQUEST_FAIL, hunter));
            }
            boolean b;
            do {
                int h = hunting.get();
                b = hunting.compareAndSet(h, --h);
            } while (!b);
        }
    }

    static class DispatcherThread extends HandlerThread {

        public DispatcherThread() {
            super(Simplicity.THREAD_NAME_DISPATCHER, Simplicity.THREAD_PRIORITY_BACKGROUND);
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
                case Simplicity.FIFO:
                    dispatcher.firstInFirstOutDispatcher();
                    break;
                case Simplicity.LIFO:
                    dispatcher.lastInFirstOutDispatcher();
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
