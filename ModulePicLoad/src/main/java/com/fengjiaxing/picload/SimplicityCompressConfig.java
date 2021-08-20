package com.fengjiaxing.picload;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * 提供的一种图片压缩配置
 */
public class SimplicityCompressConfig implements RequestBuilder.CompressConfig {

    /**
     * 允许的图片最大大小
     */
    private final int maxByteCount;

    public SimplicityCompressConfig(int maxByteCount) {
        this.maxByteCount = maxByteCount;
    }

    /**
     * 每一次将图片的长和宽都压缩至原来的一半，直到大小符合要求为止
     */
    @Override
    public Bitmap Compress(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.setScale(0.5f, 0.5f);
        int byteCount = bitmap.getByteCount();
        while (byteCount > maxByteCount) {
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, true);
            byteCount = bitmap.getByteCount();
        }
        return bitmap;
    }

}
