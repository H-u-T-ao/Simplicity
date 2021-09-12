package com.fengjiaxing.picload;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * 提供的一种图片压缩配置
 */
public class SimplicityCompressConfig implements RequestBuilder.CompressConfig {

    /**
     * 允许的图片最大宽
     */
    private final float maxWidth;

    /**
     * 允许的图片最大高
     */
    private final float maxHeight;

    public SimplicityCompressConfig(float maxWidth, float maxHeight) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    @Override
    public Bitmap Compress(Bitmap bitmap) {
        int byteCount = bitmap.getByteCount();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float maxS = Math.max(maxWidth / width, maxHeight / height);
        float s = Math.min(1f, maxS);
        Matrix matrix = new Matrix();
        matrix.setScale(s, s);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                width, height, matrix, true);
        return bitmap;
    }

}
