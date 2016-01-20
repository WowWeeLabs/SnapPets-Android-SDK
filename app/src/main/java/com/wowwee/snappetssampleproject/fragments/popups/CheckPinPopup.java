package com.wowwee.snappetssampleproject.fragments.popups;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.wowwee.bluetoothrobotcontrollib.snappets.SnapPets;
import com.wowwee.snappetssampleproject.R;
import com.wowwee.snappetssampleproject.SnappetsApplication;
import com.wowwee.snappetssampleproject.enums.CAMERA_MODE;
import com.wowwee.snappetssampleproject.fragments.BaseFragment;
import com.wowwee.snappetssampleproject.fragments.CameraFragment;
import com.wowwee.snappetssampleproject.snappethelper.AuthenticateCallback;
import com.wowwee.snappetssampleproject.snappethelper.ConnectSnappetCallback;
import com.wowwee.snappetssampleproject.snappethelper.SnappetsHelper;
import com.wowwee.snappetssampleproject.util.CameraInstance;
import com.wowwee.snappetssampleproject.util.ConnectionManager;
import com.wowwee.snappetssampleproject.util.FragmentHelper;
import com.wowwee.snappetssampleproject.util.SharedSettingsHelper;
import com.wowwee.snappetssampleproject.util.SnappetsScreenReference;
import com.wowwee.snappetssampleproject.util.TypeFaceHelper;
import com.wowwee.snappetssampleproject.util.ViewHelper;

public class CheckPinPopup extends BaseFragment {
    private View backButton;
    private View closeButton;
    private View okButton;
    private EditText pinEdit;
    private View invalidMessage;
    private CameraInstance.Callback cameraCallback;
    private SnapPets snapPet;

    public CheckPinPopup() {
        initializeFragment(R.layout.fragment_popup_check_pin, new SnappetsScreenReference());
    }

    public void setSnapPet(SnapPets snapPet) {
        this.snapPet = snapPet;
    }

    public void setCameraCallback(CameraInstance.Callback cameraCallback) {
        this.cameraCallback = cameraCallback;
    }

    @Override
    public void _onCreateView(View view) {
        //disable any touch event to pass through to back side
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        TextView petNameTextView = (TextView) view.findViewById(R.id.pet_name);
        petNameTextView.setText(ConnectionManager.mName);
        invalidMessage = view.findViewById(R.id.invalidPin);

        pinEdit = (EditText) view.findViewById(R.id.edit_pin);
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), TypeFaceHelper.SWANSE_BOLD);
        pinEdit.setTypeface(typeface);


        okButton = view.findViewById(R.id.btn_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPin();
            }
        });

        closeButton = view.findViewById(R.id.btn_close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnectDeviceAndClose();

            }
        });

        backButton = view.findViewById(R.id.btn_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewHelper.hideViewWithAnimation(getActivity(), backButton);
                ViewHelper.hideViewWithAnimation(getActivity(), invalidMessage);
                ViewHelper.showViewWithAnimation(getActivity(), closeButton);
                ViewHelper.showViewWithAnimation(getActivity(), pinEdit);
                ViewHelper.showViewWithAnimation(getActivity(), okButton);
//                backButton.setVisibility(View.GONE);
//                invalidMessage.setVisibility(View.GONE);
//                closeButton.setVisibility(View.VISIBLE);
//                pinEdit.setVisibility(View.VISIBLE);
//                okButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private void disconnectDeviceAndClose() {

        SnappetsHelper.getInstance().disconnect(snapPet, new ConnectSnappetCallback() {
            @Override
            public void connected(SnapPets snapPet) {

            }

            @Override
            public void disconnected(SnapPets snapPet) {
                CameraFragment.cameraMode = CAMERA_MODE.PHONE;
                closeButtonClicked();
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

            }

            @Override
            public void receivedPhotoCount(int count) {

            }

            @Override
            public void didDeletePhoto(int id) {

            }
        });
    }

    private void closeButtonClicked() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cameraCallback.init();
                FragmentHelper.removeFragment(getFragmentManager(), R.id.layout_popup);
            }
        });
    }

    private void checkPin() {
        String pin = pinEdit.getText().toString();
        if (SnappetsHelper.IS_ENABLE_PIN_CHECKING) {
            if (!pin.isEmpty() && SetPinPopup.validatePin(pin)) {
// AUTHENTICATION VIA PIN
                SnappetsHelper.getInstance().authenticateCallback = new AuthenticateCallback() {
                    @Override
                    public void didAuthenticate(boolean isAuthenticate) {
                        SnappetsHelper.getInstance().authenticateCallback = null;
                        if (isAuthenticate) {
                            SharedSettingsHelper.saveSnappetAndPin(getActivity(), snapPet.getName(), pinEdit.getText().toString());
                            SnappetsApplication.getApplicationClass(getActivity()).getDownloadService().startCheckNewPhotosThread();
                            SnappetsHelper.getInstance().isPinCodeChecked = true;
                            SnappetsHelper.getInstance().enableSnapPetNotificatios(SnappetsHelper.getInstance().getConnectedSnappet());
                            closeButtonClicked();
                        } else {
                            showQuestionBeforeReset();
                        }
                    }
                };

                snapPet.authenticatePinCode(pin);
            } else {
                invalidPin();
            }
        } else {
            if (!pin.isEmpty() && SetPinPopup.validatePin(pin)) {
// NOT AUTHENTICATION VIA PIN
                SnappetsApplication.getApplicationClass(getActivity()).getDownloadService().startCheckNewPhotosThread();
                SnappetsHelper.getInstance().isPinCodeChecked = true;
                closeButtonClicked();
            }
        }
    }

    private void showQuestionBeforeReset() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.reset));
                builder.setMessage(getString(R.string.reset_press_button_on_top_of_snappet));
                builder.setPositiveButton(getString(R.string.popup_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        snapPet.resetAsciiPinCodeAndEraseFlash();
                        disconnectDeviceAndClose();
//                        startResetDialog();
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.show();
            }
        });
    }

    private void startResetDialog() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.reset));
                builder.setMessage(getString(R.string.reset_press_button_on_top_of_snappet));
                builder.setPositiveButton(getString(R.string.popup_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        snapPet.resetAsciiPinCodeAndEraseFlash();
                        disconnectDeviceAndClose();
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.show();
            }
        });
    }

    private void invalidPin() {
        ViewHelper.hideViewWithAnimation(getActivity(), pinEdit);
        ViewHelper.hideViewWithAnimation(getActivity(), closeButton);
        ViewHelper.showViewWithAnimation(getActivity(), backButton);
        ViewHelper.showViewWithAnimation(getActivity(), invalidMessage);
        ViewHelper.hideViewWithAnimation(getActivity(), okButton);
//        pinEdit.setVisibility(View.GONE);
//        closeButton.setVisibility(View.GONE);
//        backButton.setVisibility(View.VISIBLE);
//        invalidMessage.setVisibility(View.VISIBLE);
//        okButton.setVisibility(View.GONE);
    }

    @Override
    public boolean allowBackPress() {
        return false;
    }

}
