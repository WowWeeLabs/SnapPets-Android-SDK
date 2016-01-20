package com.wowwee.snappetssampleproject.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ShareUtil {
    public static void shareText(Context context, String msg) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        context.startActivity(intent);
    }

    public static void shareTextWithImage(Context context, String msg, File image) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        if (msg != null) {
            intent.putExtra(Intent.EXTRA_TEXT, msg);
        }
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(image));
        context.startActivity(intent);
    }

    public static void shareTextWithMultipleImages(Context context, String msg, List<File> files) {
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("*/*");

        if (msg != null) {
            intent.putExtra(Intent.EXTRA_TEXT, msg);
        }

        ArrayList<Uri> uriList = new ArrayList<Uri>();
        for (File file : files) {
            uriList.add(Uri.fromFile(file));
        }
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);

        context.startActivity(intent);
    }
}
