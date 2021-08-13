package com.fengjiaxing.picload;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class SimplicityCompressConfig implements RequestBuilder.CompressConfig {

    private final int maxByteCount;

    public SimplicityCompressConfig(int maxByteCount) {
        this.maxByteCount = maxByteCount;
    }

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
