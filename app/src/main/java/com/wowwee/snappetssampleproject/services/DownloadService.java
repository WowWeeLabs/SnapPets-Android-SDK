package com.wowwee.snappetssampleproject.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.wowwee.bluetoothrobotcontrollib.snappets.SnapPets;
import com.wowwee.snappetssampleproject.enums.DOWNLOAD_STATE;
import com.wowwee.snappetssampleproject.interfaces.IDownloadImageStatisticListener;
import com.wowwee.snappetssampleproject.snappethelper.ConnectSnappetCallback;
import com.wowwee.snappetssampleproject.snappethelper.SnappetsHelper;
import com.wowwee.snappetssampleproject.util.CameraInstance;

import java.io.File;

public class DownloadService extends Service {
    private static final long CHECK_PHOTOS_SLEEP = 30000;
    private final IBinder mBinder = new LocalBinder();

    private IDownloadImageStatisticListener statisticListener;

    private CameraInstance.Callback imagesDownloadingCameraCallback;
    private CameraInstance.Callback takingPhotoCameraCallback;
    private CameraInstance.Callback idleCameraCallback;

    private DOWNLOAD_STATE currentState = DOWNLOAD_STATE.IDLE;
    private ConnectSnappetCallback photoTakingCallback;
    private ConnectSnappetCallback idleCallback;
    private ConnectSnappetCallback photoDownloadingCallback;
    private Thread checkPhotosOnDeviceThread;
    private Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        startUpdateCurrentState();
        //startCheckNewPhotosThread();
    }

    public void onDestory() {
        super.onDestroy();
        this.stopThread();
    }

    private void startUpdateCurrentState() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (SnappetsHelper.getInstance() != null) {
                        if (!SnappetsHelper.getInstance().isConnectedSnappet()) {
                            currentState = DOWNLOAD_STATE.IDLE;
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                return;
                            }
                        }
                    }
                }
            }
        }).start();
    }

    public void stopThread() {
        if (checkPhotosOnDeviceThread != null)
            checkPhotosOnDeviceThread.interrupt();
        checkPhotosOnDeviceThread = null;
    }

    public void startCheckNewPhotosThread() {
        if (checkPhotosOnDeviceThread != null && checkPhotosOnDeviceThread.isAlive()) {
            checkPhotosOnDeviceThread.interrupt();
        }
        checkPhotosOnDeviceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (Thread.currentThread().isInterrupted())
                        return;

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            SnapPets robot = SnappetsHelper.getInstance().getConnectedSnappet();
                            if (checkPhotosOnDeviceThread != null && SnappetsHelper.getInstance() != null && robot != null) {
                                if (checkPhotosOnDeviceThread != null && currentState == DOWNLOAD_STATE.IDLE) {
                                    SnappetsHelper.getInstance().readPhotoCount(robot, createSnappetCallback());
                                }
                            }
                        }
                    });
                    try {
                        Thread.sleep(CHECK_PHOTOS_SLEEP);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        });
        checkPhotosOnDeviceThread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


//    public void startDownloadImages() {
//        if (currentState != DOWNLOAD_STATE.TAKING_PICTURE) {
//            currentState = DOWNLOAD_STATE.DOWNLOADING_IMAGES;
//            SnappetsHelper.getInstance().readPhotoCount(SnappetsHelper.getInstance().getConnectedSnappet(), createSnappetCallback());
//        }
//    }

    public void startTakePicture() {
        if (currentState == DOWNLOAD_STATE.DOWNLOADING_IMAGES)
            stopDownloadPhotos();
        currentState = DOWNLOAD_STATE.TAKING_PICTURE;
        SnappetsHelper.getInstance().takePicture(SnappetsHelper.getInstance().getConnectedSnappet(), createSnappetCallback());
    }

    private void stopDownloadPhotos() {
        if (SnappetsHelper.getInstance().getConnectedSnappet() != null)
            SnappetsHelper.getInstance().getConnectedSnappet().stopTransfer();
        try {
            Thread.sleep(1000);
            currentState = DOWNLOAD_STATE.IDLE;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private CameraInstance.Callback createCameraCallback() {
        return new CameraInstance.Callback() {
            @Override
            public void init() {
                switch (currentState) {
                    case IDLE:
                        if (idleCameraCallback != null)
                            idleCameraCallback.init();
                        break;
                    case DOWNLOADING_IMAGES:
                        if (imagesDownloadingCameraCallback != null)
                            imagesDownloadingCameraCallback.init();
                        break;
                    case TAKING_PICTURE:
                        if (takingPhotoCameraCallback != null)
                            takingPhotoCameraCallback.init();
                        break;
                }
            }

            @Override
            public void beforeTakePicture() {
                switch (currentState) {
                    case IDLE:
                        if (idleCameraCallback != null)
                            idleCameraCallback.beforeTakePicture();
                        break;
                    case DOWNLOADING_IMAGES:
                        if (imagesDownloadingCameraCallback != null)
                            imagesDownloadingCameraCallback.beforeTakePicture();
                        break;
                    case TAKING_PICTURE:
                        if (takingPhotoCameraCallback != null)
                            takingPhotoCameraCallback.beforeTakePicture();
                        break;
                }

            }

            @Override
            public void afterTakePicture(File file) {
                switch (currentState) {
                    case IDLE:
                        if (idleCameraCallback != null)
                            idleCameraCallback.afterTakePicture(file);
                        break;
                    case DOWNLOADING_IMAGES:
                        if (imagesDownloadingCameraCallback != null)
                            imagesDownloadingCameraCallback.afterTakePicture(file);
                        break;
                    case TAKING_PICTURE:
                        if (takingPhotoCameraCallback != null)
                            takingPhotoCameraCallback.afterTakePicture(file);
                        if (imagesDownloadingCameraCallback != null)
                            imagesDownloadingCameraCallback.afterTakePicture(file);
                        currentState = DOWNLOAD_STATE.IDLE;
                        break;
                }
            }
        };
    }

    private ConnectSnappetCallback createSnappetCallback() {
        return new ConnectSnappetCallback() {
            public int currentPhotoID;
            public int picturesTotalCount;
            public int picturesCount;
            private int imageTotalSize = 0;
            private int imageBufferSize = 0;

            @Override
            public void receivedImagePieceSize(int size) {
                switch (currentState) {
                    case IDLE:
                        if (idleCallback != null)
                            idleCallback.receivedImagePieceSize(size);
                        break;
                    case DOWNLOADING_IMAGES:
                        if (photoDownloadingCallback != null)
                            photoDownloadingCallback.receivedImagePieceSize(size);
                        break;
                    case TAKING_PICTURE:
                        if (photoTakingCallback != null)
                            photoTakingCallback.receivedImagePieceSize(size);
                }

                imageBufferSize += size;

                if (statisticListener != null && imageTotalSize != 0)
                    statisticListener.downloadedPictureProgress(picturesCount, picturesTotalCount, imageBufferSize, imageTotalSize);

                if (currentState == DOWNLOAD_STATE.DOWNLOADING_IMAGES)
                    if (imageBufferSize == imageTotalSize) {
                        SnappetsHelper.getInstance().getConnectedSnappet().deletePhotoID(currentPhotoID);
                    }
            }

            @Override
            public void receivedImageBuffer(byte[] buffer) {
                switch (currentState) {
                    case IDLE:
                        if (idleCallback != null)
                            idleCallback.receivedImageBuffer(buffer);
                        break;
                    case DOWNLOADING_IMAGES:
                        if (photoDownloadingCallback != null)
                            photoDownloadingCallback.receivedImageBuffer(buffer);
                        break;
                    case TAKING_PICTURE:
                        if (photoTakingCallback != null)
                            photoTakingCallback.receivedImageBuffer(buffer);
                }

                CameraInstance.getInstance().takePictureFromSnappet(createCameraCallback(), buffer);
            }

            @Override
            public void receiveImageTotalSize(int size) {
                switch (currentState) {
                    case IDLE:
                        if (idleCallback != null)
                            idleCallback.receiveImageTotalSize(size);
                        break;
                    case DOWNLOADING_IMAGES:
                        if (photoDownloadingCallback != null)
                            photoDownloadingCallback.receiveImageTotalSize(size);
                        break;
                    case TAKING_PICTURE:
                        if (photoTakingCallback != null)
                            photoTakingCallback.receiveImageTotalSize(size);
                }
                imageTotalSize = size;
                imageBufferSize = 0;
            }

            @Override
            public void disconnected(SnapPets snapPet) {
                switch (currentState) {
                    case IDLE:
                        if (idleCallback != null)
                            idleCallback.disconnected(snapPet);
                        break;
                    case DOWNLOADING_IMAGES:
                        if (photoDownloadingCallback != null)
                            photoDownloadingCallback.disconnected(snapPet);
                        break;
                    case TAKING_PICTURE:
                        if (photoTakingCallback != null)
                            photoTakingCallback.disconnected(snapPet);
                }
                currentState = DOWNLOAD_STATE.IDLE;
            }

            @Override
            public void connected(SnapPets snapPet) {
                switch (currentState) {
                    case IDLE:
                        if (idleCallback != null)
                            idleCallback.connected(snapPet);
                        break;
                    case DOWNLOADING_IMAGES:
                        if (photoDownloadingCallback != null)
                            photoDownloadingCallback.connected(snapPet);
                        break;
                    case TAKING_PICTURE:
                        if (photoTakingCallback != null)
                            photoTakingCallback.connected(snapPet);
                }
            }

            @Override
            public void didPressedButton() {
                switch (currentState) {
                    case IDLE:
                        if (idleCallback != null)
                            idleCallback.didPressedButton();
                        break;
                    case DOWNLOADING_IMAGES:
                        if (photoDownloadingCallback != null)
                            photoDownloadingCallback.didPressedButton();
                        break;
                    case TAKING_PICTURE:
                        if (photoTakingCallback != null)
                            photoTakingCallback.didPressedButton();
                }
            }

            @Override
            public void receivedPhotoCount(final int count) {
                switch (currentState) {
                    case IDLE:
                        if (idleCallback != null)
                            idleCallback.receivedPhotoCount(count);
                        break;
                    case DOWNLOADING_IMAGES:
                        if (photoDownloadingCallback != null)
                            photoDownloadingCallback.receivedPhotoCount(count);
                        break;
                    case TAKING_PICTURE:
                        if (photoTakingCallback != null)
                            photoTakingCallback.receivedPhotoCount(count);
                }
                if (count == 0) {
                    if (currentState == DOWNLOAD_STATE.DOWNLOADING_IMAGES) {
                        currentState = DOWNLOAD_STATE.IDLE;
                    }
                    return;
                }
                if (currentState == DOWNLOAD_STATE.IDLE) {
                    currentState = DOWNLOAD_STATE.DOWNLOADING_IMAGES;
                }
                if (currentState == DOWNLOAD_STATE.DOWNLOADING_IMAGES) {
                    picturesTotalCount = count;
                    picturesCount = 0;
                    currentPhotoID = 1;
                    startDownloadPhoto();
                }
            }

            private void startDownloadPhoto() {
                picturesCount++;
                SnappetsHelper.getInstance().getConnectedSnappet().getPhotoID(currentPhotoID);
            }

            @Override
            public void didDeletePhoto(int id) {
                switch (currentState) {
                    case IDLE:
                        if (idleCallback != null)
                            idleCallback.didDeletePhoto(id);
                        break;
                    case DOWNLOADING_IMAGES:
                        if (photoDownloadingCallback != null)
                            photoDownloadingCallback.didDeletePhoto(id);
                        break;
                    case TAKING_PICTURE:
                        if (photoTakingCallback != null)
                            photoTakingCallback.didDeletePhoto(id);
                }

                if (currentState == DOWNLOAD_STATE.DOWNLOADING_IMAGES) {
                    if (picturesCount != picturesTotalCount) {
                        startDownloadPhoto();

                    } else {
                        currentState = DOWNLOAD_STATE.IDLE;
                        if (statisticListener != null) {
                            statisticListener.finishDownload();
                        }
                    }
                } else if (currentState == DOWNLOAD_STATE.TAKING_PICTURE) {
                    currentState = DOWNLOAD_STATE.IDLE;
                }
            }
        };
    }

    public void setStatisticListener(IDownloadImageStatisticListener iDownloadImageStatisticListener) {
        statisticListener = iDownloadImageStatisticListener;
    }

    public void setPhotoDownloadingCallback(ConnectSnappetCallback photoDownloadingCallback) {
        this.photoDownloadingCallback = photoDownloadingCallback;
    }

    public void setPhotoTakingCallback(ConnectSnappetCallback photoTakingCallback) {
        this.photoTakingCallback = photoTakingCallback;
    }

    public void setIdleCallback(ConnectSnappetCallback idleCallback) {
        this.idleCallback = idleCallback;
    }

    public void setImagesDownloadingCameraCallback(CameraInstance.Callback imagesDownloadingCameraCallback) {
        this.imagesDownloadingCameraCallback = imagesDownloadingCameraCallback;
    }

    public void setTakingPhotoCameraCallback(CameraInstance.Callback takingPhotoCameraCallback) {
        this.takingPhotoCameraCallback = takingPhotoCameraCallback;
    }

    public void setIdleCameraCallback(CameraInstance.Callback idleCameraCallback) {
        this.idleCameraCallback = idleCameraCallback;
    }

    public class LocalBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }
}
