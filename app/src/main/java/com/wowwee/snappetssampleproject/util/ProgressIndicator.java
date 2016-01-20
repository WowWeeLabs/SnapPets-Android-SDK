package com.wowwee.snappetssampleproject.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by EricLau on 8/1/16.
 */
public class ProgressIndicator extends ImageView {
    private final RectF mRect = new RectF();
    private final RectF mRectInner = new RectF();
    private final Paint mPaintForeground = new Paint();
    private final Paint mPaintBackground = new Paint();
    private final Paint mPaintErase = new Paint();
    private static final Xfermode PORTER_DUFF_CLEAR = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    private int mColorForeground = Color.WHITE;
    private int mColorBackground = Color.BLACK;
    private float mValue;
    /**
     * Value which makes our custom drawn indicator have roughly the same size
     * as the built-in ProgressBar indicator. Unit: dp
     */
    private static final float PADDING = 2;
    private float mPadding;
    private Bitmap mBitmap;
    /**
     * Value which makes our custom drawn indicator have roughly the same
     * thickness as the built-in ProgressBar indicator. Expressed as the ration
     * between the inner and outer radiuses
     */
    private static final float INNER_RADIUS_RATIO = 0.84f;

    public ProgressIndicator(Context context) {
        this(context, null);
    }

    public ProgressIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources r = context.getResources();
        float scale = r.getDisplayMetrics().density;
        mPadding = scale * PADDING ;
        mPaintForeground.setColor(mColorForeground);
        mPaintForeground.setAntiAlias(true);
        mPaintBackground.setColor(mColorBackground);
        mPaintBackground.setAntiAlias(true);
        mPaintErase.setXfermode(PORTER_DUFF_CLEAR);
        mPaintErase.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, getWidth() / 2 - mBitmap.getWidth() / 2,
                getHeight() / 2 - mBitmap.getHeight() / 2, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        float bitmapWidth = w - 2 * mPadding;
        float bitmapHeight = h - 2 * mPadding;
        float radius = Math.min(bitmapWidth / 2, bitmapHeight / 2);
        mRect.set(0, 0, bitmapWidth, bitmapHeight);
        radius *= INNER_RADIUS_RATIO;
        mRectInner.set(bitmapWidth / 2f - radius, bitmapHeight / 2f - radius, bitmapWidth / 2f + radius, bitmapHeight / 2f + radius);
        updateBitmap();
    }

    /**
     * Set the foreground color for this indicator. The foreground is the part
     * of the indicator that shows the actual progress
     */
    public void setForegroundColor(int color) {
        this.mColorForeground = color;
        mPaintForeground.setColor(color);
        invalidate();
    }

    /**
     * Set the background color for this indicator. The background is a dim and subtle
     * part of the indicator that appears below the actual progress
     */
    public void setBackgroundColor(int color) {
        this.mColorBackground = color;
        mPaintBackground.setColor(color);
        invalidate();
    }

    /**
     * @param value A number between 0 and 1
     */
    public synchronized void setValue(float value) {
        mValue = value;
        updateBitmap();
    }

    private void updateBitmap() {
        if (mRect == null || mRect.width() == 0) {
            return;
        }
        mBitmap = Bitmap.createBitmap((int) mRect.width(), (int) mRect.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mBitmap);
        float angle = mValue * -360;
        canvas.drawArc(mRect, -450, angle, true, mPaintForeground);
        postInvalidate();
    }
}
