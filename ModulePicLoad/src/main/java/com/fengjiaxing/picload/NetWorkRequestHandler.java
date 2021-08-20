package com.fengjiaxing.picload;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 根据Uri在网络中中获取图片的默认策略
 */
public class NetWorkRequestHandler implements RequestHandler {

    private static final String SOURCE = "NETWORK";

    private static final String SCHEME_HTTP = "http";
    private static final String SCHEME_HTTPS = "https";
    private static final int TIME_OUT = 15000;

    @Override
    public Bitmap load(Simplicity simplicity, RequestData data) {
        String scheme = data.uri.getScheme();
        Bitmap bitmap = null;
        if (SCHEME_HTTP.equals(scheme) || SCHEME_HTTPS.equals(scheme)) {
            HttpURLConnection connection = null;
            InputStream in = null;
            try {
                Uri uri = data.uri;
                URL url = new URL(uri.toString());
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(TIME_OUT);
                connection.setConnectTimeout(TIME_OUT);
                connection.setDoOutput(true);
                connection.setDoInput(true);
                in = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        return bitmap;
    }

    @Override
    public String loadSource() {
        return SOURCE;
    }

}
