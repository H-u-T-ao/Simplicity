package com.fengjiaxing.picload;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.SystemClock;

public class SimplicityDrawable extends BitmapDrawable {

    private static final float FADE_TIME = 200F;

    private boolean fading;

    private final long startTime;

    SimplicityDrawable(Context context, Bitmap bitmap) {
        super(context.getResources(), bitmap);
        startTime = SystemClock.uptimeMillis();
        fading = true;
    }

    @Override
    public void draw(Canvas canvas) {
        if (fading) {
            float i = SystemClock.uptimeMillis() - startTime;
            float completionDegree = i / FADE_TIME;
            if (completionDegree >= 1F) {
                fading = false;
                super.draw(canvas);
            } else {
                int alpha = 0xFF;
                int newAlpha = (int) (alpha * completionDegree);
                super.setAlpha(newAlpha);
                super.draw(canvas);
                super.setAlpha(alpha);
            }
        } else {
            super.draw(canvas);
        }
    }

}
