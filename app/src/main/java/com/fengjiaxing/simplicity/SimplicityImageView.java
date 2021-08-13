package com.fengjiaxing.simplicity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimplicityImageView extends androidx.appcompat.widget.AppCompatImageView
        implements View.OnClickListener {

    private static final List<SimplicityImageView> list = Collections.synchronizedList(new ArrayList<>());

    private static int max = 10;

    private Paint paint;

    private boolean selected;

    public SimplicityImageView(Context context) {
        super(context);
        init();
    }

    public SimplicityImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SimplicityImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //noinspection SuspiciousNameCombination
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (selected) {
            float s = getWidth();
            float r = s / 2;
            canvas.drawColor(0x88CFB9FF);

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
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(30f);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        this.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        this.selected = !selected;
        if (this.selected) {
            if (list.size() < max) {
                list.add(this);
                for (int i = 0; i < list.size(); i++) {
                    list.get(i).invalidate();
                }
            } else {
                this.selected = false;
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
        SimplicityImageView.max = max;
    }

    public static List<Drawable> getDrawableList() {
        List<Drawable> drawableList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Drawable d = list.get(i).getDrawable();
            drawableList.add(d);
        }
        return drawableList;
    }

    public static int getSelectedCount() {
        return list.size();
    }

    public static void clearList() {
        for (int i = 0; i < list.size(); i++) {
            list.get(i).selected = false;
            list.get(i).invalidate();
        }
        list.clear();
    }

}
