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

public class ImageShowView extends androidx.appcompat.widget.AppCompatImageView
        implements View.OnTouchListener {

    private Paint paint;

    private Bitmap b;

    private Matrix matrix;

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

    private void setTranslate(float x, float y) {
        float[] values = new float[9];
        matrix.getValues(values);
        values[2] = x;
        values[5] = y;
        matrix.setValues(values);
    }

    private void setScale(float m) {
        float[] values = new float[9];
        matrix.getValues(values);
        values[0] = m;
        values[4] = m;
        matrix.setValues(values);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(b, matrix, paint);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // 测量一些标准变量
        bWidth = b.getWidth();
        float sWidth = getWidth();
        dm = sWidth / bWidth;
        m = dm;
        nm = dm;
        bHeight = b.getHeight();
        float sHeight = getHeight();
        setScale(dm);
        if (bHeight < sHeight) {
            dy = (sHeight - bHeight * dm) / 2;
            tmy = dy;
            setTranslate(0, dy);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        paint = new Paint();
        matrix = new Matrix();
        this.setOnTouchListener(this);
    }

    public void setBitmap(Bitmap b) {
        this.b = b;
        this.invalidate();
    }

    // 图片的默认大小
    private float bWidth;
    private float bHeight;

    // 图片的默认缩放倍率
    private float dm;
    // 图片的默认位置的坐标
    private float dy;

    // 单个手指的坐标位置，用于控制平移
    private float x = 0;
    private float y = 0;

    // 图片左上角的坐标位置
    private float tmx = 0;
    private float tmy = 0;

    // 标准焦点的相对坐标
    private float rdx;
    private float rdy;

    // 两个手指之间的标准距离
    private float d;
    // 图片的标准缩放倍率
    private float m;

    // 第二只手指抬起并且事件未处理时，该值为true
    private boolean su = false;

    // 图片的实时缩放倍率
    private float nm;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getActionMasked();
        // 记录标准焦点的绝对坐标
        float adx;
        float ady;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                x = event.getX();
                y = event.getY();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) {
                    float x = event.getX(0) - event.getX(1);
                    float y = event.getY(0) - event.getY(1);
                    d = (float) Math.sqrt(x * x + y * y);
                    adx = (event.getX(0) + event.getX(1)) / 2;
                    ady = (event.getY(0) + event.getY(1)) / 2;
                    // 计算标准焦点的相对坐标
                    rdx = (adx - tmx) / m;
                    rdy = (ady - tmy) / m;
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
                    matrix.postTranslate(moveX, moveY);
                    this.invalidate();
                    tmx = tmx + moveX;
                    tmy = tmy + moveY;
                    x = nx;
                    y = ny;
                } else if (event.getPointerCount() == 2) {
                    // 实时计算焦点的绝对坐标，达到跟随效果
                    adx = (event.getX(0) + event.getX(1)) / 2;
                    ady = (event.getY(0) + event.getY(1)) / 2;

                    float ndx = event.getX(0) - event.getX(1);
                    float ndy = event.getY(0) - event.getY(1);
                    float nd = (float) Math.sqrt(ndx * ndx + ndy * ndy);
                    nm = (nd / d) * m;
                    matrix.setScale(nm, nm, rdx, rdy);

                    // 计算应该移动的距离
                    tmx = adx - nm * rdx;
                    tmy = ady - nm * rdy;

                    setTranslate(tmx, tmy);
                    this.invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                // 调整位置
                adjustPosition();
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

    // 调整因越界或缩放大小不符合要求的图片位置
    private void adjustPosition() {
        // 如果松手的时候大小小于默认大小
        if (nm < dm) {
            matrix.setScale(dm, dm);
            tmx = 0;
            tmy = dy;
            nm = dm;
            m = nm;
        }
        m = nm;
        // 如果X越界，则重新设置X位置
        if (crossBoundaryX()) {
            // 如果是左越界，即左边存在黑条
            if (tmx > maxX) {
                tmx = maxX;
            } else {
                // 如果是右越界，即右边存在黑条
                tmx = minX;
            }
        }
        // 如果Y越界，则重新设置Y位置
        if (crossBoundaryY()) {
            // 如果是上越界，即上边存在黑条
            if (tmy > maxY) {
                tmy = maxY;
            } else {
                // 如果是下越界，即右边存在黑条
                tmy = minY;
            }
        }
        setTranslate(tmx, tmy);
        this.invalidate();
    }

    private float minX;
    private float maxX;

    // 检查X方向上是否越界
    private boolean crossBoundaryX() {
        // 计算x方向上的最值
        minX = getWidth() - bWidth * nm;
        maxX = 0;
        // 判断图片在x方向上是否过界
        return tmx < minX || tmx > maxX;
    }

    private float minY;
    private float maxY;

    // 检查Y方向上是否越界
    private boolean crossBoundaryY() {
        // 计算y方向上的最值
        if (bHeight * nm < getHeight()) {
            minY = (getHeight() - bHeight * nm) / 2;
            maxY = minY;
        } else {
            minY = getHeight() - bHeight * nm;
            maxY = 0;
        }
        // 判断图片在y方向上是否过界
        return tmy < minY || tmy > maxY;
    }

}
