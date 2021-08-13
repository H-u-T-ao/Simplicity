package com.fengjiaxing.picload;

import android.graphics.Bitmap;

import java.io.IOException;

public interface RequestHandler {

    Bitmap load(Simplicity simplicity, RequestData data) throws IOException;

    String loadSource();

}
