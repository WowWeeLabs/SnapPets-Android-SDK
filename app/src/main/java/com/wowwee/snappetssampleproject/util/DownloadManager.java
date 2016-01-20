package com.wowwee.snappetssampleproject.util;

import android.util.Log;

import com.wowwee.bluetoothrobotcontrollib.snappets.SnapPets;
import com.wowwee.snappetssampleproject.enums.DownloadStatus;
import com.wowwee.snappetssampleproject.fragments.CameraFragment;
import com.wowwee.snappetssampleproject.snappethelper.ConnectSnappetCallback;
import com.wowwee.snappetssampleproject.snappethelper.SnappetsHelper;

import java.io.File;

public class DownloadManager {
    static private DownloadManager s_instance = null;

    ;
    ConnectSnappetCallback snappetCallback = null;
    private DownloadStatus downloaderStatus = DownloadStatus.PhotoDownloaderStatusIdle;
    private Callback m_callback = null;

    protected DownloadManager() {
        this.downloaderStatus = DownloadStatus.PhotoDownloaderStatusIdle;
    }

    static public DownloadManager GetInstance() {
        if (s_instance == null) {
            s_instance = new DownloadManager();
        }
        return s_instance;
    }

    public void SetCallback(Callback acallback) {
        this.m_callback = acallback;
    }

    public void DownloadAllPhotos() {

        final CameraInstance.Callback cameraCallback = new CameraInstance.Callback() {

            @Override
            public void init() {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTakePicture() {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTakePicture(final File file) {
                // TODO Auto-generated method stub
                Thread newThread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {

                            e.printStackTrace();
                        }
                        DownloadManager.this.m_callback.notifyPhotoDownloaded(file);
                        SnappetsHelper.getInstance().getConnectedSnappet().deletePhotoID(1);
                    }
                });
                newThread.start();
            }
        };

        snappetCallback = new ConnectSnappetCallback() {
            private int imageTotalSize = 0;
            private int imageBufferSize = 0;

            @Override
            public void receivedImagePieceSize(int size) {
//				Log.d(CameraFragment.class.toString(), "receivedImagePieceSize: "+size);

                imageBufferSize += size;

//				handler.updateLoadingProgress(imageBufferSize*100/imageTotalSize);
            }

            @Override
            public void receivedImageBuffer(byte[] buffer) {
                Log.i(CameraFragment.class.toString(), "receivedImageBuffer: " + buffer.length);
                CameraInstance.getInstance().takePictureFromSnappet(cameraCallback, buffer);
//				takePictureFromSnappet(buffer);
            }

            @Override
            public void receiveImageTotalSize(int size) {
//				Log.d(CameraFragment.class.toString(), "receiveImageTotalSize: "+size);

                imageTotalSize = size;
                imageBufferSize = 0;
            }

            @Override
            public void disconnected(SnapPets snapPet) {
            }

            @Override
            public void connected(SnapPets snapPet) {
                if (downloaderStatus == DownloadStatus.PhotoDownloaderStatusDownloading)
                    SnappetsHelper.getInstance().readPhotoCount(SnappetsHelper.getInstance().getConnectedSnappet(), snappetCallback);
            }

            @Override
            public void didPressedButton() {
                Log.d(CameraFragment.class.toString(), "didPressedButton");

//				handler.pressTakePicture();
            }

            @Override
            public void receivedPhotoCount(final int count) {
                // TODO Auto-generated method stub
                Log.i(CameraFragment.class.toString(), "receivedPhotoCount: " + count);
                if (count > 0) {
                    if (downloaderStatus == DownloadStatus.PhotoDownloaderStatusDownloading) {
                        Thread newThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < count; i++) {
                                    Log.i(CameraFragment.class.toString(), "getPhoto");
                                    SnappetsHelper.getInstance().getConnectedSnappet().getPhotoID(1);
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                        newThread.start();
                    } else
                        downloaderStatus = DownloadStatus.PhotoDownloaderStatusIdle;
//		            [PhotoLibraryManager sharedInstance].currentPhotoId = 0;
//		            uint8_t photoIdByte = (uint8_t)([PhotoLibraryManager sharedInstance].currentPhotoId+1);
//		            [[ConnectionManager sharedInstance].snappet snappetGetPhoto:kSnappetPhotoLargeSize photoID:photoIdByte];
                } else {
//		            if (self.delegate != nil) {
//		                [self.delegate didCompleteDownloadTask];
//		            }

                    DownloadManager.this.downloaderStatus = DownloadStatus.PhotoDownloaderStatusIdle;
                    DownloadManager.this.m_callback.finishedPhotoDownload();
                }
            }

            @Override
            public void didDeletePhoto(int id) {
                // TODO Auto-generated method stub
                Log.i(CameraFragment.class.toString(), "didDeletePhoto");

                Thread newThread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {

                            e.printStackTrace();
                        }
                        if (downloaderStatus == DownloadStatus.PhotoDownloaderStatusDownloading)
                            SnappetsHelper.getInstance().readPhotoCount(SnappetsHelper.getInstance().getConnectedSnappet(), snappetCallback);
                        else
                            downloaderStatus = DownloadStatus.PhotoDownloaderStatusIdle;
                    }

                });
                newThread.start();
            }
        };


        SnappetsHelper.getInstance().readPhotoCount(SnappetsHelper.getInstance().getConnectedSnappet(), snappetCallback);
        this.downloaderStatus = DownloadStatus.PhotoDownloaderStatusDownloading;
        DownloadManager.this.m_callback.startedPhotoDownload();
    }

    public void Stop() {
        if (SnappetsHelper.getInstance().getConnectedSnappet() != null)
            SnappetsHelper.getInstance().getConnectedSnappet().stopTransfer();
        this.downloaderStatus = DownloadStatus.PhotoDownloaderStatusStopped;
        DownloadManager.this.m_callback.finishedPhotoDownload();
    }

    public boolean IsDownloading() {
        return this.downloaderStatus == DownloadStatus.PhotoDownloaderStatusDownloading;
    }

    public interface Callback {
        void startedPhotoDownload();

        void notifyPhotoDownloaded(File file);

        void finishedPhotoDownload();
    }
}
