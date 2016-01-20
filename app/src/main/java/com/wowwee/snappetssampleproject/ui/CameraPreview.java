package com.wowwee.snappetssampleproject.ui;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;

import java.io.IOException;

/*
 * original copy from http://developer.android.com/reference/android/view/TextureView.html
 */

/**
 * A basic Camera preview class
 */
public class CameraPreview extends TextureView implements SurfaceTextureListener {
    private final String TAG = CameraPreview.class.toString();

    private Camera mCamera;

    //TODO - fix error preview scale after pressing home button
    public CameraPreview(Context context, Camera camera) {
        super(context);

        if (camera != null) {
            Camera.Parameters params = camera.getParameters();
            Camera.Size size = params.getPreviewSize();

            //zoom to crop the result to fit it to square
            Matrix transform = new Matrix();
            transform.setScale(1, size.width / (float) size.height);
            setTransform(transform);

            setSurfaceTextureListener(this);

            updateCamera(camera);
        }
    }

    public void updateCamera(Camera camera) {
        if (camera != null) {
            mCamera = camera;

            startPreview(getSurfaceTexture());
        }
    }

    private void startPreview(SurfaceTexture surface) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewTexture(surface);
                mCamera.startPreview();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    public void disbleCallback() {
        setSurfaceTextureListener(null);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        startPreview(surface);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
        }
        return true;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }
}