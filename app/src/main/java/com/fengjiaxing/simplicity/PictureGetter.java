package com.fengjiaxing.simplicity;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;

public class PictureGetter {

    static ArrayList<Uri> get(Context context) {
        ArrayList<Uri> list = new ArrayList<>();
        Cursor cursor = context.getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        null, null, null, null);
        while (cursor.moveToNext()) {
            byte[] data = cursor.getBlob(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            File file = new File(new String(data, 0, data.length - 1));
            list.add(Uri.fromFile(file));
        }
        cursor.close();
        return list;
    }

}
