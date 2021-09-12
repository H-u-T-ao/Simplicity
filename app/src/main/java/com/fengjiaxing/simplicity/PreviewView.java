package com.fengjiaxing.simplicity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fengjiaxing.picload.BitmapHunter;
import com.fengjiaxing.picload.CallBack;
import com.fengjiaxing.picload.Simplicity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 图片预览ImagePreviewView
 */
public class PreviewView extends androidx.appcompat.widget.AppCompatImageView
        implements View.OnTouchListener {

    private static final int LAST = -1;
    private static final int CENTER = 0;
    private static final int NEXT = 1;

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

    public PreviewView(Context context) {
        super(context);
        errorBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(errorBitmap);
        canvas.drawColor(Color.BLACK);
        init();
    }

    public PreviewView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        errorBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(errorBitmap);
        canvas.drawColor(Color.BLACK);
        init();
    }

    public PreviewView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        errorBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(errorBitmap);
        canvas.drawColor(Color.BLACK);
        init();
    }

    /**
     * 加载中或加载失败时显示的图片
     */
    private final Bitmap errorBitmap;

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
     * 重置越界
     */
    private void resetCross(float fm) {
        float minX = sWidth - fm * c.width;
        float maxX = 0;
        if (c.tmx < minX) {
            c.tmx = minX;
        } else {
            c.tmx = Math.min(c.tmx, maxX);
        }
        float minY;
        float maxY;
        if (c.height * fm < getHeight()) {
            maxY = minY = (sHeight - c.height * fm) / 2;
        } else {
            minY = sHeight - c.height * fm;
            maxY = 0;
        }
        if (c.tmy < minY) {
            c.tmy = minY;
        } else {
            c.tmy = Math.min(c.tmy, maxY);
        }
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
        matrixL.getValues(values);
        values[2] = values[2] + moveX;
        matrixL.setValues(values);
        l.tmx = l.tmx + moveX;
        matrixN.getValues(values);
        values[2] = values[2] + moveX;
        matrixN.setValues(values);
        n.tmx = n.tmx + moveX;
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
        float tmxL = tmx - sWidth - interval;
        setTranslate(tmxL, l.tmy, matrixL);
        l.tmx = tmxL;
        float tmxN = tmx + nm * c.width + interval;
        setTranslate(tmxN, n.tmy, matrixN);
        n.tmx = tmxN;
    }

    /**
     * 缩放中心图片
     * <p>
     * 根据用户的触摸位置进行缩放，达到跟手效果
     *
     * @param fm 缩放倍率
     */
    private void setScale(float fm) {
        matrixC.setScale(fm, fm);
        // 计算应该移动的距离
        c.tmx = adx - fm * rdx;
        c.tmy = ady - fm * rdy;
        resetCross(fm);
        setTranslate(c.tmx, c.tmy, matrixC);
        l.tmx = c.tmx - sWidth - interval;
        setTranslate(l.tmx, l.tmy, matrixL);
        n.tmx = c.tmx + fm * c.width + interval;
        setTranslate(n.tmx, n.tmy, matrixN);
        nm = fm;
    }

    /**
     * 要显示的图片列表，使用弱引用，防止图片过多无法回收
     */
    private List<WeakReference<Bitmap>> bitmapList;

    /**
     * 要加载图片的Uri列表 {@link PreviewView#setBitmapUriList(List)}
     */
    private List<Uri> uriList;

    /**
     * 设置要加载图片的Uri列表
     *
     * @param picList 要加载图片的Uri列表
     */
    public void setBitmapUriList(List<Uri> picList) {
        this.uriList = picList;
        int s = picList.size();
        this.bitmapList = new ArrayList<>();
        for (int i = 0; i < s; i++) {
            bitmapList.add(null);
        }
    }

    void swapList(int from, int to) {
        Collections.swap(bitmapList, from, to);
    }

    void swapFinish(int from, int to) {
        if (index == from) {
            index = to;
            if (onPictureChangeListener != null) {
                onPictureChangeListener.change(index);
            }
        } else if (index == to) {
            index = from;
            if (onPictureChangeListener != null) {
                onPictureChangeListener.change(index);
            }
        }
        initBitmapInfo();
    }

    /**
     * 预加载图片数
     */
    private int preSize = 4;

    /**
     * 设置预加载图片数
     */
    public void setPreSize(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("设置的预加载图片数不能为负数");
        } else if (size % 2 == 1) {
            throw new IllegalArgumentException("预加载图片数应为偶数");
        }
        preSize = size;
    }

    /**
     * 最开始展示的图片在列表中的索引 {@link PreviewView#setBeginIndex(int)}
     */
    private static int beginIndex;

    /**
     * 设置最开始展示的图片在列表中的索引
     *
     * @param beginIndex 最开始展示的图片在列表中的索引
     */
    public void setBeginIndex(int beginIndex) {
        PreviewView.beginIndex = beginIndex;
    }

    /**
     * 动画帧数 {@link PreviewView#setRefreshRate(int)}
     */
    private long unitTime = 800 / 60;

    /**
     * 设置动画帧数
     *
     * @param refreshRate 刷新率
     */
    public void setRefreshRate(int refreshRate) {
        this.unitTime = 800 / refreshRate;
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
        if (c == null) {
            index = beginIndex;
            sWidth = getWidth();
            sHeight = getHeight();
            initBitmapInfo();
        }
    }

    public void setIndex(int i) {
        index = i;
        initBitmapInfo();
        if (onPictureChangeListener != null) {
            onPictureChangeListener.change(index);
        }
    }

    /**
     * 初始化图片信息
     */
    private void initBitmapInfo() {
        l = new BitmapInfo(null, LAST);
        c = new BitmapInfo(null, CENTER);
        n = new BitmapInfo(null, NEXT);
        loadPrePic();
    }

    /**
     * 根据上一张图片，中心图片和下一张图片所对应的矩阵更新图片的大小和位置
     */
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(c.b, matrixC, paint);
        canvas.drawBitmap(l.b, matrixL, paint);
        canvas.drawBitmap(n.b, matrixN, paint);
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
    public final boolean onTouch(View v, @NonNull MotionEvent event) {
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                down(event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                pointerDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                move(event);
                break;
            case MotionEvent.ACTION_UP:
                up(event);
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

    private void down(@NonNull MotionEvent event) {
        touching = true;
        x = event.getX();
        y = event.getY();
    }

    private void pointerDown(@NonNull MotionEvent event) {
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
    }

    private void move(@NonNull MotionEvent event) {
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
            float minY;
            float maxY;
            if (c.height * nm < getHeight()) {
                maxY = minY = (sHeight - c.height * nm) / 2;
            } else {
                minY = sHeight - c.height * nm;
                maxY = 0;
            }
            if (c.tmy < minY || c.tmy > maxY) {
                moveY = 0;
            }
            postTranslate(moveX, moveY);
            postInvalidate();
            x = nx;
            y = ny;
            speedX = moveX;
            speedY = moveY;
            totalMoveX = totalMoveX + moveX;
            totalMoveY = totalMoveY + moveY;
        } else if (event.getPointerCount() == 2) {
            // 实时计算焦点的绝对坐标，达到跟随效果
            adx = (event.getX(0) + event.getX(1)) / 2;
            ady = (event.getY(0) + event.getY(1)) / 2;
            float ndx = event.getX(0) - event.getX(1);
            float ndy = event.getY(0) - event.getY(1);
            float nd = (float) Math.sqrt(ndx * ndx + ndy * ndy);
            float newNm = (nd / d) * m;
            if (newNm < c.dm) {
                newNm = c.dm;
                ToastUtil.showToast(getContext(), "不能再小啦QAQ");
            } else if (newNm > 4 * c.width / sWidth) {
                newNm = Math.max(c.dm, 4 * c.width / sWidth);
                ToastUtil.showToast(getContext(), "不能再大啦QAQ");
            }
            setScale(newNm);
            postInvalidate();
        }
    }

    private float firstX;
    private float firstY;

    private float totalMoveX;
    private float totalMoveY;
    private float totalMove;

    private int count;
    private long firstClick;

    private void up(MotionEvent event) {
        touching = false;
        count++;
        if (count == 1) {
            totalMove = (float) Math.sqrt(totalMoveX * totalMoveX + totalMoveY * totalMoveY);
            firstX = event.getX();
            firstY = event.getY();
            firstClick = SystemClock.uptimeMillis();
        } else if (count == 2) {
            long secondClick = SystemClock.uptimeMillis();
            float secondX = event.getX();
            float secondY = event.getY();
            if (secondClick - firstClick < 300L
                    && totalMove < 1
                    && Math.abs(firstX - secondX) < sWidth / 10
                    && Math.abs(firstY - secondY) < sWidth / 10) {
                adx = event.getX();
                ady = event.getY();
                rdx = (adx - c.tmx) / nm;
                rdy = (ady - c.tmy) / nm;
                float newNm;
                if (nm == c.dm) {
                    newNm = 2 * c.dm;
                    if (newNm > 4 * c.width / sWidth) {
                        newNm = Math.max(c.dm, 4 * c.width / sWidth);
                    }
                } else {
                    newNm = c.dm;
                }
                scaleAuto(newNm);
                count = 0;
                firstClick = 0;
                return;
            } else {
                count = 1;
                totalMove = (float) Math.sqrt(totalMoveX * totalMoveX + totalMoveY * totalMoveY);
                firstX = event.getX();
                firstY = event.getY();
                firstClick = secondClick;
            }
        }
        totalMoveX = totalMoveY = 0;
        if (index - 1 >= 0
                && (l.tmx + sWidth > 0.3 * sWidth
                || l.tmx > -sWidth && speedX > 3)) {
            changeAnim(false);
        } else if (index + 1 < uriList.size()
                && (n.tmx < (0.7 * sWidth)
                || ((n.tmx < sWidth) && speedX < -3))) {
            changeAnim(true);
        } else {
            if (!adjustPosition()) {
                inertialMovement();
            }
        }
    }

    private static int numS;

    /**
     * 双击缩放动画效果
     */
    private void scaleAuto(float m) {
        numS = 0;
        Timer timerS = new Timer();
        float moveM = animSpeed * (m - nm) / unitTime;
        timerS.schedule(new TimerTask() {
            @Override
            public void run() {
                if (touching) {
                    setScale(m);
                    timerS.cancel();
                }
                if (numS < unitTime / animSpeed) {
                    nm = nm + moveM;
                    if (nm < c.dm) {
                        nm = c.dm;
                    }
                    setScale(nm);
                    postInvalidate();
                    numS++;
                } else {
                    setScale(m);
                    timerS.cancel();
                }
            }
        }, 0, (long) unitTime / animSpeed);
    }

    /**
     * 切换图片时的图片切换动画效果（自动归位）
     */
    private void changeAnim(boolean next) {
        this.setEnabled(false);
        num = 0;
        Timer timerAnim = new Timer();
        float moveX = next ?
                animSpeed * (0 - n.tmx) / unitTime
                : animSpeed * (0 - l.tmx) / unitTime;
        timerAnim.schedule(new TimerTask() {
            @Override
            public void run() {
                if (touching) {
                    change(next);
                    timerAnim.cancel();
                }
                if (num < unitTime / animSpeed) {
                    postTranslate(moveX, 0);
                    postInvalidate();
                    num++;
                } else {
                    change(next);
                    timerAnim.cancel();
                }
            }
        }, 0, (long) unitTime / animSpeed);
    }

    /**
     * 切换图片
     */
    private void change(boolean next) {
        if (next) {
            n.reset(NEXT);
            index++;
        } else {
            l.reset(LAST);
            index--;
        }
        loadPrePic();
        if (onPictureChangeListener != null) {
            onPictureChangeListener.change(index);
        }
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
        void change(int index);
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
     * 松手后的惯性运动
     */
    private void inertialMovement() {
        hypotenuse = (float) Math.sqrt(speedX * speedX + speedY * speedY);
        if (hypotenuse == 0) {
            return;
        }
        hypotenuse = Math.min(50, hypotenuse);
        sin = speedY / hypotenuse;
        cos = speedX / hypotenuse;
        speedX = 0;
        speedY = 0;
        Timer timerI = new Timer();
        timerI.schedule(new TimerTask() {
            @Override
            public void run() {
                if (touching) {
                    timerI.cancel();
                }
                if (hypotenuse > 0) {
                    float endX = c.tmx + cos * hypotenuse;
                    float endY = c.tmy + sin * hypotenuse;
                    // 如果X越界，则重新设置X位置
                    if (crossBoundaryX()) {
                        // 如果是左越界，即左边存在黑条
                        if (endX >= maxX) {
                            endX = maxX;
                        } else {
                            // 如果是右越界，即右边存在黑条
                            endX = minX;
                        }
                    }
                    // 如果Y越界，则重新设置Y位置
                    if (crossBoundaryY()) {
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

                    hypotenuse =
                            hypotenuse - (float) (animSpeed * 5 / (1.25 * unitTime));
                } else {
                    hypotenuse = 0;
                    timerI.cancel();
                }
            }
        }, 0, (long) (1.25 * unitTime / animSpeed));
    }

    /**
     * 当前图片自动归位的最终X坐标
     */
    private static float endX;
    /**
     * 当前图片自动归位的最终Y坐标
     */
    private static float endY;
    private static int num;

    /**
     * 调整因越界或缩放大小不符合要求的图片位置和图片大小（当前图片自动归位）
     */
    private boolean adjustPosition() {
        float startX = endX = c.tmx;
        float startY = endY = c.tmy;
        boolean bx = crossBoundaryX();
        boolean by = crossBoundaryY();
        if (!bx && !by) {
            return false;
        }
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
        Timer timerA = new Timer();
        timerA.schedule(new TimerTask() {
            @Override
            public void run() {
                if (touching) {
                    timerA.cancel();
                }
                if (num < unitTime / animSpeed) {
                    postTranslate(moveX, moveY);
                    postInvalidate();
                    num++;
                } else {
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
        minX = sWidth - c.width * nm;
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
        if (c.height * nm < getHeight()) {
            maxY = minY = (sHeight - c.height * nm) / 2;
        } else {
            minY = sHeight - c.height * nm;
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
     * 根据设置的预加载图片数量 {@link PreviewView#preSize} 确定要加载的图片的索引
     * 并发送消息调用 {@link PreviewView#loadPic(int)} 进行加载
     */
    private void loadPrePic() {
        for (int i = 0; i <= preSize + 1; i++) {
            int j = ((index - preSize / 2) + i);
            if (j >= 0 && j <= uriList.size() - 1) {
                Bitmap bitmap = null;
                if (bitmapList.get(j) != null) {
                    bitmap = bitmapList.get(j).get();
                }
                setBitmap(bitmap, j);
                if (bitmap == null) {
                    mainHandler.handleMessage(mainHandler.obtainMessage(LOAD_PIC, j));
                }
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
            setBitmap(bitmap, i);
        }

        @Override
        public void fail(BitmapHunter hunter) {
            // 加载是失败时不用做额外操作，因为位图为空会默认设置为设置好的错误图片
        }
    }

    private void setBitmap(Bitmap bitmap, int i) {
        if (i == index - 1) {
            l.setBitmap(bitmap, LAST);
            matrixL.setScale(l.dm, l.dm);
            setTranslate(l.tmx, l.tmy, matrixL);
        } else if (i == index && c.b != bitmap) {
            c.setBitmap(bitmap, CENTER);
            matrixC.setScale(c.dm, c.dm);
            setTranslate(c.tmx, c.tmy, matrixC);
            m = nm = c.dm;
        } else if (i == index + 1) {
            n.setBitmap(bitmap, NEXT);
            matrixN.setScale(n.dm, n.dm);
            setTranslate(n.tmx, n.tmy, matrixN);
        }
        postInvalidate();
    }

    /**
     * 存储在屏幕上显示的图片（包括上一张，中心和下一张）的信息
     */
    private class BitmapInfo {

        private BitmapInfo(Bitmap b, int position) {
            if (b != null) {
                this.b = b;
            } else {
                this.b = errorBitmap;
            }
            width = this.b.getWidth();
            height = this.b.getHeight();
            dm = sWidth / width;
            tmy = (sHeight - height * dm) / 2;
            switch (position) {
                case LAST:
                    tmx = -sWidth - interval;
                    break;
                case CENTER:
                    tmx = 0;
                    nm = dm;
                    break;
                case NEXT:
                    if (c != null) {
                        tmx = nm * c.width + interval;
                    } else {
                        tmx = sWidth + interval;
                    }
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
         * 重新设置图片
         *
         * @param b 位图
         */
        private void setBitmap(Bitmap b, int position) {
            if (b != null) {
                this.b = b;
            } else {
                this.b = errorBitmap;
            }
            width = this.b.getWidth();
            height = this.b.getHeight();
            dm = sWidth / width;
            switch (position) {
                case LAST:
                    tmx = -sWidth - interval;
                    break;
                case CENTER:
                    tmx = 0;
                    nm = dm;
                    break;
                case NEXT:
                    if (c != null) {
                        tmx = nm * c.width + interval;
                    } else {
                        tmx = sWidth + interval;
                    }
                    break;
                default:
                    break;
            }
            tmy = (sHeight - height * dm) / 2;
        }

        private void reset(int position) {
            this.b = errorBitmap;
            width = this.b.getWidth();
            height = this.b.getHeight();
            dm = sWidth / width;
            tmy = (sHeight - height * dm) / 2;
            switch (position) {
                case LAST:
                    tmx = -sWidth - interval;
                    break;
                case CENTER:
                    tmx = 0;
                    nm = dm;
                    break;
                case NEXT:
                    if (c != null) {
                        tmx = nm * c.width + interval;
                    } else {
                        tmx = sWidth + interval;
                    }
                    break;
                default:
                    break;
            }
        }

    }

}
