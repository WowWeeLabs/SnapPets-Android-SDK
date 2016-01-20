package com.wowwee.snappetssampleproject.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class FitWidthSquareRelativeLayout extends RelativeLayout {

    public FitWidthSquareRelativeLayout(Context context) {
        super(context);
    }

    public FitWidthSquareRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FitWidthSquareRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
