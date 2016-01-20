package com.wowwee.snappetssampleproject.util;

import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

public class ImageViewAnimationPlayback {

    private Handler handler;
    private Runnable runnable;
    private int drawableIdIndex = 0;
    private int interval;

    public ImageViewAnimationPlayback(final ImageView imageView, final int[] drawableIds, int interval) {
        this.interval = interval;

        imageView.setImageResource(drawableIds[drawableIdIndex]);

        runnable = new Runnable() {
            @Override
            public void run() {
                drawableIdIndex = (drawableIdIndex + 1) % drawableIds.length;

                imageView.setImageResource(drawableIds[drawableIdIndex]);

                if (handler != null) {
                    handler.postDelayed(runnable, ImageViewAnimationPlayback.this.interval);
                }
            }
        };

        start();
    }

    public ImageViewAnimationPlayback(final View view, final float[] scales, int interval) {
        this.interval = interval;

        view.setScaleX(scales[drawableIdIndex]);
        view.setScaleY(scales[drawableIdIndex]);

        runnable = new Runnable() {
            @Override
            public void run() {
                drawableIdIndex = (drawableIdIndex + 1) % scales.length;

                view.setScaleX(scales[drawableIdIndex]);
                view.setScaleY(scales[drawableIdIndex]);

                if (handler != null) {
                    handler.postDelayed(runnable, ImageViewAnimationPlayback.this.interval);
                }
            }
        };

        start();
    }

    public ImageViewAnimationPlayback(final View view, TYPE type, final float[] values, int interval) {
        this.interval = interval;

        if (type == TYPE.ALPHA) {
            view.setAlpha(values[drawableIdIndex]);
        }

        runnable = new Runnable() {
            @Override
            public void run() {
                drawableIdIndex = (drawableIdIndex + 1) % values.length;

                view.setAlpha(values[drawableIdIndex]);

                if (handler != null) {
                    handler.postDelayed(runnable, ImageViewAnimationPlayback.this.interval);
                }
            }
        };
    }

    public void start() {
        stop();

        handler = new Handler();
        handler.postDelayed(runnable, interval);
    }

    public void stop() {
        if (handler != null) {
            handler.removeCallbacks(runnable);
            handler = null;
        }
    }

    public enum TYPE {
        ALPHA
    }
}
