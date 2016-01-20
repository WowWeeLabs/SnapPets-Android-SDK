package com.wowwee.snappetssampleproject.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.TextView;

import java.util.ArrayList;

public class ViewUtility {
    public static float s_scale = 0.0f;

    public static View createView(Context context, int layoutId) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        return inflater.inflate(layoutId, null, false);
    }

    public static ArrayList<View> getViewsByTag(View root, String tag) {
        ArrayList<View> views = new ArrayList<View>();
        if (root instanceof ViewGroup) {
            ViewGroup _root = (ViewGroup) root;

            final int childCount = _root.getChildCount();
            for (int i = 0; i < childCount; i++) {
                views.addAll(getViewsByTag(_root.getChildAt(i), tag));
            }
        }

        if (haveTag(root, tag)) {
            views.add(root);
        }

        return views;
    }

    private static boolean haveTag(View view, String tag) {
        final Object tagObj = view.getTag();
        if (tagObj != null && tagObj.equals(tag)) {
            return true;
        }

        return false;
    }

    public static void scaleViewsByTag(View root, String tag, float scale, float density) {
        ArrayList<View> tagViews = ViewUtility.getViewsByTag(root, tag);

        Log.d("EB_debug", root.getClass().toString() + " tagViews.size(): " + tagViews.size());

        for (View view : tagViews) {
//			Log.d("EB_debug", "[ViewUtility]scaleViewsByTag: "+root.getClass().toString()+":"+view.getId());

            LayoutParams LP = view.getLayoutParams();
            if (LP.width > ViewGroup.LayoutParams.MATCH_PARENT) {
                LP.width = (int) (view.getMeasuredWidth() * scale);
            }
            if (LP.height > ViewGroup.LayoutParams.MATCH_PARENT) {
                LP.height = (int) (view.getMeasuredHeight() * scale);
            }

            if (LP instanceof MarginLayoutParams) {
                MarginLayoutParams mLP = (MarginLayoutParams) LP;
                mLP.leftMargin *= scale;
                mLP.rightMargin *= scale;
                mLP.topMargin *= scale;
                mLP.bottomMargin *= scale;
                view.setLayoutParams(mLP);
            }

            view.setPadding((int) (view.getPaddingLeft() * scale), (int) (view.getPaddingTop() * scale), (int) (view.getPaddingRight() * scale), (int) (view.getPaddingBottom() * scale));

            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                textView.setTextSize(textView.getTextSize() * scale / density);
            }
        }
    }

    public static void autoScaleViewsByTag(Context context, ScreenReferenceInterface screenReference, View root, String tag) {
//		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        DisplayMetrics metrics = new DisplayMetrics();
        Rect rect = new Rect();
        ((Activity) context).getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        ((Activity) context).getWindowManager().getDefaultDisplay().getRectSize(rect);

        Log.d("EB_debug", "metrics.density: " + metrics.density);
        Log.d("EB_debug", "metrics.densityDpi: " + metrics.densityDpi);
        Log.d("EB_debug", "metrics.widthPixels: " + metrics.widthPixels);
        Log.d("EB_debug", "metrics.heightPixels: " + metrics.heightPixels);


//		float scale  = screenReference.getReferenceDensity() / screenReference.getReferenceHeightPixels() * Math.max(metrics.heightPixels,metrics.widthPixels) / metrics.densityDpi;
        float scale = screenReference.getReferenceDensity() / screenReference.getReferenceHeightPixels() * Math.max(rect.height(), rect.width()) / metrics.densityDpi;
        s_scale = scale;

        Log.d("EB_debug", "scale: " + scale);

        //ignore to scale the views if the change is not big
        if (Math.abs(1 - scale) > 0.0f) {
            scaleViewsByTag(root, tag, scale, metrics.density);
        }
    }

    public static float getScale() {
        return s_scale;
    }

    public interface ScreenReferenceInterface {
        public float getReferenceDensity();

        public float getReferenceWidthPixels();

        public float getReferenceHeightPixels();
    }
}
