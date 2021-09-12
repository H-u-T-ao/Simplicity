package com.fengjiaxing.picload;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 根据Uri在文件或资源文件中获取图片的默认策略
 */
public class FileStreamRequestHandler implements RequestHandler {

    private static final String SOURCE = "FILE_STREAM | RESOURCE";

//    private static final String SCHEME_ANDROID_RESOURCE = "android.resource";
//    private static final String SCHEME_FILE = "file";

    @Override
    public Bitmap load(Simplicity simplicity, RequestData data) throws IOException {
        Uri uri = data.uri;
        Bitmap bitmap;
        InputStream inputStream = simplicity.context.getContentResolver().openInputStream(uri);
        bitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();
        return bitmap;
    }

    @Override
    public String loadSource() {
        return SOURCE;
    }
}
