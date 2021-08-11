package com.fengjiaxing.hutao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.fengjiaxing.hutao.Utils.*;

public class HuTao {

    public static final String TAG = "HuTao";

    public static final String THREAD_NAME_DISPATCHER = "DISPATCHER";
    public static final int THREAD_PRIORITY_BACKGROUND = 10;

    public static final int FIFO = 101;
    public static final int LIFO = 102;

    public static final int REQUEST_SUCCESS = 1;
    public static final int REQUEST_FAIL = 2;

    static final Handler mainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case REQUEST_SUCCESS:
                    BitmapHunter hunterS = (BitmapHunter) msg.obj;
                    hunterS.huTao.setBitmap(hunterS);
                    break;
                case REQUEST_FAIL:
                    BitmapHunter hunterF = (BitmapHunter) msg.obj;
                    hunterF.huTao.requestFail(hunterF);
                    break;
            }
        }
    };

    final Context context;
    final Dispatcher dispatcher;
    final MemoryCache memoryCache;
    final MemoryCacheRequestHandler memoryCacheRequestHandler;
    final ResourceRequestHandler resourceRequestHandler;
    final List<RequestHandler> requestHandlerList;
    final ExecutorService service;
    final int maxNumber;
    final int mode;
    private final HashSet<BitmapHunter> failSet;

    private HuTao(Context context, Dispatcher dispatcher, MemoryCache memoryCache,
                  MemoryCacheRequestHandler memoryCacheRequestHandler,
                  ResourceRequestHandler resourceRequestHandler,
                  List<RequestHandler> requestHandlerList,
                  ExecutorService service, int maxNumber, int mode,
                  HashSet<BitmapHunter> failSet) {
        this.context = context;
        this.dispatcher = dispatcher;
        this.memoryCache = memoryCache;
        this.memoryCacheRequestHandler = memoryCacheRequestHandler;
        this.resourceRequestHandler = resourceRequestHandler;
        this.requestHandlerList = requestHandlerList;
        this.service = service;
        this.maxNumber = maxNumber;
        this.mode = mode;
        this.failSet = failSet;
    }

    @SuppressLint("StaticFieldLeak")
    static HuTao huTao = null;

    public static HuTao get(Context context) {
        if (huTao == null) {
            synchronized (HuTao.class) {
                if (huTao == null) {
                    huTao = new Builder(context).build();
                }
            }
        }
        return huTao;
    }

    /**
     * 根据{@linkplain Uri}加载位图
     *
     * @param uri 位图的Uri
     */
    public RequestBuilder load(Uri uri) {
        return new RequestBuilder(this, uri);
    }

    /**
     * 根据文件加载位图
     *
     * @param file 位图文件
     */
    public RequestBuilder load(File file) {
        if (file == null) {
            throw new NullPointerException("位图文件不应为空");
        }

        return load(Uri.fromFile(file));
    }

    public RequestBuilder load(int resourceId) {
        if (resourceId <= 0) {
            throw new NullPointerException("资源文件ID不应为一个非正数");
        }

        return new RequestBuilder(this, resourceId);
    }

    /**
     * 根据路径加载位图
     *
     * <p>路径最终转换为{@linkplain Uri}
     *
     * @param path 位图的路径
     */
    public RequestBuilder load(String path) {
        if (path.trim().isEmpty()) {
            throw new IllegalArgumentException("位图路径不应为空");
        }
        return load(Uri.parse(path));
    }

    private void setBitmap(BitmapHunter hunterS) {
        Bitmap bitmap = hunterS.getResult();
        BitmapDrawable drawable = new BitmapDrawable(bitmap);
        ImageView iv = hunterS.data.iv;
        iv.setImageDrawable(drawable);
        log(hunterS, true);
    }

    private void requestFail(BitmapHunter hunterF) {
        RequestData data = hunterF.data;
        ImageView iv = data.iv;
        if (data.errorDrawable != null) {
            iv.setImageDrawable(data.errorDrawable);
        }
        if (failSet != null) {
            failSet.add(hunterF);
        }
        log(hunterF, false);
    }

    public HashSet<BitmapHunter> getFailSet() {
        if (failSet == null) {
            throw new IllegalArgumentException("尚未启用请求失败集合");
        }
        return failSet;
    }

    public void clearMemoryCache() {
        memoryCache.clear();
    }

    private static int TASK_TOTAL = 0;

    public static int getTaskTotal() {
        return TASK_TOTAL;
    }

    void prepareToExecute(RequestData data) {
        TASK_TOTAL++;
        dispatcher.execute(data);
    }

    public static class Builder {

        private final Context context;
        private ExecutorService service;
        private int maxNumber;
        private int mode;
        private MemoryCache memoryCache;
        private List<RequestHandler> requestHandlerList;
        private HashSet<BitmapHunter> failSet;

        public Builder(Context context) {
            if (context == null) {
                throw new NullPointerException("上下文不应为空");
            }

            this.context = context.getApplicationContext();
        }

        public HuTao build() {
            Context context = this.context;

            if (service == null) {
                maxNumber = 5;
                service = new ThreadPoolExecutor(5, 5,
                        0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(0));
            }

            if (mode == 0) {
                mode = FIFO;
            }

            if (memoryCache == null) {
                memoryCache = new HuTaoMemoryCache();
            }

            MemoryCacheRequestHandler memoryCacheRequestHandler = new MemoryCacheRequestHandler();

            ResourceRequestHandler resourceRequestHandler = new ResourceRequestHandler();

            if (requestHandlerList == null) {
                requestHandlerList = new ArrayList<>();
                requestHandlerList.add(new FileStreamRequestHandler());
                requestHandlerList.add(new NetWorkRequestHandler());
            }

            Dispatcher dispatcher = new Dispatcher(mainHandler, service, maxNumber, mode);

            return new HuTao(context, dispatcher, memoryCache,
                    memoryCacheRequestHandler,
                    resourceRequestHandler,
                    requestHandlerList,
                    service, maxNumber, mode,
                    failSet);

        }

        public Builder useFailSet(boolean b) {
            if (this.failSet != null) {
                throw new NullPointerException("不能重复启用请求失败集合");
            }
            this.failSet = b ? new HashSet<>() : null;
            return this;
        }

        public Builder setMode(int mode) {
            if (this.mode == FIFO || this.mode == LIFO) {
                throw new IllegalStateException("不能重复设置加载模式");
            }
            if (mode == FIFO || mode == LIFO) {
                this.mode = mode;
            } else {
                throw new IllegalArgumentException("设置的加载模式参数有误");
            }

            return this;
        }

        public Builder setExecutorService(ExecutorService service, int maxNumber) {
            if (service == null) {
                throw new NullPointerException("设置的线程服务实现类不应为空");
            }
            if (maxNumber <= 0) {
                throw new IllegalArgumentException("允许同时处理的图片数量不应为非正数");
            }
            if (this.service != null) {
                throw new IllegalStateException("不能重复设置线程服务实现类");
            }
            this.maxNumber = maxNumber;
            this.service = service;

            return this;
        }

        public Builder setRequestHandlerList(List<RequestHandler> requestHandlerList) {
            if (requestHandlerList == null) {
                throw new NullPointerException("设置的请求处理程序集合不应为空");
            }
            if (this.requestHandlerList != null) {
                throw new IllegalStateException("不能重复设置请求处理程序集合");
            }
            this.requestHandlerList = requestHandlerList;

            return this;
        }

        public Builder setMemoryCache(HuTaoMemoryCache memoryCache) {
            if (memoryCache == null) {
                throw new NullPointerException("设置的内存缓存实现类不应为空");
            }
            if (this.memoryCache != null) {
                throw new IllegalStateException("不能重复设置内存缓存实现类");
            }
            this.memoryCache = memoryCache;

            return this;
        }
    }

}
