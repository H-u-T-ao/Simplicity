package com.fengjiaxing.hutao;

import android.graphics.Bitmap;

import java.io.IOException;

public interface RequestHandler {

    Bitmap load(HuTao huTao, RequestData data) throws IOException;

    String loadSource();

}
