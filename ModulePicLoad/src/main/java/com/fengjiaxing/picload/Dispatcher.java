package com.fengjiaxing.picload;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 调度者
 * <p>
 * 联系获取图片的线程池和主线程的桥梁,同时担任给线程池推送任务的职责
 */
public class Dispatcher {

    private static final int TASK_QUEUE = 1;
    static final int TASK_COMPLETE = 2;
    private static final int BATCH_SUCCESS_DELAY = 3;

    private static final int DELAY_TIME = 200;

    private final Handler mainHandler;
    private final DispatcherThread dispatcherThread;
    final Handler handler;
    private final ExecutorService service;

    /**
     * 允许线程池同时执行的任务数量
     */
    private final int corePoolSize;

    /**
     * FIFO模式下，若总任务数超过 stealLimit，
     * 则会从任务队列的最后一个任务往前数的第 stealLimit 个任务处窃取任务推送给线程池执行。
     * <p>
     * 若想要在FIFO模式下以正常的FIFO原则推送任务，
     * 只需要把这个变量设置为 {@link Integer#MAX_VALUE} 即可
     * <p>
     * LIFO模式下，该参数不起作用
     */
    private final int stealLimit;

    /**
     * 批处理，优化视觉效果，减少主线程looper的工作量（一次性推送多个成功的任务）
     */
    private final Set<BitmapHunter> successBatch;

    /**
     * 正在线程池中执行的任务数
     */
    private final AtomicInteger hunting;

    /**
     * 等待队列，存放正在排队的任务
     */
    private final List<BitmapHunter> list;

    Dispatcher(Handler mainHandler, ExecutorService service, int corePoolSize, int mode, int stealLimit) {
        this.mainHandler = mainHandler;
        this.dispatcherThread = new DispatcherThread();
        dispatcherThread.start();
        this.handler = new DispatcherHandler(dispatcherThread.getLooper(), this);
        this.service = service;
        this.corePoolSize = corePoolSize;
        this.stealLimit = stealLimit;
        this.successBatch = new HashSet<>();
        this.hunting = new AtomicInteger(0);
        this.list = new ArrayList<>();
        handler.sendMessage(handler.obtainMessage(mode));
    }

    void shutdown() {
        service.shutdown();
        dispatcherThread.quit();
    }

    /**
     * 新的 {@link RequestData} 传入
     */
    void execute(RequestData data) {
        handler.sendMessage(handler.obtainMessage(TASK_QUEUE, data));
    }

    /**
     * 根据传入的 {@link RequestData} 封装一个 {@link BitmapHunter} 对象,
     * 并将这个BitmapHunter对象加入到等待队列中进行排队
     */
    private void queue(RequestData data) {
        BitmapHunter hunter = new BitmapHunter(Simplicity.simplicity,
                this, data);
        list.add(hunter);
    }

    /**
     * 根据索引从排队队列中获取任务推送给线程池执行
     */
    private void submit(int index) {
        BitmapHunter hunter = list.get(index);
        hunter.future = service.submit(hunter);
    }

    /**
     * LIFO模式下，每一次都从等待队列末尾推送任务给线程池执行
     */
    private void lastInFirstOutDispatcher() {
        int h = hunting.get();
        if (h < corePoolSize) {
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
    private static final int STEAL = 1;

    private int fifoState = NORMAL;
    private int stealIndex;

    /**
     * FIFO模式下，每次推送都要检查任务数量是否超过 {@link Dispatcher#stealLimit} ,
     * 决定是否开始窃取任务执行。
     * <p>
     * 详细的窃取的操作逻辑详见 {@link Dispatcher#fifo(boolean, int)}
     * 和 {@link Dispatcher#changeState(int, int)}
     */
    private void firstInFirstOutDispatcher() {
        int h = hunting.get();
        if (h < corePoolSize) {
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
                if (s <= stealLimit) {
                    submit(0);
                } else {
                    changeState(STEAL, s);
                }
                break;
            case STEAL:
                if (s - stealIndex <= stealLimit) {
                    if (stealIndex < s) {
                        submit(stealIndex);
                    } else {
                        changeState(NORMAL, s);
                    }
                } else if (s - stealIndex >= stealLimit) {
                    stealIndex = s - stealLimit;
                    submit(stealIndex);
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
            case STEAL:
                stealIndex = s - stealLimit;
                submit(stealIndex);
                this.fifoState = STEAL;
                break;
            default:
                break;
        }
    }

    /**
     * 每间隔一段时间就把成功的任务添加到成功队列中等待批处理
     */
    private void batchSuccess(BitmapHunter hunterS) {
        successBatch.add(hunterS);
        if (!handler.hasMessages(BATCH_SUCCESS_DELAY)) {
            handler.sendEmptyMessageDelayed(BATCH_SUCCESS_DELAY, DELAY_TIME);
        }
    }

    /**
     * 每隔一段时间就将已经一批请求成功的任务推送给主线程进行下一步操作
     */
    private void batchComplete() {
        Set<BitmapHunter> copy = new HashSet<>(successBatch);
        successBatch.clear();
        mainHandler.sendMessage(mainHandler.obtainMessage(Simplicity.REQUEST_SUCCESS, copy));
    }

    /**
     * 请求失败向主线程发送失败的信息
     */
    private void requestFail(BitmapHunter hunterF) {
        mainHandler.sendMessage(mainHandler.obtainMessage(Simplicity.REQUEST_FAIL, hunterF));
    }

    /**
     * 在 {@link BitmapHunter} 中获取图片完成以后，调用此方法根据获取的结果执行对应的操作。
     * 若获取成功，则准备批处理。若获取失败，发送信息给主线程告知主线程
     */
    private void complete(BitmapHunter hunter) {
        if (list.remove(hunter)) {
            Bitmap result = hunter.getResult();
            if (result != null) {
                batchSuccess(hunter);
            } else {
                requestFail(hunter);
            }
            boolean b;
            do {
                int h = hunting.get();
                b = hunting.compareAndSet(h, --h);
            } while (!b);
        }
    }

    /**
     * 调度者内部的用于调度任务的线程
     */
    static class DispatcherThread extends HandlerThread {

        public DispatcherThread() {
            super(Simplicity.THREAD_NAME_DISPATCHER, Simplicity.THREAD_PRIORITY_BACKGROUND);
        }
    }

    /**
     * 调度者内部的用于调度任务的线程的handler
     */
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
                case BATCH_SUCCESS_DELAY:
                    dispatcher.batchComplete();
                    break;
                default:
                    break;
            }
        }

    }

}
