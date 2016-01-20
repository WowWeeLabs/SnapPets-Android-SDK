package com.wowwee.snappetssampleproject.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;

import com.wowwee.snappetssampleproject.R;

public class DimmableButton extends Button {
    private final int ALPHA_FOR_NORMAL = 255;
    private final int ALPHA_FOR_DISABLE = 128;

    private BitmapDrawable bgDrawable;
    private Paint bgPaint;

    private float m_alpha;

    public DimmableButton(Context context) {
        super(context);
        init(null);
    }

    public DimmableButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    public DimmableButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public void init(AttributeSet attrs) {
        if (attrs != null) {
            {
                TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MagicTextView);
                String typefaceName = a.getString(R.styleable.MagicTextView_typeface);
                if (typefaceName != null) {
                    if (!(isInEditMode())) {
                        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), String.format("fonts/%s", typefaceName));
                        setTypeface(tf);
                    }
                }
            }

            {
                TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.DimmableButton);
                Drawable d = a.getDrawable(R.styleable.DimmableButton_dimmableBackground);
                if (d != null && d instanceof BitmapDrawable) {
                    bgPaint = new Paint();

                    bgDrawable = (BitmapDrawable) d;

                    setBackground(null);
                }
            }
            this.m_alpha = 1.0f;
        }
    }

    public void setDimmableBackground(int resid) {
        bgDrawable = (BitmapDrawable) getContext().getResources().getDrawable(resid);

        //force it to reload
        this.invalidate();
    }

    public void setDimmableBackground(Drawable background) {
        bgDrawable = (BitmapDrawable) background;

        //force it to reload
        this.invalidate();
    }

    public float getAlpha() {
        return this.m_alpha;
    }

    public void setAlpha(float alpha) {
        this.m_alpha = alpha;
        super.setAlpha(this.m_alpha);
    }

    public void reset() {
        if (bgDrawable != null) {
            bgPaint.setColorFilter(new PorterDuffColorFilter(Color.WHITE, Mode.MULTIPLY));
        } else {
            getBackground().setColorFilter(Color.WHITE, Mode.MULTIPLY);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bgDrawable != null) {
            bgPaint.setAlpha((int) (getAlpha() * (isEnabled() ? ALPHA_FOR_NORMAL : ALPHA_FOR_DISABLE)));

            Matrix m = new Matrix();
            RectF drawableRect = new RectF(0, 0, bgDrawable.getIntrinsicWidth(), bgDrawable.getIntrinsicHeight());
            RectF viewRect = new RectF(0, 0, getMeasuredWidth(), getMeasuredHeight());
            m.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);
            canvas.drawBitmap(bgDrawable.getBitmap(), m, bgPaint);
        } else {
            getBackground().setAlpha((int) (getAlpha() * (isEnabled() ? ALPHA_FOR_NORMAL : ALPHA_FOR_DISABLE)));
        }
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);

        if (isEnabled() && isClickable()) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (bgDrawable != null) {
                    bgPaint.setColorFilter(new PorterDuffColorFilter(Color.GRAY, Mode.MULTIPLY));
                } else {
                    getBackground().setColorFilter(Color.GRAY, Mode.MULTIPLY);
                }
                invalidate();
            } else if (event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL) {
                if (bgDrawable != null) {
                    bgPaint.setColorFilter(new PorterDuffColorFilter(Color.WHITE, Mode.MULTIPLY));
                } else {
                    getBackground().setColorFilter(Color.WHITE, Mode.MULTIPLY);
                }
                invalidate();
            }
        }

        return result;
    }

}
