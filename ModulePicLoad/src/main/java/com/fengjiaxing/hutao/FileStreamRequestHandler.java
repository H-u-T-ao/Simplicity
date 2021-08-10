package com.fengjiaxing.hutao;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;

public class FileStreamRequestHandler implements RequestHandler {

    private static final String SOURCE = "FILE_STREAM | RESOURCE";

    private static final String SCHEME_ANDROID_RESOURCE = "android.resource";
    private static final String SCHEME_FILE = "file";

    @Override
    public Bitmap load(HuTao huTao, RequestData data) throws IOException {
        Uri uri = data.uri;
        String scheme = uri.getScheme();
        Bitmap bitmap = null;
        if (SCHEME_FILE.equals(scheme) || SCHEME_ANDROID_RESOURCE.equals(scheme)) {
            InputStream inputStream = huTao.context.getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(inputStream);
        }
        return bitmap;
    }

    @Override
    public String loadSource() {
        return SOURCE;
    }
}
