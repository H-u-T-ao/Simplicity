package com.fengjiaxing.simplicity.ImagePreview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fengjiaxing.picload.BitmapHunter;
import com.fengjiaxing.picload.CallBack;
import com.fengjiaxing.picload.Simplicity;
import com.fengjiaxing.simplicity.SimplicityApplication;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 图片预览ImagePreviewView
 */
public class ImagePreviewView extends androidx.appcompat.widget.AppCompatImageView
        implements View.OnTouchListener {

    /**
     * 上一张图片的信息{@link BitmapInfo}
     */
    private BitmapInfo l;
    /**
     * 中心图片的信息{@link BitmapInfo}
     */
    private BitmapInfo c;
    /**
     * 下一张图片的信息{@link BitmapInfo}
     */
    private BitmapInfo n;

    /**
     * 当前中心图片的索引
     */
    private int index;

    public ImagePreviewView(Context context) {
        super(context);
        init();
    }

    public ImagePreviewView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImagePreviewView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
    }

    /**
     * 加载中或加载失败时显示的图片 {@link ImagePreviewView#setErrorBitmap(Bitmap)}
     */
    private static Bitmap errorBitmap;

    /**
     * 设置加载中或加载失败时显示的图片
     */
    public void setErrorBitmap(Bitmap errorBitmap) {
        ImagePreviewView.errorBitmap = errorBitmap;
    }

    /**
     * 动画速度倍率，建议设置整数
     */
    private int animSpeed = 1;

    public void setAnimSpeed(int x) {
        if (x <= 0) {
            throw new IllegalArgumentException("动画速度倍率不能小于等于零");
        }
        animSpeed = x;
    }

    private Paint paint;
    /**
     * 控制上一张图片位置，大小的矩阵
     */
    private Matrix matrixL;
    /**
     * 控制中心图片位置，大小的矩阵
     */
    private Matrix matrixC;
    /**
     * 控制下一张图片位置，大小的矩阵
     */
    private Matrix matrixN;

    /**
     * 图片与图片之间的间隙大小
     */
    private static final int interval = 40;

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        paint = new Paint();
        matrixL = new Matrix();
        matrixC = new Matrix();
        matrixN = new Matrix();
        this.setOnTouchListener(this);
    }

    /**
     * 控制特定图片的平移
     *
     * @param tmx    平移的目的地的X轴坐标
     * @param tmy    平移的目的地的Y轴坐标
     * @param matrix 要平移的图片对应的矩阵
     */
    private void setTranslate(float tmx, float tmy, Matrix matrix) {
        float[] values = new float[9];
        matrix.getValues(values);
        values[2] = tmx;
        values[5] = tmy;
        matrix.setValues(values);
    }

    /**
     * 控制三张图片同步平移（由平移量确定位置）
     *
     * @param moveX 平移的X轴上的位移量
     * @param moveY 平移的Y轴上的位移量
     */
    private void postTranslate(float moveX, float moveY) {
        float[] values = new float[9];
        matrixC.getValues(values);
        values[2] = values[2] + moveX;
        values[5] = values[5] + moveY;
        matrixC.setValues(values);
        c.tmx = c.tmx + moveX;
        c.tmy = c.tmy + moveY;
        if (l != null) {
            matrixL.getValues(values);
            values[2] = values[2] + moveX;
            matrixL.setValues(values);
            l.tmx = l.tmx + moveX;
        }
        if (n != null) {
            matrixN.getValues(values);
            values[2] = values[2] + moveX;
            matrixN.setValues(values);
            n.tmx = n.tmx + moveX;
        }
    }

    /**
     * 控制三张图片同步平移（由中心图片目的地确定位置）
     *
     * @param tmx 中心图片目的地的X轴坐标
     * @param tmy 中心图片目的地的Y轴坐标
     */
    private void homingAll(float tmx, float tmy) {
        setTranslate(tmx, tmy, matrixC);
        c.tmx = tmx;
        c.tmy = tmy;
        if (l != null) {
            float tmxL = tmx - sWidth - interval;
            setTranslate(tmxL, l.tmy, matrixL);
            l.tmx = tmxL;
        }
        if (n != null) {
            float tmxN = tmx + nm * c.width + interval;
            setTranslate(tmxN, n.tmy, matrixN);
            n.tmx = tmxN;
        }
    }

    /**
     * 缩放中心图片
     * <p>
     * 根据用户的触摸位置进行缩放，达到跟手效果
     *
     * @param m 缩放倍率
     */
    private void setScale(float m) {
        matrixC.setScale(m, m);
        // 计算应该移动的距离
        if (nm != c.dm) {
            c.tmx = adx - m * rdx;
            c.tmy = ady - m * rdy;
        }
        setTranslate(c.tmx, c.tmy, matrixC);
        if (l != null) {
            l.tmx = c.tmx - sWidth - interval;
            setTranslate(l.tmx, l.tmy, matrixL);
        }
        if (n != null) {
            n.tmx = c.tmx + nm * c.width + interval;
            setTranslate(n.tmx, n.tmy, matrixN);
        }
    }

    /**
     * 要显示的图片列表，使用弱引用，防止图片过多无法回收
     */
    private List<WeakReference<Bitmap>> bitmapList;

    /**
     * 要加载图片的Uri列表 {@link ImagePreviewView#setBitmapUriList(List)}
     */
    private List<Uri> uriList;

    /**
     * 设置要加载图片的Uri列表
     *
     * @param fileList 要加载图片的Uri列表
     */
    public void setBitmapUriList(List<Uri> fileList) {
        this.uriList = fileList;
        int s = fileList.size();
        this.bitmapList = new ArrayList<>();
        for (int i = 0; i < s; i++) {
            bitmapList.add(null);
        }
    }

    /**
     * 预加载图片数 {@link ImagePreviewView#setPreSize(int)}
     */
    private int preSize = 5;

    /**
     * 设置预加载图片数
     *
     * @param preSize 预加载图片数
     */
    public void setPreSize(int preSize) {
        this.preSize = preSize;
    }

    /**
     * 最开始展示的图片在列表中的索引 {@link ImagePreviewView#setBeginIndex(int)}
     */
    private static int beginIndex;

    /**
     * 设置最开始展示的图片在列表中的索引
     *
     * @param beginIndex 最开始展示的图片在列表中的索引
     */
    public void setBeginIndex(int beginIndex) {
        ImagePreviewView.beginIndex = beginIndex;
    }

    /**
     * 动画帧数 {@link ImagePreviewView#setRefreshRate(float)}
     */
    private float unitTime = 1000 / 60F;

    /**
     * 设置动画帧数
     *
     * @param refreshRate 刷新率
     */
    public void setRefreshRate(float refreshRate) {
        this.unitTime = (long) (1000 / refreshRate);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * View的宽
     */
    private float sWidth;
    /**
     * View的高
     */
    private float sHeight;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // super.onLayout(changed, left, top, right, bottom);
        initBitmapInfo();
    }

    /**
     * 初始化图片信息
     */
    private void initBitmapInfo() {
        if (c == null) {
            sWidth = getWidth();
            sHeight = getHeight();
            index = beginIndex;
            loadPrePic();
            if (errorBitmap == null) {
                errorBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_4444);
                Canvas canvas = new Canvas(errorBitmap);
                canvas.drawColor(Color.BLACK);
            }
            WeakReference<Bitmap> wBitmap;
            if (index - 1 >= 0) {
                wBitmap = bitmapList.get(index - 1);
                Bitmap last = (wBitmap == null) ? null : wBitmap.get();
                l = new BitmapInfo(last, sWidth, sHeight, BitmapInfo.LAST);
                matrixL.setScale(l.dm, l.dm);
                setTranslate(l.tmx, l.tmy, matrixL);
            }
            if (index >= 0) {
                wBitmap = bitmapList.get(index);
                Bitmap center = (wBitmap == null) ? null : wBitmap.get();
                c = new BitmapInfo(center, sWidth, sHeight, BitmapInfo.CENTER);
                matrixC.setScale(c.dm, c.dm);
                setTranslate(c.tmx, c.tmy, matrixC);
            }
            if (index + 1 <= bitmapList.size() - 1) {
                wBitmap = bitmapList.get(index + 1);
                Bitmap next = (wBitmap == null) ? null : wBitmap.get();
                n = new BitmapInfo(next, sWidth, sHeight, BitmapInfo.NEXT);
                matrixN.setScale(n.dm, n.dm);
                setTranslate(n.tmx, n.tmy, matrixN);
                m = nm = c.dm;
            }
        }
    }

    /**
     * 根据上一张图片，中心图片和下一张图片所对应的矩阵更新图片的大小和位置
     */
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(c.b, matrixC, paint);
        if (l != null) {
            canvas.drawBitmap(l.b, matrixL, paint);
        }
        if (n != null) {
            canvas.drawBitmap(n.b, matrixN, paint);
        }
    }


    /**
     * 单个手指触摸时，手指的绝对X坐标
     */
    private float x = 0;
    /**
     * 单个手指触摸时，手指的绝对Y坐标
     */
    private float y = 0;


    // 标准焦点的绝对坐标
    /**
     * 双指触控时，双指连线的中点的绝对X坐标
     */
    private float adx;
    /**
     * 双指触控时，双指连线的中点的绝对Y坐标
     */
    private float ady;


    // 标准焦点的相对坐标
    /**
     * 双指触控时，双指连线的中点的相对X坐标
     */
    private float rdx;
    /**
     * 双指触控时，双指连线的中点的相对Y坐标
     */
    private float rdy;


    /**
     * 两个手指之间的标准距离
     */
    private float d;

    /**
     * 中心图片的标准缩放倍率
     */
    private float m;

    /**
     * 第二只手指抬起并且事件未处理时，该值为true
     */
    private boolean su = false;

    /**
     * 中心图片的实时缩放倍率
     */
    private float nm;

    /**
     * 正在触摸时，该值为true
     */
    private boolean touching;

    /**
     * 图片平移缩放的一系列手势逻辑控制
     */
    @Override
    public final boolean onTouch(View v, MotionEvent event) {
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                touching = true;
                if (timerA != null) {
                    timerA.cancel();
                }
                if (timerI != null) {
                    timerI.cancel();
                }
                x = event.getX();
                y = event.getY();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) {
                    m = nm;
                    float x = event.getX(0) - event.getX(1);
                    float y = event.getY(0) - event.getY(1);
                    d = (float) Math.sqrt(x * x + y * y);
                    adx = (event.getX(0) + event.getX(1)) / 2;
                    ady = (event.getY(0) + event.getY(1)) / 2;
                    // 计算标准焦点的相对坐标
                    rdx = (adx - c.tmx) / m;
                    rdy = (ady - c.tmy) / m;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1) {
                    if (su) {
                        su = false;
                        x = event.getX();
                        y = event.getY();
                    }
                    float nx = event.getX();
                    float ny = event.getY();
                    // 计算要移动的位移
                    float moveX = nx - x;
                    float moveY = ny - y;
                    postTranslate(moveX, moveY);
                    postInvalidate();
                    x = nx;
                    y = ny;
                    speedX = moveX;
                    speedY = moveY;
                } else if (event.getPointerCount() == 2) {
                    // 实时计算焦点的绝对坐标，达到跟随效果
                    adx = (event.getX(0) + event.getX(1)) / 2;
                    ady = (event.getY(0) + event.getY(1)) / 2;

                    float ndx = event.getX(0) - event.getX(1);
                    float ndy = event.getY(0) - event.getY(1);
                    float nd = (float) Math.sqrt(ndx * ndx + ndy * ndy);
                    float newNm = (nd / d) * m;

                    if (newNm > (0.9 * c.dm)) {
                        nm = newNm;
                    }
                    setScale(nm);
                    postInvalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                touching = false;
                if (l != null
                        && ((l.tmx + sWidth) > (0.3 * sWidth)
                        || ((l.tmx > -sWidth) && speedX > 5))) {
                    changeAnim(false);
                } else if (n != null
                        && (n.tmx < (0.7 * sWidth)
                        || ((n.tmx < sWidth) && speedX < -5))) {
                    changeAnim(true);
                } else {
                    if (!adjustPosition()) {
                        inertialMovement();
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() == 2) {
                    su = true;
                }
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 切换图片时的图片切换动画效果（自动归位）
     */
    private void changeAnim(boolean next) {
        this.setEnabled(false);
        num = 0;
        Timer timerAnim = new Timer();
        if (next) {
            float moveX = animSpeed * (0 - n.tmx) / unitTime;
            float moveY = animSpeed * (((sHeight - nm * c.height) / 2) - c.tmy) / unitTime;
            timerAnim.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (touching) {
                        change(true);
                        timerAnim.cancel();
                    }
                    if (num < unitTime / animSpeed) {
                        postTranslate(moveX, moveY);
                        postInvalidate();
                        num++;
                    } else {
                        change(true);
                        timerAnim.cancel();
                    }
                }
            }, 0, (long) unitTime / animSpeed);
        } else {
            float moveX = animSpeed * (0 - l.tmx) / unitTime;
            float moveY = animSpeed * (((sHeight - nm * c.height) / 2) - c.tmy) / unitTime;
            timerAnim.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (touching) {
                        change(false);
                        timerAnim.cancel();
                    }
                    if (num < unitTime / animSpeed) {
                        postTranslate(moveX, moveY);
                        postInvalidate();
                        num++;
                    } else {
                        change(false);
                        timerAnim.cancel();
                    }
                }
            }, 0, (long) unitTime / animSpeed);
        }
    }

    /**
     * 切换图片
     */
    private void change(boolean next) {
        if (next) {
            if (index + 2 <= bitmapList.size() - 1) {
                index++;
                loadPrePic();
                WeakReference<Bitmap> wBitmap = bitmapList.get(index + 1);
                Bitmap b = (wBitmap == null) ? null : wBitmap.get();
                l = c.changePosition(sWidth, sHeight, BitmapInfo.LAST);
                c = n.changePosition(sWidth, sHeight, BitmapInfo.CENTER);
                n = new BitmapInfo(b, sWidth, sHeight, BitmapInfo.NEXT);
                if (onPictureChangeListener != null) {
                    onPictureChangeListener.onChangeNext(index);
                }
            } else if (index + 1 == bitmapList.size() - 1) {
                index++;
                loadPrePic();
                l = c.changePosition(sWidth, sHeight, BitmapInfo.LAST);
                c = n.changePosition(sWidth, sHeight, BitmapInfo.CENTER);
                n = null;
                if (onPictureChangeListener != null) {
                    onPictureChangeListener.onChangeNext(index);
                }
            } else {
                return;
            }
        } else {
            if (index - 1 == 0) {
                index--;
                loadPrePic();
                n = c.changePosition(sWidth, sHeight, BitmapInfo.NEXT);
                c = l.changePosition(sWidth, sHeight, BitmapInfo.CENTER);
                l = null;
                if (onPictureChangeListener != null) {
                    onPictureChangeListener.onChangeLast(index);
                }
            } else if (index - 1 > 0) {
                index--;
                loadPrePic();
                WeakReference<Bitmap> wBitmap = bitmapList.get(index - 1);
                Bitmap b = (wBitmap == null) ? null : wBitmap.get();
                n = c.changePosition(sWidth, sHeight, BitmapInfo.NEXT);
                c = l.changePosition(sWidth, sHeight, BitmapInfo.CENTER);
                l = new BitmapInfo(b, sWidth, sHeight, BitmapInfo.LAST);
                if (onPictureChangeListener != null) {
                    onPictureChangeListener.onChangeLast(index);
                }
            } else {
                return;
            }
        }
        matrixC.setScale(c.dm, c.dm);
        setTranslate(c.tmx, c.tmy, matrixC);
        m = nm = c.dm;
        if (l != null) {
            matrixL.setScale(l.dm, l.dm);
            setTranslate(l.tmx, l.tmy, matrixL);
        }
        if (n != null) {
            matrixN.setScale(n.dm, n.dm);
            setTranslate(n.tmx, n.tmy, matrixN);
        }
        postInvalidate();
        mainHandler.handleMessage(mainHandler.obtainMessage(ENABLE));
    }

    /**
     * 更换图片监听器
     */
    private OnPictureChangeListener onPictureChangeListener;

    /**
     * 更换图片监听接口
     */
    public interface OnPictureChangeListener {
        void onChangeLast(int index);

        void onChangeNext(int index);
    }

    /**
     * 设置更换图片监听器
     */
    public void setOnPictureChangeListener(OnPictureChangeListener listener) {
        this.onPictureChangeListener = listener;
    }

    /**
     * 松手前最后一次手指挪动的速度
     */
    private static float hypotenuse;
    /**
     * 松手前最后一次手指挪动的X方向上的速度
     */
    private static float speedX;
    /**
     * 松手前最后一次手指挪动的Y方向上的速度
     */
    private static float speedY;
    /**
     * 松手前最后一次手指挪动方向的正弦值（以X轴正方向为零角）
     */
    private static float sin;
    /**
     * 松手前最后一次手指挪动方向的余弦值（以X轴正方向为零角）
     */
    private static float cos;
    /**
     * 用于控制惯性运动的计时器
     */
    private Timer timerI;

    /**
     * 松手后的惯性运动
     */
    private void inertialMovement() {
        hypotenuse = (float) Math.sqrt(speedX * speedX + speedY * speedY);
        if (hypotenuse == 0) return;
        sin = speedY / hypotenuse;
        cos = speedX / hypotenuse;
        speedX = 0;
        speedY = 0;
        timerI = new Timer();
        timerI.schedule(new TimerTask() {
            @Override
            public void run() {
                if (touching) {
                    timerI.cancel();
                }
                if (hypotenuse > 0) {
                    float endX = c.tmx + cos * hypotenuse;
                    float endY = c.tmy + sin * hypotenuse;
                    boolean bx = crossBoundaryX();
                    boolean by = crossBoundaryY();
                    // 如果X越界，则重新设置X位置
                    if (bx) {
                        // 如果是左越界，即左边存在黑条
                        if (endX >= maxX) {
                            endX = maxX;
                        } else {
                            // 如果是右越界，即右边存在黑条
                            endX = minX;
                        }
                    }
                    // 如果Y越界，则重新设置Y位置
                    if (by) {
                        // 如果是上越界，即上边存在黑条
                        if (endY >= maxY) {
                            endY = maxY;
                        } else {
                            // 如果是下越界，即右边存在黑条
                            endY = minY;
                        }
                    }
                    homingAll(endX, endY);
                    postInvalidate();

                    hypotenuse = hypotenuse - animSpeed * 5 / unitTime;
                } else {
                    hypotenuse = 0;
                    timerI.cancel();
                }
            }
        }, 0, (long) unitTime / animSpeed);
    }

    /**
     * 当前图片自动归位的最终缩放倍率
     */
    private static float endM;
    /**
     * 当前图片自动归位的最终X坐标
     */
    private static float endX;
    /**
     * 当前图片自动归位的最终Y坐标
     */
    private static float endY;
    private static int num;

    private Timer timerA;

    /**
     * 调整因越界或缩放大小不符合要求的图片位置和图片大小（当前图片自动归位）
     */
    private boolean adjustPosition() {
        // 如果松手的时候大小小于默认大小
        float startM = endM = nm;
        if (nm < c.dm) {
            endM = c.dm;
        }
        float startX = endX = c.tmx;
        float startY = endY = c.tmy;
        boolean bx = crossBoundaryX();
        boolean by = crossBoundaryY();
        if (!bx && !by) return false;
        // 如果X越界，则重新设置X位置
        if (bx) {
            // 如果是左越界，即左边存在黑条
            if (c.tmx >= maxX) {
                endX = maxX;
            } else {
                // 如果是右越界，即右边存在黑条
                endX = minX;
            }
        }
        // 如果Y越界，则重新设置Y位置
        if (by) {
            // 如果是上越界，即上边存在黑条
            if (c.tmy >= maxY) {
                endY = maxY;
            } else {
                // 如果是下越界，即右边存在黑条
                endY = minY;
            }
        }
        num = 0;
        float moveX = animSpeed * (endX - startX) / unitTime;
        float moveY = animSpeed * (endY - startY) / unitTime;
        float moveM = animSpeed * (endM - startM) / unitTime;
        timerA = new Timer();
        timerA.schedule(new TimerTask() {
            @Override
            public void run() {
                if (touching) {
                    timerA.cancel();
                }
                if (num < unitTime / animSpeed) {
                    nm = nm + moveM;
                    float tmx = c.tmx + moveX;
                    float tmy = c.tmy + moveY;
                    setScale(nm);
                    homingAll(tmx, tmy);
                    postInvalidate();
                    num++;
                } else {
                    nm = endM;
                    setScale(nm);
                    homingAll(endX, endY);
                    postInvalidate();
                    timerA.cancel();
                }
            }
        }, 0, (long) unitTime / animSpeed);
        return true;
    }

    /**
     * 归位前计算的图片的最小X坐标
     */
    private static float minX;
    /**
     * 归位前计算的图片的最大X坐标
     */
    private static float maxX;

    /**
     * 检查X方向上是否越界
     */
    private boolean crossBoundaryX() {
        // 计算x方向上的最值
        minX = sWidth - c.width * endM;
        maxX = 0;
        // 判断图片在x方向上是否过界
        return c.tmx <= minX || c.tmx >= maxX;
    }

    /**
     * 归位前计算的图片的最小Y坐标
     */
    private static float minY;
    /**
     * 归位前计算的图片的最大Y坐标
     */
    private static float maxY;

    /**
     * 检查Y方向上是否越界
     */
    private boolean crossBoundaryY() {
        // 计算y方向上的最值
        if (c.height * endM < getHeight()) {
            minY = (sHeight - c.height * endM) / 2;
            maxY = minY;
        } else {
            minY = sHeight - c.height * endM;
            maxY = 0;
        }
        // 判断图片在y方向上是否过界
        return c.tmy <= minY || c.tmy >= maxY;
    }

    private static final int LOAD_PIC = 0;
    private static final int ENABLE = 1;

    /**
     * 加载图片和设置View的可触摸属性需要在主线程执行
     */
    private final Handler mainHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case LOAD_PIC:
                    int i = (int) msg.obj;
                    mainHandler.post(() -> loadPic(i));
                    break;
                case ENABLE:
                    mainHandler.post(() -> setEnabled(true));
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 根据设置的预加载图片数量 {@link ImagePreviewView#preSize} 确定要加载的图片的索引
     * 并发送消息调用 {@link ImagePreviewView#loadPic(int)} 进行加载
     */
    private void loadPrePic() {
        for (int i = 0; i < preSize; i++) {
            int j = ((index - preSize / 2) + i);
            if (j >= 0 && j <= uriList.size() - 1
                    && (bitmapList.get(j) == null
                    || (bitmapList.get(j) != null
                    && bitmapList.get(j).get() == null))) {
                mainHandler.handleMessage(mainHandler.obtainMessage(LOAD_PIC, j));
            }
        }
    }

    /**
     * 加载指定索引的图片
     */
    private void loadPic(int index) {
        Uri uri = uriList.get(index);
        Simplicity.get(SimplicityApplication.getApplication())
                .load(uri)
                .into(new PreviewViewCallBack(index));
    }

    /**
     * 获取加载图片的CallBack
     */
    private class PreviewViewCallBack implements CallBack {
        private final int i;

        private PreviewViewCallBack(int i) {
            this.i = i;
        }

        @Override
        public void success(BitmapHunter hunter) {
            Bitmap bitmap = hunter.getResult();
            WeakReference<Bitmap> wBitmap = new WeakReference<>(bitmap);
            bitmapList.set(i, wBitmap);
            if (i == index - 1) {
                l.setBitmap(bitmap, sWidth, sHeight);
                matrixL.setScale(l.dm, l.dm);
                setTranslate(l.tmx, l.tmy, matrixL);
            } else if (i == index) {
                c.setBitmap(bitmap, sWidth, sHeight);
                matrixC.setScale(c.dm, c.dm);
                setTranslate(c.tmx, c.tmy, matrixC);
                m = nm = c.dm;
            } else if (i == index + 1) {
                n.setBitmap(bitmap, sWidth, sHeight);
                matrixN.setScale(n.dm, n.dm);
                setTranslate(n.tmx, n.tmy, matrixN);
            }
            postInvalidate();
        }

        @Override
        public void fail(BitmapHunter hunter) {
            // 加载是失败时不用做额外操作，因为位图为空会默认设置为设置好的错误图片
        }
    }

    /**
     * 存储在屏幕上显示的图片（包括上一张，中心和下一张）的信息
     */
    private static class BitmapInfo {

        private static final int LAST = -1;
        private static final int CENTER = 0;
        private static final int NEXT = 1;

        private BitmapInfo(Bitmap b, float sWidth, float sHeight, int position) {
            if (b != null) {
                this.b = b;
            } else {
                this.b = ImagePreviewView.errorBitmap;
            }
            if (b != null) {
                width = b.getWidth();
                height = b.getHeight();
            }
            dm = sWidth / width;
            tmy = (sHeight - height * dm) / 2;
            switch (position) {
                case LAST:
                    tmx = -sWidth - interval;
                    break;
                case CENTER:
                    tmx = 0;
                    break;
                case NEXT:
                    tmx = sWidth + interval;
                    break;
                default:
                    break;
            }
        }

        /**
         * 图片
         */
        private Bitmap b;

        /**
         * 图片的绝对宽
         */
        private float width;
        /**
         * 图片的绝对高
         */
        private float height;

        /**
         * 让图片的宽度溢满屏幕的标准缩放倍率
         */
        private float dm;

        /**
         * 图片的X坐标
         */
        private float tmx;
        /**
         * 图片的Y坐标
         */
        private float tmy;

        /**
         * 根据图片是上一张，中间或下一张改变图片的位置
         *
         * @param sWidth   屏幕的宽
         * @param sHeight  屏幕的高
         * @param position 图片的位置（上一张，中间或下一张）
         */
        private BitmapInfo changePosition(float sWidth, float sHeight, int position) {
            switch (position) {
                case LAST:
                    tmx = -sWidth - interval;
                    tmy = (sHeight - height * dm) / 2;
                    break;
                case CENTER:
                    tmx = 0;
                    tmy = (sHeight - height * dm) / 2;
                    break;
                case NEXT:
                    tmx = sWidth + interval;
                    tmy = (sHeight - height * dm) / 2;
                    break;
                default:
                    break;
            }
            return this;
        }

        /**
         * 重新设置图片
         *
         * @param b       位图
         * @param sWidth  屏幕的宽
         * @param sHeight 屏幕的高
         */
        private void setBitmap(Bitmap b, float sWidth, float sHeight) {
            if (b != null) {
                this.b = b;
                width = b.getWidth();
                height = b.getHeight();
            } else {
                this.b = ImagePreviewView.errorBitmap;
                width = errorBitmap.getWidth();
                height = errorBitmap.getHeight();
            }
            dm = sWidth / width;
            tmy = (sHeight - height * dm) / 2;
        }

    }

}
