package com.wowwee.snappetssampleproject.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.TextView;

public class DialogUtil {
    public static void showYesNoDialog(Context context, String title, String yesButton, String noButton, final Callback yesCallback, final Callback noCallback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setPositiveButton(yesButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

                if (yesCallback != null) {
                    yesCallback.callback(dialog);
                }
            }
        });
        builder.setNegativeButton(noButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

                if (noCallback != null) {
                    noCallback.callback(dialog);
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

        TextView textView = (TextView) dialog.findViewById(android.R.id.title);
        if (textView != null)
            textView.setTextSize(20);
    }

    public interface Callback {
        public void callback(DialogInterface dialog);
    }
}
