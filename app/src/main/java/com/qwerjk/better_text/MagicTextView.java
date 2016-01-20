package com.qwerjk.better_text;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Pair;
import android.widget.TextView;

import com.wowwee.snappetssampleproject.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.WeakHashMap;

public class MagicTextView extends TextView {
    private static HashMap<String, Typeface> typefaceCache = new HashMap<>();
    private ArrayList<Shadow> outerShadows;
    private ArrayList<Shadow> innerShadows;
    private WeakHashMap<String, Pair<Canvas, Bitmap>> canvasStore;
    private Canvas tempCanvas;
    private Bitmap tempBitmap;
    private Drawable foregroundDrawable;
    private float strokeWidth;
    private Integer strokeColor;
    private Join strokeJoin;
    private float strokeMiter;
    private int[] lockedCompoundPadding;
    private boolean frozen = false;
    private Integer gradientTopColor;
    private Integer gradientBottomColor;
    private Shader gradientShader;

    public MagicTextView(Context context) {
        super(context);
        init(null);
    }

    public MagicTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public MagicTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    public void init(AttributeSet attrs) {
        outerShadows = new ArrayList<>();
        innerShadows = new ArrayList<>();
        if (canvasStore == null) {
            canvasStore = new WeakHashMap<>();
        }

        if (attrs != null) {
            TypedArray customAttributes = getContext().obtainStyledAttributes(attrs, R.styleable.MagicTextView);

            setupTypeface(customAttributes);
            setupForeground(customAttributes);
            setupBackground(customAttributes);
            setupInnerShadowColor(customAttributes);
            setupOuterShadowColor(customAttributes);
            setupStrokeColor(customAttributes);
            setupGradient(customAttributes);

            customAttributes.recycle();
        }
    }

    private void setupGradient(TypedArray customAttributes) {
        if (customAttributes.hasValue(R.styleable.MagicTextView_gradientTop) && customAttributes.hasValue(R.styleable.MagicTextView_gradientBottom)) {
            this.gradientTopColor = customAttributes.getColor(R.styleable.MagicTextView_gradientTop, 0xff000000);
            this.gradientBottomColor = customAttributes.getColor(R.styleable.MagicTextView_gradientBottom, 0xff000000);
        }
    }

    private void setupStrokeColor(TypedArray customAttributes) {

        if (customAttributes.hasValue(R.styleable.MagicTextView_strokeColor)) {

            float strokeWidth = customAttributes.getFloat(R.styleable.MagicTextView_strokeWidth, 1);
            int strokeColor = customAttributes.getColor(R.styleable.MagicTextView_strokeColor, 0xff000000);
            float strokeMiter = customAttributes.getFloat(R.styleable.MagicTextView_strokeMiter, 10);
            Join strokeJoin = null;
            switch (customAttributes.getInt(R.styleable.MagicTextView_strokeJoinStyle, 0)) {
                case (0):
                    strokeJoin = Join.MITER;
                    break;
                case (1):
                    strokeJoin = Join.BEVEL;
                    break;
                case (2):
                    strokeJoin = Join.ROUND;
                    break;
            }
            this.setStroke(strokeWidth, strokeColor, strokeJoin, strokeMiter);
        }
    }

    private void setupOuterShadowColor(TypedArray customAttributes) {

        if (customAttributes.hasValue(R.styleable.MagicTextView_outerShadowColor)) {
            this.addOuterShadow(customAttributes.getFloat(R.styleable.MagicTextView_outerShadowRadius, 0),
                    customAttributes.getFloat(R.styleable.MagicTextView_outerShadowDx, 0),
                    customAttributes.getFloat(R.styleable.MagicTextView_outerShadowDy, 0),
                    customAttributes.getColor(R.styleable.MagicTextView_outerShadowColor, 0xff000000));
        }
    }

    private void setupInnerShadowColor(TypedArray customAttributes) {
        if (customAttributes.hasValue(R.styleable.MagicTextView_innerShadowColor)) {
            this.addInnerShadow(customAttributes.getFloat(R.styleable.MagicTextView_innerShadowRadius, 0),
                    customAttributes.getFloat(R.styleable.MagicTextView_innerShadowDx, 0),
                    customAttributes.getFloat(R.styleable.MagicTextView_innerShadowDy, 0),
                    customAttributes.getColor(R.styleable.MagicTextView_innerShadowColor, 0xff000000));
        }
    }

    private void setupBackground(TypedArray customAttributes) {

        if (customAttributes.hasValue(R.styleable.MagicTextView_backgroundcolor)) {
            Drawable background = customAttributes.getDrawable(R.styleable.MagicTextView_backgroundcolor);
            if (background != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    this.setBackground(background);
                } else {
                    //noinspection deprecation
                    this.setBackgroundDrawable(background);
                }
            } else {
                this.setBackgroundColor(customAttributes.getColor(R.styleable.MagicTextView_backgroundcolor, 0xff000000));
            }
        }
    }

    private void setupForeground(TypedArray customAttributes) {
        if (customAttributes.hasValue(R.styleable.MagicTextView_foreground)) {
            Drawable foreground = customAttributes.getDrawable(R.styleable.MagicTextView_foreground);
            if (foreground != null) {
                this.setForegroundDrawable(foreground);
            } else {
                this.setTextColor(customAttributes.getColor(R.styleable.MagicTextView_foreground, 0xff000000));
            }
        }
    }

    private void setupTypeface(TypedArray customAttributes) {
        String typefaceName = customAttributes.getString(R.styleable.MagicTextView_typeface);
        if (typefaceName != null) {
            if (!(isInEditMode())) {
                Typeface tf = null;
                if (typefaceCache.containsKey(typefaceName)) {
                    tf = typefaceCache.get(typefaceName);
                }
                if (tf == null) {
                    tf = Typeface.createFromAsset(getContext().getAssets(), String.format("fonts/%s", typefaceName));
                }
                // add typeface to cache
                if (!(typefaceCache.containsKey(typefaceName))) {
                    typefaceCache.put(typefaceName, tf);
                }

                setTypeface(tf);
            }
        }
    }

    public void setStroke(float width, int color, Join join, float miter) {
        strokeWidth = width;
        strokeColor = color;
        strokeJoin = join;
        strokeMiter = miter;
    }

    public void addOuterShadow(float r, float dx, float dy, int color) {
        if (r == 0) {
            r = 0.0001f;
        }
        outerShadows.add(new Shadow(r, dx, dy, color));
    }

    public void addInnerShadow(float r, float dx, float dy, int color) {
        if (r == 0) {
            r = 0.0001f;
        }
        innerShadows.add(new Shadow(r, dx, dy, color));
    }

    public void setForegroundDrawable(Drawable d) {
        this.foregroundDrawable = d;
    }


    @Override
    public void onDraw(@NonNull Canvas canvas) {

        // Gradient
        if (gradientBottomColor != null && gradientTopColor != null) {
            if (gradientShader == null) {
                gradientShader = new LinearGradient(0, 0, 0, this.getMeasuredHeight(), new int[]{gradientTopColor, gradientBottomColor}, null, TileMode.CLAMP);
            }
            this.getPaint().setShader(gradientShader);
        }

        super.onDraw(canvas);

        freeze();
        Drawable restoreBackground = this.getBackground();
        Drawable[] restoreDrawables = this.getCompoundDrawables();
        int restoreColor = this.getCurrentTextColor();

        this.setCompoundDrawables(null, null, null, null);

        for (Shadow shadow : outerShadows) {
            this.setShadowLayer(shadow.r, shadow.dx, shadow.dy, shadow.color);
            super.onDraw(canvas);
        }
        this.setShadowLayer(0, 0, 0, 0);
        this.setTextColor(restoreColor);

        if (this.foregroundDrawable != null && this.foregroundDrawable instanceof BitmapDrawable) {
            generateTempCanvas();
            super.onDraw(tempCanvas);
            Paint paint = ((BitmapDrawable) this.foregroundDrawable).getPaint();
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
            this.foregroundDrawable.setBounds(canvas.getClipBounds());
            this.foregroundDrawable.draw(tempCanvas);
            canvas.drawBitmap(tempBitmap, 0, 0, null);
            tempCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }

        if (strokeColor != null) {
            TextPaint paint = this.getPaint();
            this.getPaint().setShader(null);
            paint.setStyle(Style.STROKE);
            paint.setStrokeJoin(strokeJoin);
            paint.setStrokeMiter(strokeMiter);
            this.setTextColor(strokeColor);
            paint.setStrokeWidth(strokeWidth);
            super.onDraw(canvas);
            paint.setStyle(Style.FILL);
            this.setTextColor(restoreColor);
        }
        if (innerShadows.size() > 0) {
            generateTempCanvas();
            TextPaint paint = this.getPaint();
            for (Shadow shadow : innerShadows) {
                this.setTextColor(shadow.color);
                super.onDraw(tempCanvas);
                this.setTextColor(0xFF000000);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
                paint.setMaskFilter(new BlurMaskFilter(shadow.r, BlurMaskFilter.Blur.NORMAL));

                tempCanvas.save();
                tempCanvas.translate(shadow.dx, shadow.dy);
                super.onDraw(tempCanvas);
                tempCanvas.restore();
                canvas.drawBitmap(tempBitmap, 0, 0, null);
                tempCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                paint.setXfermode(null);
                paint.setMaskFilter(null);
                this.setTextColor(restoreColor);
                this.setShadowLayer(0, 0, 0, 0);
            }
        }

        this.setCompoundDrawablesWithIntrinsicBounds(restoreDrawables[0], restoreDrawables[1], restoreDrawables[2], restoreDrawables[3]);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            this.setBackground(restoreBackground);
        } else {
            //noinspection deprecation
            this.setBackgroundDrawable(restoreBackground);
        }
        this.setTextColor(restoreColor);

        unfreeze();
    }

    private void generateTempCanvas() {
        String key = String.format("%dx%d", getWidth(), getHeight());
        Pair<Canvas, Bitmap> stored = canvasStore.get(key);
        if (stored != null) {
            tempCanvas = stored.first;
            tempBitmap = stored.second;
        } else {
            tempCanvas = new Canvas();
            tempBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            tempCanvas.setBitmap(tempBitmap);
            canvasStore.put(key, new Pair<>(tempCanvas, tempBitmap));
        }
    }


    // Keep these things locked while onDraw in processing
    public void freeze() {
        lockedCompoundPadding = new int[]{
                getCompoundPaddingLeft(),
                getCompoundPaddingRight(),
                getCompoundPaddingTop(),
                getCompoundPaddingBottom()
        };
        frozen = true;
    }

    public void unfreeze() {
        frozen = false;
    }


    @Override
    public void requestLayout() {
        if (!frozen) super.requestLayout();
    }

    @Override
    public void postInvalidate() {
        if (!frozen) super.postInvalidate();
    }

    @Override
    public void postInvalidate(int left, int top, int right, int bottom) {
        if (!frozen) super.postInvalidate(left, top, right, bottom);
    }

    @Override
    public void invalidate() {
        if (!frozen) super.invalidate();
    }

    @Override
    public void invalidate(@NonNull Rect rect) {
        if (!frozen) super.invalidate(rect);
    }

    @Override
    public void invalidate(int l, int t, int r, int b) {
        if (!frozen) super.invalidate(l, t, r, b);
    }

    @Override
    public int getCompoundPaddingLeft() {
        return !frozen ? super.getCompoundPaddingLeft() : lockedCompoundPadding[0];
    }

    @Override
    public int getCompoundPaddingRight() {
        return !frozen ? super.getCompoundPaddingRight() : lockedCompoundPadding[1];
    }

    @Override
    public int getCompoundPaddingTop() {
        return !frozen ? super.getCompoundPaddingTop() : lockedCompoundPadding[2];
    }

    @Override
    public int getCompoundPaddingBottom() {
        return !frozen ? super.getCompoundPaddingBottom() : lockedCompoundPadding[3];
    }
}
