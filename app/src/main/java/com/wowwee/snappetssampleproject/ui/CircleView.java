package com.wowwee.snappetssampleproject.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.wowwee.snappetssampleproject.R;

public class CircleView extends ImageView {
    private static final int MIN_SIZE_DEFAULT = 10;
    private float progress = 0.0f;

    public CircleView(Context context) {
        super(context);
        setFocusable(true);
        init();
    }

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
        init();
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFocusable(true);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        setWillNotCacheDrawing(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int wSpec = MeasureSpec.getSize(widthMeasureSpec);
        int hSpec = MeasureSpec.getSize(heightMeasureSpec);
        int width = Math.max(wSpec, MIN_SIZE_DEFAULT);
        int height = Math.max(hSpec, MIN_SIZE_DEFAULT);
        setMeasuredDimension(width, height);
    }


    public void updateCircle(int imageBufferSize, int imageTotalSize) {
        progress = imageBufferSize / (float) imageTotalSize;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.TRANSPARENT);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(getResources().getColor(R.color.dark_grey_color));
        p.setStyle(Paint.Style.FILL);
        canvas.drawArc(new RectF(0, 0, getWidth(), getHeight()), 360 - 90, (360 - progress * 360), true, p);
    }
}
