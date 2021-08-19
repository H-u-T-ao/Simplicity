package com.fengjiaxing.picload;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.fengjiaxing.picload.Utils.*;

public class Simplicity {

    public static final String TAG = "SIMPLICITY";

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
                    @SuppressWarnings("unchecked")
                    Set<BitmapHunter> hunterSet = (Set<BitmapHunter>) msg.obj;
                    for (BitmapHunter hunterS : hunterSet) {
                        hunterS.simplicity.setBitmap(hunterS);
                    }
                    break;
                case REQUEST_FAIL:
                    BitmapHunter hunterF = (BitmapHunter) msg.obj;
                    hunterF.simplicity.requestFail(hunterF);
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

    private Simplicity(Context context, Dispatcher dispatcher, MemoryCache memoryCache,
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
    static volatile Simplicity simplicity = null;

    public static Simplicity get(Context context) {
        if (simplicity == null) {
            synchronized (Simplicity.class) {
                if (simplicity == null) {
                    simplicity = new Builder(context).build();
                }
            }
        }
        return simplicity;
    }

    public static void setSimplicityInstance(Simplicity instance) {
        if (instance == null) {
            throw new NullPointerException("设置的Simplicity对象不应为null");
        }
        synchronized (Simplicity.class) {
            if (simplicity != null) {
                throw new IllegalStateException("不能重复设置唯一的Simplicity对象");
            }
            simplicity = instance;
        }
    }

    private boolean shutdown;

    public void shutdown() {
        if (shutdown) {
            return;
        }
        dispatcher.shutdown();
        clearMemoryCache();
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
        RequestData data = hunterS.data;
        ImageView iv = hunterS.data.iv;
        String tag = data.uri != null ?
                data.uri.toString() : Integer.toString(data.resourceId);
        if (iv != null && tag.equals(iv.getTag())) {
            Bitmap bitmap = hunterS.getResult();
            bitmap.prepareToDraw();
            Drawable drawable = new SimplicityDrawable(context, bitmap);
            iv.setImageDrawable(drawable);
        }
        CallBack c = data.getCallBack();
        if (c != null) {
            c.success(hunterS);
        }
        // log(hunterS, true);
    }

    private void requestFail(BitmapHunter hunterF) {
        RequestData data = hunterF.data;
        ImageView iv = data.iv;
        if (data.errorDrawable != null && iv != null) {
            iv.setImageDrawable(data.errorDrawable);
        }
        CallBack c = hunterF.data.getCallBack();
        if (c != null) {
            c.fail(hunterF);
        }
        if (failSet != null) {
            failSet.add(hunterF);
        }
        // log(hunterF, false);
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
        private int corePoolSize;
        private int mode;
        private int stealLimit;
        private MemoryCache memoryCache;
        private List<RequestHandler> requestHandlerList;
        private HashSet<BitmapHunter> failSet;

        public Builder(Context context) {
            if (context == null) {
                throw new NullPointerException("上下文不应为空");
            }

            this.context = context.getApplicationContext();
        }

        public Simplicity build() {
            Context context = this.context;

            if (service == null) {
                corePoolSize = 5;
                service = new ThreadPoolExecutor(5, 5,
                        0, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
            }

            if (mode == 0) {
                mode = LIFO;
            }

            if (stealLimit == 0) {
                stealLimit = Integer.MAX_VALUE;
            }

            if (memoryCache == null) {
                memoryCache = new SimplicityMemoryCache();
            }

            MemoryCacheRequestHandler memoryCacheRequestHandler = new MemoryCacheRequestHandler();

            ResourceRequestHandler resourceRequestHandler = new ResourceRequestHandler();

            if (requestHandlerList == null) {
                requestHandlerList = new ArrayList<>();
                requestHandlerList.add(new FileStreamRequestHandler());
                requestHandlerList.add(new NetWorkRequestHandler());
            }

            Dispatcher dispatcher = new Dispatcher(mainHandler,
                    service, corePoolSize,
                    mode, stealLimit);

            return new Simplicity(context, dispatcher, memoryCache,
                    memoryCacheRequestHandler,
                    resourceRequestHandler,
                    requestHandlerList,
                    service, corePoolSize, mode,
                    failSet);

        }

        public Builder useFailSet(boolean b) {
            if (this.failSet != null) {
                throw new IllegalArgumentException("不能重复启用请求失败集合");
            }
            this.failSet = b ? new HashSet<>() : null;
            return this;
        }

        public Builder setStealLimit(int stealLimit) {
            if (this.stealLimit != 0) {
                throw new IllegalStateException("不能重复设置FIFO模式窃取限制");
            }
            if (stealLimit <= 0) {
                throw new IllegalArgumentException("设置的FIFO模式窃取限制不应为非正数");
            }
            this.stealLimit = stealLimit;

            return this;
        }

        public Builder setMode(int mode) {
            if (this.mode == FIFO || this.mode == LIFO) {
                throw new IllegalStateException("不能重复设置加载模式");
            }
            if (mode == FIFO || mode == LIFO) {
                this.mode = mode;
            } else {
                throw new IllegalArgumentException("设置的加载模式有误");
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
            this.corePoolSize = maxNumber;
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

        public Builder setMemoryCache(SimplicityMemoryCache memoryCache) {
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
