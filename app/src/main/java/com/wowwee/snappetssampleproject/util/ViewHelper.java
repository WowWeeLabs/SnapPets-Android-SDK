package com.wowwee.snappetssampleproject.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.qwerjk.better_text.MagicTextView;

/**
 * Created by Artem on 23.09.2015.
 */
public final class ViewHelper {

    public static void showViewWithAnimation(Context context, final View v) {
        if (v.getVisibility() != View.VISIBLE) {
            v.setAlpha(0);
            v.setVisibility(View.VISIBLE);

            v.animate()
                    .alpha(1f)
                    .setDuration(context.getResources().getInteger(
                            android.R.integer.config_shortAnimTime))
                    .setListener(null);
        }
    }

    public static void hideViewWithAnimation(Context context, final View v) {
        if (v.getVisibility() != View.GONE)
            v.animate()
                    .alpha(0f)
                    .setDuration(context.getResources().getInteger(
                            android.R.integer.config_shortAnimTime))
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            v.setVisibility(View.GONE);
                        }
                    });
    }

    public static void invisibleViewWithAnimation(Context context, final View v) {
        if (v.getVisibility() != View.INVISIBLE)
            v.animate()
                    .alpha(0f)
                    .setDuration(context.getResources().getInteger(
                            android.R.integer.config_shortAnimTime))
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            v.setVisibility(View.INVISIBLE);
                        }
                    });
    }

    public static void setButtonTouchListener(View b) {
        final int onTouchColor = Color.GREEN;
        final int nonTouchColor = Color.TRANSPARENT;

        b.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (v instanceof ImageView)
                        ((ImageView) v).setColorFilter(onTouchColor, PorterDuff.Mode.MULTIPLY);
                    else if (v instanceof ImageButton)
                        ((ImageButton) v).setColorFilter(onTouchColor, PorterDuff.Mode.MULTIPLY);
                    else if (v instanceof MagicTextView) {
                        ((MagicTextView) v).setTextColor(onTouchColor);
                        if (((MagicTextView) v).getCompoundDrawables()[0] != null)
                            ((MagicTextView) v).getCompoundDrawables()[0].setColorFilter(onTouchColor, PorterDuff.Mode.SRC_IN);
                    } else if (v instanceof TextView) {
                        ((TextView) v).setTextColor(onTouchColor);
                        if (((TextView) v).getCompoundDrawables()[0] != null)
                            ((TextView) v).getCompoundDrawables()[0].setColorFilter(onTouchColor, PorterDuff.Mode.SRC_IN);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (v instanceof ImageView)
                        ((ImageView) v).setColorFilter(nonTouchColor, PorterDuff.Mode.XOR);
                    else if (v instanceof ImageButton)
                        ((ImageButton) v).setColorFilter(nonTouchColor, PorterDuff.Mode.XOR);
                    else if (v instanceof MagicTextView) {
                        ((MagicTextView) v).setTextColor(Color.WHITE);
                        if (((MagicTextView) v).getCompoundDrawables()[0] != null)
                            ((MagicTextView) v).getCompoundDrawables()[0].setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                    } else if (v instanceof TextView) {
                        ((TextView) v).setTextColor(Color.WHITE);
                        if (((TextView) v).getCompoundDrawables()[0] != null)
                            ((TextView) v).getCompoundDrawables()[0].setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                    }
                }
                return false;
            }

        });
    }

    public static void setButtonTouchListener(View b, int color) {
        final int onTouchColor = Color.GREEN;
        final int nonTouchColor = color;

        b.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (v instanceof ImageView)
                        ((ImageView) v).setColorFilter(onTouchColor, PorterDuff.Mode.MULTIPLY);
                    else if (v instanceof ImageButton)
                        ((ImageButton) v).setColorFilter(onTouchColor, PorterDuff.Mode.MULTIPLY);
                    else if (v instanceof MagicTextView) {
                        ((MagicTextView) v).setTextColor(onTouchColor);
                        if (((MagicTextView) v).getCompoundDrawables()[0] != null)
                            ((MagicTextView) v).getCompoundDrawables()[0].setColorFilter(onTouchColor, PorterDuff.Mode.SRC_IN);
                    } else if (v instanceof TextView) {
                        ((TextView) v).setTextColor(onTouchColor);
                        if (((TextView) v).getCompoundDrawables()[0] != null)
                            ((TextView) v).getCompoundDrawables()[0].setColorFilter(onTouchColor, PorterDuff.Mode.SRC_IN);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (v instanceof ImageView)
                        ((ImageView) v).setColorFilter(nonTouchColor, PorterDuff.Mode.XOR);
                    else if (v instanceof ImageButton)
                        ((ImageButton) v).setColorFilter(nonTouchColor, PorterDuff.Mode.XOR);
                    else if (v instanceof MagicTextView) {
                        ((MagicTextView) v).setTextColor(nonTouchColor);
                        if (((MagicTextView) v).getCompoundDrawables()[0] != null)
                            ((MagicTextView) v).getCompoundDrawables()[0].setColorFilter(nonTouchColor, PorterDuff.Mode.SRC_IN);
                    } else if (v instanceof TextView) {
                        ((TextView) v).setTextColor(nonTouchColor);
                        if (((TextView) v).getCompoundDrawables()[0] != null)
                            ((TextView) v).getCompoundDrawables()[0].setColorFilter(nonTouchColor, PorterDuff.Mode.SRC_IN);
                    }
                }
                return false;
            }

        });
    }

    public static void setButtonTouchListener(View mainView, final View b, int color) {
        final int onTouchColor = Color.GREEN;
        final int nonTouchColor = color;

        mainView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (b instanceof ImageView)
                        ((ImageView) b).setColorFilter(onTouchColor, PorterDuff.Mode.MULTIPLY);
                    else if (b instanceof ImageButton)
                        ((ImageButton) b).setColorFilter(onTouchColor, PorterDuff.Mode.MULTIPLY);
                    else if (b instanceof MagicTextView) {
                        ((MagicTextView) b).setTextColor(onTouchColor);
                        if (((MagicTextView) b).getCompoundDrawables()[0] != null)
                            ((MagicTextView) b).getCompoundDrawables()[0].setColorFilter(onTouchColor, PorterDuff.Mode.SRC_IN);
                    } else if (v instanceof TextView) {
                        ((TextView) b).setTextColor(onTouchColor);
                        if (((TextView) b).getCompoundDrawables()[0] != null)
                            ((TextView) b).getCompoundDrawables()[0].setColorFilter(onTouchColor, PorterDuff.Mode.SRC_IN);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (b instanceof ImageView)
                        ((ImageView) b).setColorFilter(nonTouchColor, PorterDuff.Mode.XOR);
                    else if (b instanceof ImageButton)
                        ((ImageButton) b).setColorFilter(nonTouchColor, PorterDuff.Mode.XOR);
                    else if (b instanceof MagicTextView) {
                        ((MagicTextView) b).setTextColor(nonTouchColor);
                        if (((MagicTextView) b).getCompoundDrawables()[0] != null)
                            ((MagicTextView) b).getCompoundDrawables()[0].setColorFilter(nonTouchColor, PorterDuff.Mode.SRC_IN);
                    } else if (b instanceof TextView) {
                        ((TextView) b).setTextColor(nonTouchColor);
                        if (((TextView) b).getCompoundDrawables()[0] != null)
                            ((TextView) b).getCompoundDrawables()[0].setColorFilter(nonTouchColor, PorterDuff.Mode.SRC_IN);
                    }
                }
                return false;
            }

        });
    }

    public static int navBarHeight(Context context) {
        context.getResources();
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }
}
