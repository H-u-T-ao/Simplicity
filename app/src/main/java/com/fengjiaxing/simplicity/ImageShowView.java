package com.fengjiaxing.simplicity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ImageShowView extends androidx.appcompat.widget.AppCompatImageView
        implements View.OnTouchListener {

    private BitmapInfo l;
    private BitmapInfo c;
    private BitmapInfo n;

    private int index;

    public ImageShowView(Context context) {
        super(context);
        init();
    }

    public ImageShowView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageShowView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private int animSpeed = 1;

    public void setAnimSpeed(int x) {
        animSpeed = x;
    }

    private Paint paint;
    private Matrix matrixL;
    private Matrix matrixC;
    private Matrix matrixN;

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        paint = new Paint();
        matrixL = new Matrix();
        matrixC = new Matrix();
        matrixN = new Matrix();
        this.setOnTouchListener(this);
    }

    private void setTranslate(float tmx, float tmy, Matrix matrix) {
        float[] values = new float[9];
        matrix.getValues(values);
        values[2] = tmx;
        values[5] = tmy;
        matrix.setValues(values);
    }

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

    private void homingAll(float tmx, float tmy) {
        setTranslate(tmx, tmy, matrixC);
        c.tmx = tmx;
        c.tmy = tmy;
        if (l != null) {
            setTranslate(tmx - sWidth, l.tmy, matrixL);
            l.tmx = tmx - sWidth;
        }
        if (n != null) {
            setTranslate(tmx + nm * c.width, n.tmy, matrixN);
            n.tmx = tmx + nm * c.width;
        }
    }

    private void setScale(float m) {
        matrixC.setScale(m, m);
        // 计算应该移动的距离
        if (nm != c.dm) {
            c.tmx = adx - m * rdx;
            c.tmy = ady - m * rdy;
        }
        setTranslate(c.tmx, c.tmy, matrixC);
        if (l != null) {
            l.tmx = c.tmx - sWidth - 8;
            setTranslate(l.tmx, l.tmy, matrixL);
        }
        if (n != null) {
            n.tmx = c.tmx + m * c.width + 8;
            setTranslate(n.tmx, n.tmy, matrixN);
        }
    }

    private List<Bitmap> bitmapList;

    public void setBitmapList(List<Bitmap> bitmapList) {
        this.bitmapList = bitmapList;
    }

    private float unitTime = 1000 / 60F;

    public void setRefreshRate(float refreshRate) {
        this.unitTime = (long) (1000 / refreshRate);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private float sWidth;
    private float sHeight;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // super.onLayout(changed, left, top, right, bottom);
        initBitmapInfo();
    }

    private void initBitmapInfo() {
        if (c == null) {
            sWidth = getWidth();
            sHeight = getHeight();
            index = 0;
            if (bitmapList.size() >= 2) {
                Bitmap center = bitmapList.get(0);
                Bitmap next = bitmapList.get(1);
                c = new BitmapInfo(center, sWidth, sHeight, BitmapInfo.CENTER);
                n = new BitmapInfo(next, sWidth, sHeight, BitmapInfo.NEXT);
                matrixC.setScale(c.dm, c.dm);
                setTranslate(c.tmx, c.tmy, matrixC);
                matrixN.setScale(n.dm, n.dm);
                setTranslate(n.tmx, n.tmy, matrixN);
                m = nm = c.dm;
            } else if (bitmapList.size() == 1) {
                Bitmap center = bitmapList.get(0);
                c = new BitmapInfo(center, sWidth, sHeight, BitmapInfo.CENTER);
                matrixC.setScale(c.dm, c.dm);
                setTranslate(c.tmx, c.tmy, matrixC);
                m = nm = c.dm;
            } else {
                throw new IllegalArgumentException("传入位图集合为空");
            }
        }
    }

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

    // 单个手指的坐标位置，用于控制平移
    private float x = 0;
    private float y = 0;

    // 标准焦点的绝对坐标
    private float adx;
    private float ady;

    // 标准焦点的相对坐标
    private float rdx;
    private float rdy;

    // 两个手指之间的标准距离
    private float d;
    // 中心图片的标准缩放倍率
    private float m;

    // 第二只手指抬起并且事件未处理时，该值为true
    private boolean su = false;

    // 中心图片的实时缩放倍率
    private float nm;

    private boolean touching;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
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
                        && ((l.tmx + sWidth) > (0.4 * sWidth)
                        || ((l.tmx > -sWidth) && speedX > 10))) {
                    changeAnim(false);
                } else if (n != null
                        && (n.tmx < (0.6 * sWidth)
                        || ((n.tmx < sWidth) && speedX < -10))) {
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

    private void change(boolean next) {
        if (next) {
            if (bitmapList.size() == index + 2) {
                index++;
                l = c.changePosition(sWidth, sHeight, BitmapInfo.LAST);
                c = n.changePosition(sWidth, sHeight, BitmapInfo.CENTER);
                n = null;
                nm = c.dm;
            } else if (bitmapList.size() > index + 2) {
                index++;
                Bitmap b = bitmapList.get(index + 1);
                l = c.changePosition(sWidth, sHeight, BitmapInfo.LAST);
                c = n.changePosition(sWidth, sHeight, BitmapInfo.CENTER);
                n = new BitmapInfo(b, sWidth, sHeight, BitmapInfo.NEXT);
                nm = c.dm;
            } else {
                return;
            }
        } else {
            if (index == 1) {
                index--;
                n = c.changePosition(sWidth, sHeight, BitmapInfo.NEXT);
                c = l.changePosition(sWidth, sHeight, BitmapInfo.CENTER);
                l = null;
                nm = c.dm;
            } else if (index > 1) {
                index--;
                Bitmap b = bitmapList.get(index - 1);
                n = c.changePosition(sWidth, sHeight, BitmapInfo.NEXT);
                c = l.changePosition(sWidth, sHeight, BitmapInfo.CENTER);
                l = new BitmapInfo(b, sWidth, sHeight, BitmapInfo.LAST);
                nm = c.dm;
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
        this.setEnabled(true);
    }

    private static float hypotenuse;
    private static float speedX;
    private static float speedY;
    private static float sin;
    private static float cos;

    private Timer timerI;

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

    private static float endM;
    private static float endX;
    private static float endY;
    private static int num;

    private Timer timerA;

    // 调整因越界或缩放大小不符合要求的图片位置
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

    private static float minX;
    private static float maxX;

    // 检查X方向上是否越界
    private boolean crossBoundaryX() {
        // 计算x方向上的最值
        minX = sWidth - c.width * endM;
        maxX = 0;
        // 判断图片在x方向上是否过界
        return c.tmx <= minX || c.tmx >= maxX;
    }

    private static float minY;
    private static float maxY;

    // 检查Y方向上是否越界
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

    private static class BitmapInfo {

        private static final int LAST = -1;
        private static final int CENTER = 0;
        private static final int NEXT = 1;

        private BitmapInfo(Bitmap b, float sWidth, float sHeight, int position) {
            this.b = b;
            width = b.getWidth();
            height = b.getHeight();
            dm = sWidth / width;
            tmy = (sHeight - height * dm) / 2;
            switch (position) {
                case LAST:
                    tmx = -sWidth - 8;
                    break;
                case CENTER:
                    tmx = 0;
                    break;
                case NEXT:
                    tmx = sWidth + 8;
                    break;
                default:
                    break;
            }
        }

        private final Bitmap b;
        // 图片的大小
        private final float width;
        private final float height;
        // 图片的默认缩放倍率
        private final float dm;
        // 图片左上角的坐标位置
        private float tmx;
        private float tmy;

        private BitmapInfo changePosition(float sWidth, float sHeight, int position) {
            switch (position) {
                case LAST:
                    tmx = -sWidth - 8;
                    tmy = (sHeight - height * dm) / 2;
                    break;
                case CENTER:
                    tmx = 0;
                    tmy = (sHeight - height * dm) / 2;
                    break;
                case NEXT:
                    tmx = sWidth + 8;
                    tmy = (sHeight - height * dm) / 2;
                    break;
                default:
                    break;
            }
            return this;
        }

    }

}
