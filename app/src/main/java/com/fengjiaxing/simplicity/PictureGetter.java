package com.fengjiaxing.simplicity;

import android.content.ContentUris;
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
            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID));
            Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            list.add(uri);
        }
        cursor.close();
        return list;
    }

}
