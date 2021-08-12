package com.fengjiaxing.simplicity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HuTaoImageView extends androidx.appcompat.widget.AppCompatImageView implements View.OnClickListener {

    private static final List<HuTaoImageView> list = Collections.synchronizedList(new ArrayList<>());

    private static int max = 10;

    private Paint paint;

    private boolean checked;

    public HuTaoImageView(Context context) {
        super(context);
        init();
    }

    public HuTaoImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HuTaoImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (checked) {
            float s = getWidth();
            float r = s / 2;
            canvas.drawColor(0x88888888);

            paint.setTextSize(5 * r / 4);
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) == this) {
                    canvas.drawText(Integer.toString(i + 1), s / 2, (s / 2 + r / 2), paint);
                }
            }

        }
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(30f);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        this.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        this.checked = !checked;
        if (this.checked) {
            if (list.size() < max) {
                list.add(this);
                for (int i = 0; i < list.size(); i++) {
                    list.get(i).invalidate();
                }
            } else {
                this.checked = false;
            }
        } else {
            for (int i = 0; i < list.size(); i++) {
                list.get(i).invalidate();
            }
            list.remove(this);
        }
    }

    public static int getMax() {
        return max;
    }

    public static void setMax(int max) {
        if (max <= 0) {
            throw new IllegalArgumentException("设置的最大选择图片数量不应为非正数");
        }
        HuTaoImageView.max = max;
    }

}
