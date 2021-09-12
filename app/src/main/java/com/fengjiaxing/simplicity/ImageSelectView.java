package com.fengjiaxing.simplicity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 可以进行图片选择的ImageSelectView
 */
public class ImageSelectView extends androidx.appcompat.widget.AppCompatImageView
        implements View.OnClickListener {

    /**
     * 已被选择的图片Uri列表
     */
    private static final List<Uri> list = Collections.synchronizedList(new ArrayList<>());

    /**
     * 已被选择的ImageSelectView列表
     */
    private static final Set<ImageSelectView> viewSet =
            Collections.synchronizedSet(new HashSet<>());

    /**
     * true代表正在选择模式，false反之
     * <p>
     * 通过 {@link ImageSelectView#setSelectable(boolean)} 进行设置
     */
    private static boolean selectable;

    /**
     * 未进入选择模式时的单击操作接口{@link OnNormalClickListener}
     */
    private OnNormalClickListener onNormalClickListener;

    private OnSelectedListener onSelectedListener;

    /**
     * 允许选择的最大数量
     * <p>
     * 通过 {@link ImageSelectView#setMax(int)} 进行设置
     */
    private static int max = 10;

    private Paint paint;

    /**
     * 当前ImageSelectView是否已经被选择
     */
    private boolean selected;

    /**
     * ImageSelectView的Uri标记
     */
    private Uri uri;

    public ImageSelectView(Context context) {
        super(context);
        init();
    }

    public ImageSelectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageSelectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        int i = list.indexOf(uri);
        if (i == -1) {
            viewSet.remove(this);
            selected = false;
        } else {
            viewSet.add(this);
            selected = true;
        }
        super.setImageDrawable(drawable);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //noinspection SuspiciousNameCombination
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int i = list.lastIndexOf(this.uri);
        if (i != -1) {
            this.selected = true;
            float s = getWidth();
            float r = s / 2;
            canvas.drawColor(R.color.app_theme_color);
            paint.setTextSize(5 * r / 4);
            canvas.drawText(Integer.toString(i + 1), s / 2, (s / 2 + r / 2), paint);
        }
    }

    // 初始化
    private void init() {
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(30f);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        this.setOnClickListener(this);
    }

    public boolean isSelected() {
        return selected;
    }

    @Override
    public void onClick(View v) {
        // 选择模式下单击选择
        if (selectable) {
            this.selected = !selected;
            if (this.selected) {
                selected();
            } else {
                unSelected();
            }
        } else {
            // 非选择模式调用的方法
            if (onNormalClickListener != null) {
                onNormalClickListener.normalClick();
            }
        }
    }

    /**
     * 选择
     */
    public final boolean selected() {
        if (list.size() < max) {
            selected = true;
            list.add(this.uri);
            invalidate();
            viewSet.add(this);
            if (onSelectedListener != null) {
                onSelectedListener.onSelected(this);
            }
            return true;
        } else {
            selected = false;
            return false;
        }
    }

    /**
     * 取消选择
     */
    public final void unSelected() {
        boolean b = list.remove(this.uri);
        if (b) {
            for (ImageSelectView view : viewSet) {
                view.invalidate();
            }
            viewSet.remove(this);
        }
        if (onSelectedListener != null) {
            onSelectedListener.onUnSelected(this);
        }
    }

    /**
     * 选择和取消选择时的回调接口
     */
    public interface OnSelectedListener {
        void onSelected(ImageSelectView view);

        void onUnSelected(ImageSelectView view);
    }

    public void setOnSelectedListener(OnSelectedListener listener) {
        this.onSelectedListener = listener;
    }

    /**
     * 未进入选择模式时的单击操作接口
     */
    public interface OnNormalClickListener {
        void normalClick();
    }

    /**
     * 设置未进入选择模式时的单击操作接口
     */
    public void setOnNormalClickListener(OnNormalClickListener listener) {
        this.onNormalClickListener = listener;
    }

    /**
     * 设置选择图片的最大数量
     */
    public static void setMax(int max) {
        if (max <= 0) {
            throw new IllegalArgumentException("设置的最大选择图片数量不应为非正数");
        }
        ImageSelectView.max = max;
    }

    /**
     * 获取已被选择的Uri的列表
     */
    public static List<Uri> getList() {
        return list;
    }

    /**
     * 获取已被选择的Uri的总数
     */
    public static int getSelectedCount() {
        return list.size();
    }

    /**
     * 清除选择列表
     */
    public static void clearList() {
        for (ImageSelectView view : viewSet) {
            view.selected = false;
            view.invalidate();
        }
        viewSet.clear();
        list.clear();
    }

    /**
     * 获取当前是否是选择模式
     */
    public static boolean getSelectable() {
        return selectable;
    }

    /**
     * 开启/关闭选择模式
     *
     * @param selectable 开启/关闭
     */
    public static void setSelectable(boolean selectable) {
        ImageSelectView.selectable = selectable;
    }

    /**
     * 给当前ImageSelectView设置Uri标识
     */
    public void setUri(Uri uri) {
        this.uri = uri;
    }

}
