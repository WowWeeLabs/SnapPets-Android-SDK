package com.wowwee.snappetssampleproject.ui;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class SplashBgImageView extends ImageView {

    private final RectF drawableRect = new RectF(0, 0, 0, 0);
    private final RectF viewRect = new RectF(0, 0, 0, 0);
    private final Matrix m = new Matrix();
    private boolean done = false;

    public SplashBgImageView(Context context) {
        super(context);

        setScaleType(ScaleType.MATRIX);
    }

    public SplashBgImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setScaleType(ScaleType.MATRIX);
    }

    public SplashBgImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setScaleType(ScaleType.MATRIX);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (done) {
            return;// Already fixed drawable scale
        }
        final Drawable d = getDrawable();
        if (d == null) {
            return;// No drawable to correct for
        }
        int viewHeight = getMeasuredHeight();
        int viewWidth = getMeasuredWidth();
        int drawableWidth = d.getIntrinsicWidth();
        int drawableHeight = d.getIntrinsicHeight();
        drawableRect.set(0, 0, drawableWidth, drawableHeight);// Represents the original image

        //fit width, crop height & align to bottom
        float scale = (float) viewWidth / (float) drawableWidth;
        float scaledHeight = drawableHeight * scale;
        viewRect.set(0, viewHeight - scaledHeight, viewWidth, viewHeight);

        m.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);
        setImageMatrix(m);

        done = true;

        requestLayout();
    }
}
