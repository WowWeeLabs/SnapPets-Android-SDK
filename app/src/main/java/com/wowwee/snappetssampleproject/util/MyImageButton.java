package com.wowwee.snappetssampleproject.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.MotionEvent;
import android.widget.ImageButton;

/**
 * Created by Artem on 25.09.2015.
 */
public class MyImageButton extends ImageButton {

    public MyImageButton(Context context) {
        super(context);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        }

        return result;
    }
}
