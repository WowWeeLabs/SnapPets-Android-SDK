package com.wowwee.snappetssampleproject.fragments;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wowwee.bluetoothrobotcontrollib.snappets.SnapPets;
import com.wowwee.snappetssampleproject.R;
import com.wowwee.snappetssampleproject.SnappetsApplication;
import com.wowwee.snappetssampleproject.enums.CAMERA_FLASH;
import com.wowwee.snappetssampleproject.enums.CAMERA_MODE;
import com.wowwee.snappetssampleproject.enums.CAMERA_STATUS;
import com.wowwee.snappetssampleproject.enums.CAMERA_TIMER;
import com.wowwee.snappetssampleproject.enums.PICTURE_EVERY_TIMER;
import com.wowwee.snappetssampleproject.fragments.popups.CheckPinPopup;
import com.wowwee.snappetssampleproject.fragments.popups.SearchPopup;
import com.wowwee.snappetssampleproject.fragments.popups.SetupNamePopup;
import com.wowwee.snappetssampleproject.interfaces.IDownloadImageStatisticListener;
import com.wowwee.snappetssampleproject.services.DownloadService;
import com.wowwee.snappetssampleproject.snappethelper.AuthenticateCallback;
import com.wowwee.snappetssampleproject.snappethelper.ConnectSnappetCallback;
import com.wowwee.snappetssampleproject.snappethelper.SnappetsHelper;
import com.wowwee.snappetssampleproject.ui.CameraPreview;
import com.wowwee.snappetssampleproject.util.CameraInstance;
import com.wowwee.snappetssampleproject.util.ConnectionManager;
import com.wowwee.snappetssampleproject.util.DialogUtil;
import com.wowwee.snappetssampleproject.util.DialogUtil.Callback;
import com.wowwee.snappetssampleproject.util.DrawableUtil;
import com.wowwee.snappetssampleproject.util.FragmentHelper;
import com.wowwee.snappetssampleproject.util.ShareUtil;
import com.wowwee.snappetssampleproject.util.SharedSettingsHelper;
import com.wowwee.snappetssampleproject.util.SnappetsScreenReference;
import com.wowwee.snappetssampleproject.util.ViewHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraFragment extends BaseFragment implements View.OnClickListener {

    public static CAMERA_MODE cameraMode = CAMERA_MODE.PHONE;
    public static boolean isSearchingSnappets = false;
    private static boolean firstStartOfFragment = true;
    private final int EXPECTED_PHOTO_WIDTH = 640;
    private final int EXPECTED_PHTOT_HEIGHT = 480;

    public CameraInstance.Callback cameraCallback;
    public Handler handler;
    private CameraInstance cameraInstance;
    private CameraPreview mPreview;

    private CAMERA_FLASH cameraFlash = CAMERA_FLASH.DISABLE;

    private CAMERA_TIMER cameraTimer = CAMERA_TIMER.OFF;

    private CountDownTimer countDownTimer;
    private Button snappetButton;
    private CAMERA_STATUS cameraStatus = CAMERA_STATUS.READY;

    private View settingButton;
    private TextView countdownText;
    private View cameraToolLayout;
    private Button switchFlashButton;
    private ImageView previewImage;
    private Button switchCameraButton;
    private View imageToolLayout;
    private Button switchTimerButton;
    private View shareButton;
    private View editButton;
    private View deleteButton;
    private View loadingLayout;
    private ProgressBar loadingProgress;
    private View cameraControlLayout;
    private ViewGroup switchTimerLayout;
    private PICTURE_EVERY_TIMER pictureEveryTimer;
    private Button everyTimerButton;
    private ViewGroup everyTimerLayout;
    private View snapPhoneLayout;
    private TextView countdownSnapText;
    private View saveButton;
    private DownloadService downloadService;
    private Button captureButton;

    public CameraFragment() {
        initializeFragment(R.layout.fragment_camera, new SnappetsScreenReference());
    }

    @Override
    public void _onCreateView(final View view) {

        createCameraInstance();
        changeHeaderHeight(view);
        setupSettingsButton(view);
        setupSimpleViews(view);
        setupImageActionButtons(view);
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                createCameraPreview(view);
            }
        }, 500);

        setupSwitchFlashButton(view);
        setupSwitchCameraButton(view);
        setupSwithTimerLayout(view);
        setupPictureEveryTimeLayout(view);

        setupCameraCallback();

        setupHandler();


        setupCaptureButton(view);
        setupCameraCancelButton(view);
        setupGalleryButton(view);
        setupSnappetButton(view);

        cameraCallback.init();
    }

    private void changeHeaderHeight(View view) {
        RelativeLayout header = (RelativeLayout) view.findViewById(R.id.camera_fragment_header);
        LinearLayout.LayoutParams headerLayoutParams = (LinearLayout.LayoutParams) header.getLayoutParams();
        headerLayoutParams.height = ((getResources().getDisplayMetrics().heightPixels - ViewHelper.navBarHeight(getActivity())) - getResources().getDisplayMetrics().widthPixels) / 2;
        header.setLayoutParams(headerLayoutParams);
    }

    @Override
    public void onStart() {
        super.onStart();

        Activity activity = getActivity();
        if (activity != null) {
            downloadService = SnappetsApplication.getApplicationClass(activity).getDownloadService();
            if (downloadService != null) {
                downloadService.setPhotoTakingCallback(createTakePhotoCallback());
                downloadService.setIdleCallback(getChecksnappetPressButtonCallback());
                downloadService.setPhotoDownloadingCallback(getChecksnappetPressButtonCallback());
                downloadService.setIdleCameraCallback(cameraCallback);
                downloadService.setTakingPhotoCameraCallback(cameraCallback);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (downloadService != null) {
            downloadService.setPhotoTakingCallback(null);
            downloadService.setIdleCallback(null);
            downloadService.setStatisticListener(null);
            downloadService.setPhotoDownloadingCallback(null);
            downloadService.setIdleCameraCallback(null);
            downloadService.setTakingPhotoCameraCallback(null);
        }
    }

    private void setupPictureEveryTimeLayout(View view) {
        everyTimerButton = (Button) view.findViewById(R.id.btn_pic_every_time);
        everyTimerButton.setVisibility(View.GONE);
        pictureEveryTimer = PICTURE_EVERY_TIMER.OFF;

        ViewHelper.setButtonTouchListener(everyTimerButton);
        everyTimerButton.setEnabled(false);
        everyTimerButton.setOnClickListener(this);

        view.findViewById(R.id.sec30).setOnClickListener(this);
        view.findViewById(R.id.min2).setOnClickListener(this);
        view.findViewById(R.id.min5).setOnClickListener(this);
        view.findViewById(R.id.close_every_time_bar).setOnClickListener(this);
        ViewHelper.setButtonTouchListener(view.findViewById(R.id.sec30));
        ViewHelper.setButtonTouchListener(view.findViewById(R.id.min2));
        ViewHelper.setButtonTouchListener(view.findViewById(R.id.min5));
        ViewHelper.setButtonTouchListener(view.findViewById(R.id.close_every_time_bar));
        everyTimerLayout = (ViewGroup) view.findViewById(R.id.layout_every_time_tool);
    }

    private void setupImageActionButtons(View view) {
        shareButton = view.findViewById(R.id.btn_share);
        editButton = view.findViewById(R.id.btn_edit);
        deleteButton = view.findViewById(R.id.btn_delete);
        saveButton = view.findViewById(R.id.btn_save);
    }

    private void setupGalleryButton(View view) {
        //handle the gallery button
        View galleryButton = view.findViewById(R.id.btn_photoalbum);
        galleryButton.setOnClickListener(this);
        int padding = getSideButtonPadding();
        galleryButton.setPadding(padding, padding, padding, padding);
    }

    private void setupSimpleViews(View view) {
        countdownText = (TextView) view.findViewById(R.id.txt_countdown);
        countdownSnapText = (TextView) view.findViewById(R.id.txt_countdown_snap);
        cameraToolLayout = view.findViewById(R.id.layout_camera_tool);
        previewImage = (ImageView) view.findViewById(R.id.img_camera_preview);
        imageToolLayout = view.findViewById(R.id.layout_image_tool);
        loadingLayout = view.findViewById(R.id.layout_loading);
        loadingProgress = (ProgressBar) view.findViewById(R.id.progress_loading);
        cameraControlLayout = view.findViewById(R.id.layout_camera_control);
        snapPhoneLayout = view.findViewById(R.id.snapPhoneLayout);
    }

    private ConnectSnappetCallback createTakePhotoCallback() {
        return new ConnectSnappetCallback() {
            @Override
            public void receivedImagePieceSize(int size) {
                //handler.updateLoadingProgress(imageBufferSize * 100 / imageTotalSize);
            }

            @Override
            public void receivedImageBuffer(byte[] buffer) {
                //takePictureFromSnappet(buffer);
            }

            @Override
            public void receiveImageTotalSize(int size) {
            }

            @Override
            public void disconnected(SnapPets snapPet) {
            }

            @Override
            public void connected(SnapPets snapPet) {
            }

            @Override
            public void didPressedButton() {
            }

            @Override
            public void receivedPhotoCount(int count) {
            }

            @Override
            public void didDeletePhoto(int id) {
            }
        };
    }


    @Override
    public void onResume() {
        Log.d(CameraFragment.class.toString(), "onResume");
        super.onResume();
        startUpdateCameraPreviewThread();
        cameraCallback.init();
        if (firstStartOfFragment) {
            firstStartOfFragment = false;
//            snappetButtonClicked();
        }
    }

    @Override
    public void onPause() {
        Log.d(CameraFragment.class.toString(), "onPause");
        SnappetsHelper.getInstance().stopSearch();
        super.onPause();
        releaseCamera();
    }

    @Override
    public boolean allowBackPress() {
        if (cameraStatus == CAMERA_STATUS.CONFIRM) {
            cameraCallback.init();
            return false;
        } else if (cameraStatus == CAMERA_STATUS.WORKING) {
            return false;
        }

        return true;
    }

    @Override
    public void onClick(View v) {
        if(isSearchingSnappets) {
            Log.i("","onClick is blocked because SearchPopup is shown");
            return;
        }
        if (v.getId() == R.id.btn_settings) {
            settingsButtonClicked();
        } else if (v == mPreview) {
            previewClicked();
        } else if (v.getId() == R.id.btn_flash) {
            switchFlashButtonClicked();
        } else if (v.getId() == R.id.btn_camswitch) {
            switchCameraButtonClicked();
        } else if (v.getId() == R.id.btn_back) {
            backButtonClicked();
        } else if (v.getId() == R.id.btn_snap) {
            snapButtonClicked();
        } else if (v.getId() == R.id.btn_cam_cancel) {
            cameraCancelButtonClicked();
        } else if (v.getId() == R.id.btn_photoalbum) {
            galleryButtonClicked();
        } else if (v.getId() == R.id.btn_cammode) {
            snappetButtonClicked();
        } else if (v.getId() == R.id.btn_timer) {
            switchTimerButtonClicked();
        } else if (v.getId() == R.id.sec3) {
            updateCameraTimer(CAMERA_TIMER.THREE_SECONDS);
        } else if (v.getId() == R.id.sec7) {
            updateCameraTimer(CAMERA_TIMER.SEVEN_SECONDS);
        } else if (v.getId() == R.id.sec10) {
            updateCameraTimer(CAMERA_TIMER.TEN_SECONDS);
        } else if (v.getId() == R.id.close_timer_bar) {
            updateCameraTimer(CAMERA_TIMER.OFF);
        } else if (v.getId() == R.id.btn_pic_every_time) {
            everyTimerButtonClicked();
        } else if (v.getId() == R.id.sec30) {
            updateEveryTimer(PICTURE_EVERY_TIMER.THIRTY_SECONDS);
        } else if (v.getId() == R.id.min2) {
            updateEveryTimer(PICTURE_EVERY_TIMER.TWO_MINUTES);
        } else if (v.getId() == R.id.min5) {
            updateEveryTimer(PICTURE_EVERY_TIMER.FIVE_MINUTES);
        } else if (v.getId() == R.id.close_every_time_bar) {
            updateEveryTimer(PICTURE_EVERY_TIMER.OFF);
        }
    }

    private void everyTimerButtonClicked() {
        ViewHelper.showViewWithAnimation(getActivity(), everyTimerLayout);
        ViewHelper.hideViewWithAnimation(getActivity(), cameraToolLayout);
        ViewHelper.hideViewWithAnimation(getActivity(), cameraControlLayout);
    }

    private void updateEveryTimer(PICTURE_EVERY_TIMER newTimer) {
        pictureEveryTimer = newTimer;
        updateEveryTimerLayout();
    }

    private void updateEveryTimerLayout() {
        ViewHelper.hideViewWithAnimation(getActivity(), everyTimerLayout);
        ViewHelper.showViewWithAnimation(getActivity(), cameraToolLayout);
        ViewHelper.showViewWithAnimation(getActivity(), cameraControlLayout);
    }

    private void updateCameraTimer(CAMERA_TIMER newTimer) {
        cameraTimer = newTimer;
        updateTimeSwitchLayout();
    }

    private void updateTimeSwitchLayout() {
        ViewHelper.hideViewWithAnimation(getActivity(), switchTimerLayout);
        ViewHelper.showViewWithAnimation(getActivity(), cameraToolLayout);
        ViewHelper.showViewWithAnimation(getActivity(), cameraControlLayout);
        if(cameraTimer.getIndex() == CAMERA_TIMER.THREE_SECONDS.index) {
            switchTimerButton.setText(getResources().getString(R.string.timer) + ": " + getResources().getString(R.string.sec3));
        } else if(cameraTimer.getIndex() == CAMERA_TIMER.SEVEN_SECONDS.index) {
            switchTimerButton.setText(getResources().getString(R.string.timer) + ": " + getResources().getString(R.string.sec7));
        } else if(cameraTimer.getIndex() == CAMERA_TIMER.TEN_SECONDS.index) {
            switchTimerButton.setText(getResources().getString(R.string.timer) + ": " + getResources().getString(R.string.sec10));
        } else {
            switchTimerButton.setText(getResources().getString(R.string.timer));
        }
    }

    private void releaseCamera() {
        if (mPreview != null)
            mPreview.disbleCallback();
        if (cameraInstance != null)
            cameraInstance.releaseCamera();              // release the camera immediately on pause event
    }

    private void startUpdateCameraPreviewThread() {
        Thread newThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cameraInstance.createCamera();
                        mPreview.updateCamera(cameraInstance.camera);
                        cameraCallback.init();
                    }
                });
            }
        });
        newThread.start();
    }

    private void setupSnappetButton(View view) {
        //show the search popup / switch to snappet mode
        snappetButton = (Button) view.findViewById(R.id.btn_cammode);
        snappetButton.setOnClickListener(this);
        int padding = getSideButtonPadding();
        snappetButton.setPadding(padding, padding, padding, padding);
    }

    private void snappetButtonClicked() {
        if (SnappetsHelper.getInstance().getConnectedSnappet() != null) {
            cameraMode = cameraMode.swap();
            cameraCallback.init();
        } else {
            //show the search popup
            SearchPopup fragment = new SearchPopup();
            fragment.setCameraCallback(cameraCallback);
            FragmentHelper.showWithoutSlideAnimation(getFragmentManager(), fragment, R.id.layout_popup, true);
        }
    }

    private void setupCaptureButton(View view) {
        // Add a listener to the Capture button
        captureButton = (Button) view.findViewById(R.id.btn_snap);
        captureButton.setOnClickListener(this);

        int padding = getCenterButtonPadding();
        captureButton.setPadding(padding, padding, padding, padding);
    }

    private int getCenterButtonPadding() {
        int padding = 0;
        padding = (getResources().getDisplayMetrics().heightPixels - getResources().getDisplayMetrics().widthPixels
                + (int) (getResources().getDisplayMetrics().ydpi - getResources().getDisplayMetrics().xdpi)) / 18;
        return padding;
    }

    private int getSideButtonPadding() {
        int padding = 0;
        padding = (getResources().getDisplayMetrics().heightPixels - getResources().getDisplayMetrics().widthPixels
                + (int) (getResources().getDisplayMetrics().ydpi - getResources().getDisplayMetrics().xdpi)) / 14;
        return padding;
    }

    private void setupCameraCancelButton(View view) {
        // Add a listener to the Capture button
        Button cameraCancelButton = (Button) view.findViewById(R.id.btn_cam_cancel);
        cameraCancelButton.setOnClickListener(this);

        int padding = getCenterButtonPadding();
        cameraCancelButton.setPadding(padding, padding, padding, padding);
    }

    private void setupHandler() {
        handler = new Handler() {
            @Override
            public boolean pressTakePicture() {
                if (cameraStatus == CAMERA_STATUS.CONFIRM) {
                    cameraCallback.init();
                    if (cameraMode == CAMERA_MODE.PHONE)
                        return false;
//                    return false;
                } else if (cameraStatus == CAMERA_STATUS.WORKING) {
                    return false;
                }

                //handler the timer if it is enabled
                if (cameraTimer != CAMERA_TIMER.OFF) {
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                        countDownTimer = null;
                    }

                    //hidden the camera tool
                    ViewHelper.showViewWithAnimation(getActivity(), cameraControlLayout.findViewById(R.id.btn_cam_cancel));
                    ViewHelper.invisibleViewWithAnimation(getActivity(), cameraToolLayout);

                    if (cameraMode == CAMERA_MODE.PHONE) {
                        //hidden any preview
                        ViewHelper.hideViewWithAnimation(getActivity(), previewImage);
                        previewImage.setImageResource(0);
                    } else if (cameraMode == CAMERA_MODE.SNAPPET) {
                        //show pure theme color
                        previewImage.setImageResource(0);
                    }

                    //show the count down
                    if (cameraMode == CAMERA_MODE.PHONE) {
                        ViewHelper.showViewWithAnimation(getActivity(), countdownText);
                        ViewHelper.invisibleViewWithAnimation(getActivity(), countdownSnapText);
                    } else {
                        ViewHelper.showViewWithAnimation(getActivity(), countdownSnapText);
                        ViewHelper.invisibleViewWithAnimation(getActivity(), countdownText);
                    }

                    countDownTimer = new CountDownTimer(cameraTimer.waitTime, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            countdownText.setText("" + millisUntilFinished / 1000);
                            countdownSnapText.setText("" + millisUntilFinished / 1000);
                        }

                        @Override
                        public void onFinish() {
                            //cancel the countdown
                            countDownTimer.cancel();
                            countDownTimer = null;

                            //show the camera tool
                            ViewHelper.showViewWithAnimation(getActivity(), cameraToolLayout);

                            //hidden the cancel button
                            ViewHelper.hideViewWithAnimation(getActivity(), cameraControlLayout.findViewById(R.id.btn_cam_cancel));

                            //hidden the count down
                            ViewHelper.invisibleViewWithAnimation(getActivity(), countdownText);
                            ViewHelper.invisibleViewWithAnimation(getActivity(), countdownSnapText);

                            countdownText.post(new Runnable() {

                                @Override
                                public void run() {
                                    doTakePicture();
                                }
                            });
                        }
                    };
                    countDownTimer.start();
                } else {
                    doTakePicture();
                }

                return true;
            }

            @Override
            public void updateLoadingProgress(int percentage) {
                loadingProgress.setProgress(percentage);
            }

            @Override
            public void setTitle(String name) {
            }
        };

    }

    private void setupCameraCallback() {
        cameraCallback = new CameraInstance.Callback() {
            @Override
            public void init() {
                final SnapPets snapPet = SnappetsHelper.getInstance().getConnectedSnappet();
                if (snapPet != null) {
                    setupLayoutWhenSnapPetDetected(snapPet);
                } else {
                    cameraMode = CAMERA_MODE.PHONE;
                }

                cameraStatus = CAMERA_STATUS.READY;
                if (cameraMode == CAMERA_MODE.PHONE) {
                    updateLayoutForPhoneCamera();
                } else if (cameraMode == CAMERA_MODE.SNAPPET) {
                    updateLayoutForSnappetCamera();
                }

                if (snapPet != null) {
                    ViewHelper.showViewWithAnimation(getActivity(), settingButton);
                    settingButton.setVisibility(View.VISIBLE);
                } else {
                    settingButton.setVisibility(View.GONE);
                }

                ViewHelper.invisibleViewWithAnimation(getActivity(), imageToolLayout);
                ViewHelper.hideViewWithAnimation(getActivity(), loadingLayout);
            }

            @Override
            public void beforeTakePicture() {
                cameraStatus = CAMERA_STATUS.WORKING;

                //camera tool
                ViewHelper.hideViewWithAnimation(getActivity(), cameraToolLayout);

                ViewHelper.invisibleViewWithAnimation(getActivity(), imageToolLayout);

                //loading layout
                if (cameraMode == CAMERA_MODE.PHONE) {
                    ViewHelper.hideViewWithAnimation(getActivity(), loadingLayout);
                } else if (cameraMode == CAMERA_MODE.SNAPPET) {
                    ViewHelper.showViewWithAnimation(getActivity(), loadingLayout);
                    captureButton.setEnabled(false);

                    //assign 0 into loading progress
                    handler.updateLoadingProgress(0);
                }
            }

            @Override
            public void afterTakePicture(final File file) {
                cameraStatus = CAMERA_STATUS.CONFIRM;

                ViewHelper.hideViewWithAnimation(getActivity(), cameraToolLayout);
                ViewHelper.hideViewWithAnimation(getActivity(), loadingLayout);
                captureButton.setEnabled(true);

                ViewHelper.showViewWithAnimation(getActivity(), previewImage);
                previewImage.setImageDrawable(DrawableUtil.getDrawableFromFile(context, file));

                ViewHelper.showViewWithAnimation(getActivity(), imageToolLayout);
                ViewHelper.hideViewWithAnimation(getActivity(), snapPhoneLayout);

                shareButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        ShareUtil.shareTextWithImage(context, getString(R.string.share_msg), file);
                        ShareUtil.shareTextWithImage(context, "", file);
                    }
                });
                ViewHelper.setButtonTouchListener(shareButton);

                editButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveCurrentPhoto(file);
//                        StickerFragment fragment = new StickerFragment();
//                        fragment.setImageFile(file);
//                        FragmentHelper.switchFragment(getFragmentManager(), fragment, R.id.layout_core, true);
                    }
                });
                ViewHelper.setButtonTouchListener(editButton);

                deleteButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO: read the text from Resource
                        DialogUtil.showYesNoDialog(
                                context,
                                getString(R.string.photos_will_be_deleted_on_snappet_and_app),
                                getString(R.string.yes),
                                getString(R.string.no),
                                new Callback() {
                                    @Override
                                    public void callback(DialogInterface dialog) {
                                        //delete the current image
                                        file.delete();

                                        cameraCallback.init();
                                    }
                                },
                                null);
                    }
                });
                ViewHelper.setButtonTouchListener(deleteButton);


                saveButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cameraCallback.init();
                    }
                });
                ViewHelper.setButtonTouchListener(saveButton);
            }
        };
    }

    private void setupLayoutWhenSnapPetDetected(final SnapPets snapPet) {
        if (SnappetsHelper.IS_ENABLE_PIN_CHECKING) {
            if (!SnappetsHelper.getInstance().isPinCodeChecked) {
                if (SnappetsHelper.getInstance().needTutorial(snapPet)) {
                    //TRY TO SAVE device name. need for checking if need tutorial
                    //TODO now this method sometimes not work
                    android.os.Handler handler = new android.os.Handler();

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            snapPet.writeModuleInfoUserDeviceName(BluetoothAdapter.getDefaultAdapter().getName());
                        }
                    }, 1000);

                    if (getView() != null)
                        getView().post(new Runnable() {
                            @Override
                            public void run() {
                                startSetPetNameFragment(snapPet);
                            }
                        });

                } else {
                    if (getView() != null)
                        getView().post(new Runnable() {
                            @Override
                            public void run() {
                                //If this is no first snap pet start - start if we need only pin dialog
                                //TODO implement checking need pin dialog start
                                if (!SnappetsHelper.getInstance().isPinCodeChecked) {
                                    startCheckPinDialog(snapPet);
                                }
                            }

                        });
                }
            }
        } else {
            SnappetsHelper.getInstance().isPinCodeChecked = true;
            SnappetsApplication.getApplicationClass(getActivity()).getDownloadService().startCheckNewPhotosThread();
            try {
                Thread.sleep(500);
            } catch (Exception ex) {

            }
        }
    }

    private void startSetPetNameFragment(SnapPets snapPet) {
        SetupNamePopup fragment = new SetupNamePopup();
        fragment.setSnappet(snapPet);
        fragment.setCameraFragment(cameraCallback);
        FragmentHelper.showWithoutSlideAnimation(getFragmentManager(), fragment, R.id.layout_popup, true);
    }

    private void startCheckPinDialog(final SnapPets snapPet) {
        String lastDeviceName = SharedSettingsHelper.getLastName(getActivity());
        String lastDevicePin = SharedSettingsHelper.getLastPin(getActivity());
        if (lastDeviceName != null && lastDeviceName.equals(ConnectionManager.mName) && lastDevicePin != null) {
            if (SnappetsHelper.IS_ENABLE_PIN_CHECKING) {
// AUTHENTICATION VIA PIN - UNCOMENT THIS SECTION OF CODE
                SnappetsHelper.getInstance().authenticateCallback = new AuthenticateCallback() {
                    @Override
                    public void didAuthenticate(boolean isAuthenticate) {
                        SnappetsHelper.getInstance().authenticateCallback = null;
                        if (!isAuthenticate) {
                            showCheckPinDialog(snapPet);
                        } else {
                            SnappetsHelper.getInstance().isPinCodeChecked = true;
                            SnappetsApplication.getApplicationClass(getActivity()).getDownloadService().startCheckNewPhotosThread();

                        }
                    }
                };
                snapPet.authenticatePinCode(lastDevicePin);
            } else {
// NOT AUTHENTICATION VIA PIN
                SnappetsHelper.getInstance().isPinCodeChecked = true;
                SnappetsApplication.getApplicationClass(getActivity()).getDownloadService().startCheckNewPhotosThread();
            }
        } else {
            showCheckPinDialog(snapPet);
        }
    }

    private void showCheckPinDialog(SnapPets snapPet) {
        CheckPinPopup checkPinFragment = new CheckPinPopup();
        checkPinFragment.setSnapPet(snapPet);
        checkPinFragment.setCameraCallback(cameraCallback);
        FragmentHelper.showWithoutSlideAnimation(getFragmentManager(), checkPinFragment, R.id.layout_popup, false);
    }

    private void updateLayoutForSnappetCamera() {
        ViewHelper.showViewWithAnimation(getActivity(), cameraToolLayout);
        cameraToolLayout.setBackgroundColor(getResources().getColor(R.color.dark_grey_color));
        switchTimerLayout.setBackgroundColor(getResources().getColor(R.color.dark_grey_color));
        everyTimerLayout.setBackgroundColor(getResources().getColor(R.color.dark_grey_color));
        ViewHelper.invisibleViewWithAnimation(getActivity(), switchFlashButton);
        ViewHelper.invisibleViewWithAnimation(getActivity(), switchCameraButton);
        ViewHelper.showViewWithAnimation(getActivity(), previewImage);
        ViewHelper.showViewWithAnimation(getActivity(), snapPhoneLayout);
    }

    private void updateLayoutForPhoneCamera() {
//        ViewHelper.invisibleViewWithAnimation(getActivity(), settingButton);

        ViewHelper.showViewWithAnimation(getActivity(), cameraToolLayout);

        cameraToolLayout.setBackgroundColor(context.getResources().getColor(R.color.camera_tool_bg));
        switchTimerLayout.setBackgroundColor(context.getResources().getColor(R.color.camera_tool_bg));
        everyTimerLayout.setBackgroundColor(context.getResources().getColor(R.color.camera_tool_bg));

        if (cameraInstance.hasCameraFlash()) {
            ViewHelper.showViewWithAnimation(getActivity(), switchFlashButton);
        } else {
            ViewHelper.hideViewWithAnimation(getActivity(), switchFlashButton);
        }

        if (cameraInstance.hasCameraSwitch()) {
            ViewHelper.showViewWithAnimation(getActivity(), switchCameraButton);
        } else {
            ViewHelper.hideViewWithAnimation(getActivity(), switchCameraButton);
        }

        ViewHelper.hideViewWithAnimation(getActivity(), previewImage);
        previewImage.setImageResource(0);
        ViewHelper.hideViewWithAnimation(getActivity(), snapPhoneLayout);
    }

    private ConnectSnappetCallback getChecksnappetPressButtonCallback() {
        return new ConnectSnappetCallback() {
            @Override
            public void connected(SnapPets snapPet) {

            }

            @Override
            public void disconnected(SnapPets snapPet) {

            }

            @Override
            public void receiveImageTotalSize(int size) {

            }

            @Override
            public void receivedImagePieceSize(int size) {

            }

            @Override
            public void receivedImageBuffer(byte[] buffer) {

            }

            @Override
            public void didPressedButton() {
                handler.pressTakePicture();
            }

            @Override
            public void receivedPhotoCount(int count) {

            }

            @Override
            public void didDeletePhoto(int id) {

            }
        };
    }


    private void setupSwithTimerLayout(View view) {
        // Add switch timer mode
        switchTimerButton = (Button) view.findViewById(R.id.btn_timer);
        switchFlashButton.setText(getResources().getString(R.string.flash_mode_off));
        cameraTimer = CAMERA_TIMER.OFF;

        switchTimerButton.setOnClickListener(this);
        ViewHelper.setButtonTouchListener(switchTimerButton);

        view.findViewById(R.id.sec3).setOnClickListener(this);
        view.findViewById(R.id.sec7).setOnClickListener(this);
        view.findViewById(R.id.sec10).setOnClickListener(this);
        view.findViewById(R.id.close_timer_bar).setOnClickListener(this);
        ViewHelper.setButtonTouchListener(view.findViewById(R.id.sec3));
        ViewHelper.setButtonTouchListener(view.findViewById(R.id.sec7));
        ViewHelper.setButtonTouchListener(view.findViewById(R.id.sec10));
        ViewHelper.setButtonTouchListener(view.findViewById(R.id.close_timer_bar));
        switchTimerLayout = (ViewGroup) view.findViewById(R.id.layout_timer_tool);
    }

    private void setupSwitchCameraButton(View view) {
        // Add switch camera
        switchCameraButton = (Button) view.findViewById(R.id.btn_camswitch);
        switchCameraButton.setText(getResources().getString(R.string.front_camera));
        if (cameraInstance.hasCameraSwitch()) {
            ViewHelper.setButtonTouchListener(switchCameraButton);
            switchCameraButton.setOnClickListener(this);
        }

    }

    private void setupSwitchFlashButton(View view) {
        // Add switch flash mode
        switchFlashButton = (Button) view.findViewById(R.id.btn_flash);
        if (cameraInstance.hasCameraFlash()) {
            cameraFlash = CAMERA_FLASH.OFF;

            ViewHelper.setButtonTouchListener(switchFlashButton);
            switchFlashButton.setOnClickListener(this);
        }
    }

    private void createCameraPreview(View rootView) {
        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(context, cameraInstance.camera);
        mPreview.setOnClickListener(this);
        RelativeLayout preview = (RelativeLayout) rootView.findViewById(R.id.layout_camera_xxx);
        preview.addView(mPreview);
    }

    private void setupSettingsButton(View rootView) {
        settingButton = rootView.findViewById(R.id.btn_settings);
        ViewHelper.setButtonTouchListener(settingButton);
        settingButton.setOnClickListener(this);
        ViewHelper.setButtonTouchListener(settingButton);
    }

    private void galleryButtonClicked() {
        FragmentHelper.switchFragment(getFragmentManager(), new GalleryFragment(), R.id.layout_core, true);
    }

    private void snapButtonClicked() {
        if (!handler.pressTakePicture()) {
        }
    }

    private void cameraCancelButtonClicked() {
        countDownTimer.cancel();
        countDownTimer = null;

        ViewHelper.invisibleViewWithAnimation(getActivity(), countdownText);
        ViewHelper.invisibleViewWithAnimation(getActivity(), countdownSnapText);

        ViewHelper.showViewWithAnimation(getActivity(), cameraToolLayout);
        ViewHelper.hideViewWithAnimation(getActivity(), cameraControlLayout.findViewById(R.id.btn_cam_cancel));
    }

    private void backButtonClicked() {
        cameraCallback.init();
    }

    private void switchTimerButtonClicked() {
        ViewHelper.showViewWithAnimation(getActivity(), switchTimerLayout);
        ViewHelper.hideViewWithAnimation(getActivity(), cameraToolLayout);
        ViewHelper.hideViewWithAnimation(getActivity(), cameraControlLayout);
    }


    private void switchCameraButtonClicked() {
        if (cameraInstance.switchCamera()) {
            mPreview.updateCamera(cameraInstance.camera);
            if (cameraInstance.hasCameraFlash()) {
                if (cameraInstance.getCameraId() == 0) {
                    switchFlashButton.setEnabled(true);
                    cameraInstance.setCameraFlash(cameraFlash.mode);
                    switchCameraButton.setText(getResources().getString(R.string.front_camera));
                } else {
                    switchFlashButton.setEnabled(false);
                    switchCameraButton.setText(getResources().getString(R.string.back_camera));
                }
            }
        }
    }

    private void switchFlashButtonClicked() {
        cameraFlash = cameraFlash.next();
        cameraInstance.setCameraFlash(cameraFlash.mode);
        if(cameraFlash.mode == CAMERA_FLASH.OFF.mode) {
            switchFlashButton.setText(getResources().getString(R.string.flash_mode_off));
        } else if(cameraFlash.mode == CAMERA_FLASH.ON.mode) {
            switchFlashButton.setText(getResources().getString(R.string.flash_mode_on));
        } else if(cameraFlash.mode == CAMERA_FLASH.AUTO.mode) {
            switchFlashButton.setText(getResources().getString(R.string.flash_mode_auto));
        } else {
            switchFlashButton.setText(getResources().getString(R.string.flash_mode_disable));
        }
    }

    private void previewClicked() {
        if (cameraTimer != CAMERA_TIMER.OFF
                && countDownTimer != null) {
            //cancel the countdown
            countDownTimer.cancel();
            countDownTimer = null;

            //show the camera tool
            ViewHelper.showViewWithAnimation(getActivity(), cameraToolLayout);

            //hidden the count down
            ViewHelper.invisibleViewWithAnimation(getActivity(), countdownText);
            ViewHelper.invisibleViewWithAnimation(getActivity(), countdownSnapText);

            if (cameraMode == CAMERA_MODE.SNAPPET) {
                //camera tool
                ViewHelper.showViewWithAnimation(getActivity(), cameraToolLayout);
                ViewHelper.hideViewWithAnimation(getActivity(), cameraControlLayout.findViewById(R.id.btn_cam_cancel));
//                cameraToolLayout.setBackgroundResource(0);

                //show the snappet's hint
//                ViewHelper.showViewWithAnimation(getActivity(), previewImage);
//                previewImage.setBackgroundColor(context.getResources().getColor(R.color.theme_color));
//                if (animationPlayback != null) {
//                    animationPlayback.stop();
//                }
//                animationPlayback = new ImageViewAnimationPlayback(
//                        previewImage, new int[]{
//                        R.drawable.anime_popup_pet_as_cam_1,
//                        R.drawable.anime_popup_pet_as_cam_2},
//                        500 else {
                ViewHelper.showViewWithAnimation(getActivity(), cameraToolLayout);
                ViewHelper.hideViewWithAnimation(getActivity(), cameraControlLayout.findViewById(R.id.btn_cam_cancel));
            }
            //TODO: show the Toast for cancel
        }
    }

    private void settingsButtonClicked() {
        //use current Snappet instead of null for SettingsFragmment
        SettingsFragment fragment = new SettingsFragment();
        fragment.setSnappet(SnappetsHelper.getInstance().getConnectedSnappet());
        fragment.setCameraCallback(cameraCallback);
        FragmentHelper.switchFragment(getFragmentManager(), fragment, R.id.layout_core, true);
    }

    private void createCameraInstance() {
        cameraInstance = CameraInstance.createInstance(context, getResources().getString(R.string.store_image_folder), EXPECTED_PHOTO_WIDTH, EXPECTED_PHTOT_HEIGHT);
        cameraInstance.createCamera();
    }

    public void remoteTakePictureFromSnanpet() {
        handler.pressTakePicture();
    }

    //don't call this function directly, since this function will be triggered by TIMER too
    private void doTakePicture() {
        if (cameraMode == CAMERA_MODE.PHONE) {
            // get an image from the camera
            cameraInstance.takePicture(cameraCallback);
        } else if (cameraMode == CAMERA_MODE.SNAPPET) {
            /**
             * workflow for show the picture:
             *
             * 1. [UI] trigger the transfer UI by cameraCallback.beforeTakePicture()
             * 2. [BLE] send cmd and receive callback from BLE?
             * 3. [BLE] receive the data from snappet
             * 4. [UI] update the loading progress by handler.updateLoadingProgress(progress)
             * 5. [UI] when transfer is done, do post-process and trigger the picture UI by takePictureFromSnappet(data)
             */

            //send the command to snappet to take picture
            //TODO new logic of loading picture
            if (downloadService == null)
                downloadService = SnappetsApplication.getApplicationClass(this.getActivity()).getDownloadService();
            if (downloadService != null) {
                downloadService.setStatisticListener(getDownloadStatisticListener());
                downloadService.startTakePicture();
                cameraCallback.beforeTakePicture();
            }
        }
    }

    private void saveCurrentPhoto(File imageFile) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(imageFile), null, options);
            Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            savePhotoOnDisk(mutableBitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void savePhotoOnDisk(Bitmap mutableBitmap) {
        String file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        File dir = new File(file_path);

        if (!dir.exists())
            dir.mkdirs();

        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
        Date date = new Date();

        String name = getResources().getString(R.string.new_photo_default_name).concat(dateFormat.format(date)).concat(".png");
        File file = new File(dir, name);
        try {
            file.createNewFile();
            FileOutputStream fOut = null;

            fOut = new FileOutputStream(file);
            mutableBitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);

            fOut.flush();
            fOut.close();

            notifyGallery(file.getPath());

            Toast.makeText(getActivity(), getResources().getString(R.string.saved_photo_text).concat(file_path),
                    Toast.LENGTH_SHORT).show();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void notifyGallery(String path) {
        MediaScannerConnection.scanFile(context,
                new String[]{path}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                    }
                });
    }

    private IDownloadImageStatisticListener getDownloadStatisticListener() {
        return new IDownloadImageStatisticListener() {


            @Override
            public void finishDownload() {

            }

            @Override
            public void downloadedPictureProgress(int pictruesCount, int picturesTotalCount, int imageBufferSize, int imageTotalSize) {
                handler.updateLoadingProgress(imageBufferSize * 100 / imageTotalSize);
            }
        };
    }


    public interface Handler {
        boolean pressTakePicture();

        void updateLoadingProgress(int percentage);    //100 is the base value

        void setTitle(String name);
    }
}
