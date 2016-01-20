package com.wowwee.snappetssampleproject.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.os.Environment;
import android.util.Log;

import com.wowwee.snappetssampleproject.enums.FLASH_MODE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class CameraInstance {
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static CameraInstance instance = null;
    private final boolean SHOW_INFO = false;
    private final String TAG = CameraInstance.class.toString();
    public Camera camera;
    private Context context;
    private String folderName;
    private int expectedWidth;
    private int expectedHeight;
    private Camera.Size previewSize;
    private Camera.Size pictureSize;
    private PictureCallback mPicture;
    private int cameraId = 0;
    private Callback callback;
    private FLASH_MODE flashMode = null;

    private CameraInstance(Context context, String folderName, int expectedWidth, int expectedHeight) {
        this.context = context;

        this.folderName = folderName;
        this.expectedWidth = expectedWidth;
        this.expectedHeight = expectedHeight;

        camera = getCameraInstance();
        setCameraFlash(FLASH_MODE.OFF);

        mPicture = new PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null) {
                    Log.d(TAG, "Error creating media file, check storage permissions");

                    if (callback != null) {
                        callback.afterTakePicture(null);
                        callback = null;
                    }
                    return;
                }

                try {
                    Bitmap realImage = BitmapFactory.decodeByteArray(data, 0, data.length);

                    //get Camera Info
                    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                    Camera.getCameraInfo(cameraId, cameraInfo);

                    //rotate and crop the image
                    Matrix matrix = new Matrix();
                    if (camera != null) {
                        //apply the rotation for phone ONLY
                        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                            matrix.setRotate(-90);
                            realImage = Bitmap.createBitmap(realImage, realImage.getWidth() - realImage.getHeight(), 0, realImage.getHeight(), realImage.getHeight(), matrix, true);
                        } else {
                            matrix.setRotate(90);
                            realImage = Bitmap.createBitmap(realImage, 0, 0, realImage.getHeight(), realImage.getHeight(), matrix, true);
                        }
                    }

                    if(pictureFile != null && realImage != null) {
                        FileOutputStream fos = new FileOutputStream(pictureFile);
                        realImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        fos.close();

                        if (callback != null) {
                            callback.afterTakePicture(pictureFile);
                            callback = null;
                        }
                    }
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "File not found: " + e.getMessage());

                    if (callback != null) {
                        callback.afterTakePicture(null);
                        callback = null;
                    }
                } catch (IOException e) {
                    Log.d(TAG, "Error accessing file: " + e.getMessage());

                    if (callback != null) {
                        callback.afterTakePicture(null);
                        callback = null;
                    }
                }

                if (camera != null) {
                    camera.startPreview();
                }
            }
        };
    }

    public static CameraInstance createInstance(Context context, String folderName, int expectedWidth, int expectedHeight) {
        if (instance == null) {
            instance = new CameraInstance(context, folderName, expectedWidth, expectedHeight);
        }

        return instance;
    }

    public static CameraInstance getInstance() {
        return instance;
    }

    public void takePicture(Callback cb) {
        callback = cb;

        if (callback != null) {
            callback.beforeTakePicture();
        }

        if (camera != null && mPicture != null)
            camera.takePicture(null, null, mPicture);
    }

    public void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    public void takePictureFromSnappet(Callback cb, byte[] data) {
        callback = cb;

        mPicture.onPictureTaken(data, null);
    }

    /**
     * Create a File for saving an image or video
     */
    private File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), folderName);
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    private List<File> getFiles(int type) {
        List<File> mediaFiles = new ArrayList<File>();

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), folderName);
        File[] files = mediaStorageDir.listFiles();

        //do the sorting
        if (files != null) {
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File lhs, File rhs) {
                    return lhs.getPath().compareTo(rhs.getPath());
                }
            });


            for (File f : files) {
                if (type == MEDIA_TYPE_IMAGE && f.getPath().endsWith(".jpg")) {
                    mediaFiles.add(f);
                } else if (type == MEDIA_TYPE_VIDEO && f.getPath().endsWith(".mp4")) {
                    mediaFiles.add(f);
                }
            }
//			Log.i("File Count", ""+files.length);
        }
        return mediaFiles;
    }

    public List<File> getPictures() {
        return getFiles(MEDIA_TYPE_IMAGE);
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    private Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(cameraId); // attempt to get a Camera instance
            c.setDisplayOrientation(90);

            Camera.Parameters params = c.getParameters();

            if (SHOW_INFO) {
                Log.d(TAG, "Camera ID: " + cameraId);

                for (Camera.Size size : params.getSupportedPreviewSizes()) {
                    Log.d(TAG, "Supported Preview Size: " + size.width + "x" + size.height + " -> ratio: " + size.width / (float) size.height);
                }

                for (Camera.Size size : params.getSupportedPictureSizes()) {
                    Log.d(TAG, "Supported Picture Size:" + size.width + "x" + size.height + " -> ratio: " + size.width / (float) size.height);
                }
            }

            previewSize = getOptimalSize(params.getSupportedPreviewSizes(), expectedWidth, expectedHeight);
            if (previewSize != null) {
                Log.d(TAG, "Choose Preview Size: " + previewSize.width + "x" + previewSize.height + " -> ratio: " + previewSize.width / (float) previewSize.height);

                params.setPreviewSize(previewSize.width, previewSize.height);
            }
            pictureSize = getOptimalSize(params.getSupportedPictureSizes(), expectedWidth, expectedHeight);
            if (pictureSize != null) {
                Log.d(TAG, "Choose Picture Size: " + pictureSize.width + "x" + pictureSize.height + " -> ratio: " + pictureSize.width / (float) pictureSize.height);

                params.setPictureSize(pictureSize.width, pictureSize.height);
            }
            //setup auto focus
            List<String> focusModes = params.getSupportedFocusModes();
            if (focusModes.contains(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) // Fixed a bug
                params.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

            c.setParameters(params);

        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Log.i("Exception", e.toString());
        }
        return c; // returns null if camera is unavailable
    }

    public void createCamera() {
        if (camera == null)
            camera = getCameraInstance();
    }

    public boolean hasCameraSwitch() {
        return getCameraCount() > 1;
    }

    public boolean switchCamera() {
        int count = getCameraCount();

        if (count > 1) {
            cameraId = (cameraId + 1) % count;

            releaseCamera();

            camera = getCameraInstance();

            return true;
        }

        return false;
    }

    public int getCameraId() {
        return cameraId;
    }

    public int getCameraCount() {
        return Camera.getNumberOfCameras();
    }

    public boolean hasCameraFlash() {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    public boolean setCameraFlash(FLASH_MODE mode) {
        if (flashMode != mode) {
            flashMode = mode;

            if (camera == null) return false;
            Camera.Parameters params = camera.getParameters();
            List<String> flashModes = params.getSupportedFlashModes();
            if (flashModes != null && flashModes.contains(mode.value))
                params.setFlashMode(mode.value);
            camera.setParameters(params);

            return true;
        }

        return false;
    }

    public FLASH_MODE getCameraFlash() {
        return flashMode;
    }

    /*
     * reference -> http://stackoverflow.com/questions/19577299/android-camera-preview-stretched
     */
    private Camera.Size getOptimalSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public interface Callback {
        public void init();

        public void beforeTakePicture();

        public void afterTakePicture(File file);
    }
}