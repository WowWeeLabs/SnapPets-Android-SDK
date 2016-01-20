package com.wowwee.snappetssampleproject.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.File;

public class DrawableUtil {
    public static Drawable getDrawableFromFile(Context context, File file) {
        if (file != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
            return new BitmapDrawable(context.getResources(), bitmap);
        } else {
            return null;
        }
    }
}
