package com.wowwee.snappetssampleproject.fragments;

import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wowwee.bluetoothrobotcontrollib.snappets.SnapPets;
import com.wowwee.snappetssampleproject.R;
import com.wowwee.snappetssampleproject.SnappetsApplication;
import com.wowwee.snappetssampleproject.adapters.GaleryAdapter;
import com.wowwee.snappetssampleproject.interfaces.IDownloadImageStatisticListener;
import com.wowwee.snappetssampleproject.services.DownloadService;
import com.wowwee.snappetssampleproject.snappethelper.ConnectSnappetCallback;
import com.wowwee.snappetssampleproject.ui.CircleView;
import com.wowwee.snappetssampleproject.util.CameraInstance;
import com.wowwee.snappetssampleproject.util.DialogUtil;
import com.wowwee.snappetssampleproject.util.DialogUtil.Callback;
import com.wowwee.snappetssampleproject.util.DownloadManager;
import com.wowwee.snappetssampleproject.util.FragmentHelper;
import com.wowwee.snappetssampleproject.util.ShareUtil;
import com.wowwee.snappetssampleproject.util.SnappetsScreenReference;
import com.wowwee.snappetssampleproject.util.ViewHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GalleryFragment extends BaseFragment implements DownloadManager.Callback, IDownloadImageStatisticListener {

    GaleryAdapter galleryAdapter;
    GridView galleryGridView;
    private List<File> galleryFiles;
    private boolean isMultipleSelect = false;
    private List<File> selectedFiles = new ArrayList<>();
    private View toolbarLayout;
    private OnClickListener rightButtonOnClickListener;
    private TextView downloadedPicturesCount;
    private CircleView circlePictureLoading;
    private View downloadedPictures;
    private DownloadService mService;

    public GalleryFragment() {
        initializeFragment(R.layout.fragment_gallery, new SnappetsScreenReference());
    }

    @Override
    public void _onCreateView(final View view) {
        changeHeaderHeight(view);
        setupBackButton(view);
        setupToolbarLayout(view);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setupGallery(view);
            }
        }, 500);
        setupSelectButton(view);
        setupDeleteButton(view);
        setupShareButton(view);

        setupDowloadPhotosViews(view);
    }

    private void changeHeaderHeight(View view) {
        RelativeLayout header = (RelativeLayout) view.findViewById(R.id.headerLayout);
        RelativeLayout.LayoutParams headerLayoutParams = (RelativeLayout.LayoutParams) header.getLayoutParams();
        headerLayoutParams.height = ((getResources().getDisplayMetrics().heightPixels - ViewHelper.navBarHeight(getActivity())) - getResources().getDisplayMetrics().widthPixels) / 2;
        header.setLayoutParams(headerLayoutParams);
    }

    @Override
    public void onStart() {
        super.onStart();
        ConnectSnappetCallback snappetCallback = createSnappetCallback();
        CameraInstance.Callback cameraCallback = crateCameraCallback();
        mService = SnappetsApplication.getApplicationClass(getActivity()).getDownloadService();
        mService.setStatisticListener(GalleryFragment.this);
        mService.setPhotoDownloadingCallback(snappetCallback);
        mService.setImagesDownloadingCameraCallback(cameraCallback);

        //mService.setBindedStatisticListner(SnappetsApplication.getMockStatistick(getActivity()));
    }

    @Override
    public void onStop() {
        mService.setPhotoDownloadingCallback(null);
        mService.setPhotoDownloadingCallback(null);
        mService.setStatisticListener(null);
        super.onStop();
    }

    private void setupDowloadPhotosViews(View view) {
        downloadedPictures = view.findViewById(R.id.downloadedPictures);
        downloadedPicturesCount = (TextView) view.findViewById(R.id.downloadedCountTV);
        circlePictureLoading = (CircleView) view.findViewById(R.id.circleLoading);
    }

    private CameraInstance.Callback crateCameraCallback() {
        return new CameraInstance.Callback() {

            @Override
            public void init() {

            }

            @Override
            public void beforeTakePicture() {
            }

            @Override
            public void afterTakePicture(File file) {
                if (file != null) {
                    if (galleryFiles != null) {
                        galleryFiles.add(file);
                        sortList(galleryFiles);
                        if (galleryAdapter != null)
                            galleryAdapter.notifyDataSetChanged();
                    }
                }
            }
        };
    }

    private ConnectSnappetCallback createSnappetCallback() {
        return new ConnectSnappetCallback() {
//            public int currentPhotoID;
//            public int picturesTotalCount;
//            public int pictruesCount;
//            private int imageTotalSize = 0;
//            private int imageBufferSize = 0;

            @Override
            public void receivedImagePieceSize(int size) {
                ViewHelper.showViewWithAnimation(getActivity(), downloadedPictures);

            }

            @Override
            public void receivedImageBuffer(byte[] buffer) {
                // CameraInstance.getInstance().takePictureFromSnappet(cameraCallback, buffer);
            }

            @Override
            public void receiveImageTotalSize(int size) {
//                imageTotalSize = size;
//                imageBufferSize = 0;
            }

            @Override
            public void disconnected(SnapPets snapPet) {
                Log.d("TAG", "disconnected");
            }

            @Override
            public void connected(SnapPets snapPet) {
                Log.d("TAG", "connected");
            }

            @Override
            public void didPressedButton() {
                Log.d("TAG", "didPressButton");
            }

            @Override
            public void receivedPhotoCount(final int count) {
//
            }

            //
            @Override
            public void didDeletePhoto(int id) {
            }
        };

    }

    private void setupShareButton(View view) {

        View shareButton = view.findViewById(R.id.btn_share);
        ViewHelper.setButtonTouchListener(shareButton);
        shareButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                List<File> imageFiles = new ArrayList<>();

                for (File selectedPhoto : selectedFiles) {
                    imageFiles.add(selectedPhoto);
                }

//                ShareUtil.shareTextWithMultipleImages(context, getString(R.string.share_msg), imageFiles);
                ShareUtil.shareTextWithMultipleImages(context, "", imageFiles);
            }
        });

    }

    private void setupDeleteButton(View view) {
        View deleteButton = view.findViewById(R.id.btn_delete);
        ViewHelper.setButtonTouchListener(deleteButton);
        deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtil.showYesNoDialog(
                        context,
                        getString(R.string.photos_will_be_deleted_on_snappet_and_app),
                        getString(R.string.yes),
                        getString(R.string.no),
                        new Callback() {
                            @Override
                            public void callback(DialogInterface dialog) {
                                for (File file : selectedFiles) {
                                    file.delete();
                                }

                                //reset the selection
                                if (isMultipleSelect) {
                                    rightButtonOnClickListener.onClick(null);
                                }

                                //reload the gallery
                                galleryFiles.clear();
                                selectedFiles.clear();
                                galleryFiles = CameraInstance.getInstance().getPictures();
                                sortList(galleryFiles);
                                galleryAdapter.setGalleryFiles(galleryFiles);
                                galleryAdapter.notifyDataSetChanged();
                            }
                        },
                        null);
            }
        });
    }

    private void setupSelectButton(View view) {
        final TextView rightButton = (TextView) view.findViewById(R.id.btn_right);
        rightButtonOnClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                isMultipleSelect = !isMultipleSelect;

                rightButton.setText(isMultipleSelect ? getString(R.string.cancel) : getString(R.string.select));
                //rightButton.setDimmableBackground(isMultipleSelect ? R.drawable.btn_close : R.drawable.btn_selectall);

                if (!isMultipleSelect) {
                    //hidden the toolbar
                    ViewHelper.hideViewWithAnimation(getActivity(), toolbarLayout);

                    selectedFiles.clear();
                    galleryAdapter.notifyDataSetChanged();
                }
            }
        };

        isMultipleSelect = false;
        rightButton.setText(isMultipleSelect ? getString(R.string.cancel) : getString(R.string.select));
        //rightButton.setDimmableBackground(isMultipleSelect ? R.drawable.btn_close : R.drawable.btn_selectall);
        ViewHelper.setButtonTouchListener(rightButton, getResources().getColor(R.color.white_color));
        rightButton.setOnClickListener(rightButtonOnClickListener);

    }

    private void setupGallery(View view) {
        if (galleryFiles != null)
            galleryFiles.clear();
        galleryFiles = CameraInstance.getInstance().getPictures();
        sortList(galleryFiles);

//        sortList(galleryFiles);

        galleryAdapter = new GaleryAdapter(getActivity(), galleryFiles, selectedFiles,
                new android.view.View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        File photo = (File) v.getTag();

                        if (isMultipleSelect) {
                            boolean isSelected = false;

                            for (File selectedPhoto : selectedFiles) {
                                if (selectedPhoto == photo) {
                                    isSelected = true;
                                }
                            }

                            if (isSelected) {
                                //remove the selection
                                selectedFiles.remove(photo);

                                //hidden the toolbar if need
                                if (selectedFiles.size() <= 0) {
                                    ViewHelper.hideViewWithAnimation(getActivity(), toolbarLayout);
                                }
                            } else {
                                //mark it as selection
                                selectedFiles.add(photo);

                                //show the toolbar
                                ViewHelper.showViewWithAnimation(getActivity(), toolbarLayout);
                            }
                            galleryAdapter.notifyDataSetChanged();
                        } else {
                            //switch to sticker page
                            Object tag = v.getTag();
                            if (tag != null && tag instanceof File) {
                                StickerFragment fragment = new StickerFragment();
                                fragment.setImageFile((File) tag);
                                FragmentHelper.switchFragment(getFragmentManager(), fragment, R.id.layout_core, true);
                            } else {
                                Toast.makeText(context, "File is missing on v.getTag()", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                });

        galleryGridView = (GridView) view.findViewById(R.id.grid_gallery);
        galleryGridView.setAdapter(galleryAdapter);

    }

    private void sortList(List<File> galleryFiles) {
        Collections.sort(galleryFiles, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                if (lhs.lastModified() < rhs.lastModified()) return 1;
                if (lhs.lastModified() > rhs.lastModified()) return -1;
                return 0;
            }
        });
    }

    private void setupToolbarLayout(View view) {
        toolbarLayout = view.findViewById(R.id.layout_gallery_tool);
        ViewHelper.hideViewWithAnimation(getActivity(), toolbarLayout);
    }

    private void setupBackButton(View view) {
        View backButton = view.findViewById(R.id.btn_back);
        ViewHelper.setButtonTouchListener(backButton, getResources().getColor(R.color.grey_color));
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DownloadManager.GetInstance().IsDownloading())
                    DownloadManager.GetInstance().Stop();
                FragmentHelper.popBackStack(getFragmentManager());
            }
        });
    }

    @Override
    public void notifyPhotoDownloaded(final File file) {
    }

    @Override
    public void startedPhotoDownload() {
    }

    @Override
    public void finishedPhotoDownload() {
    }

    @Override
    public void finishDownload() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ViewHelper.hideViewWithAnimation(getActivity(), downloadedPictures);
            }
        });
    }

    @Override
    public void downloadedPictureProgress(final int pictruesCount, final int picturesTotalCount, final int imageBufferSize, final int imageTotalSize) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                downloadedPicturesCount.setText(String.valueOf(pictruesCount) + "/" + String.valueOf(picturesTotalCount));
                circlePictureLoading.updateCircle(imageBufferSize, imageTotalSize);
            }
        });
    }
}
