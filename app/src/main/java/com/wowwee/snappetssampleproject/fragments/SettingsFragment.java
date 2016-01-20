package com.wowwee.snappetssampleproject.fragments;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wowwee.bluetoothrobotcontrollib.snappets.SnapPets;
import com.wowwee.snappetssampleproject.R;
import com.wowwee.snappetssampleproject.SnappetsApplication;
import com.wowwee.snappetssampleproject.enums.CAMERA_MODE;
import com.wowwee.snappetssampleproject.snappethelper.ConnectSnappetCallback;
import com.wowwee.snappetssampleproject.snappethelper.SnappetsHelper;
import com.wowwee.snappetssampleproject.util.CameraInstance;
import com.wowwee.snappetssampleproject.util.FragmentHelper;
import com.wowwee.snappetssampleproject.util.SnappetsScreenReference;
import com.wowwee.snappetssampleproject.util.ViewHelper;

public class SettingsFragment extends BaseFragment {
    private SnapPets snappet;
    private CameraInstance.Callback cameraCallback;

    public SettingsFragment() {
        initializeFragment(R.layout.fragment_settings, new SnappetsScreenReference());
    }

    public void setSnappet(SnapPets snappet) {
        this.snappet = snappet;
    }

    public void setCameraCallback(CameraInstance.Callback callback) {
        this.cameraCallback = callback;
    }

    @Override
    public void _onCreateView(View view) {
        setupBackButtonClicked(view);
        this.snappet = SnappetsHelper.getInstance().getConnectedSnappet();
        if (snappet != null) {
            setupSnappetName(view);
            setupPin(view);
            setupResetButton(view);
            setupDisconnectButton(view);
            setupFirmwareVersion(view);
        } else {
            enablePin(view, false);
            enableResetButton(view, false);
            enableFirmwareVersion(view, false);
            enableVersionRow(view, false);
        }
    }

    private void enableFirmwareVersion(View view, boolean isEnabled) {
        LinearLayout row = ((LinearLayout) view.findViewById(R.id.row_update));
        if (isEnabled) {
            row.setVisibility(View.VISIBLE);
        } else {
            row.setVisibility(View.GONE);
        }
    }

    private void enableVersionRow(View view, boolean isEnabled) {
        LinearLayout row = ((LinearLayout) view.findViewById(R.id.row_general_version));
        if (isEnabled) {
            row.setVisibility(View.VISIBLE);
        } else {
            row.setVisibility(View.GONE);
        }
    }

    private void setupFirmwareVersion(View view) {
        TextView updateInfo = (TextView) view.findViewById(R.id.updateInfo);
        updateInfo.setText(getResources().getString(R.string.firmware_version) + ": " + snappet.getFirmwareVersion());
    }

    private void setupDisconnectButton(View view) {
        View disconnectButton = view.findViewById(R.id.btn_disconnect);
        ViewHelper.setButtonTouchListener(disconnectButton, getResources().getColor(R.color.grey_color));
        disconnectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //disconnect snappet
                SnappetsApplication.getApplicationClass(getActivity()).getDownloadService().stopThread();
                SnappetsHelper.getInstance().disconnect(snappet, getDisconnectSnappet());
            }
        });
    }

    private ConnectSnappetCallback getDisconnectSnappet() {
        return new ConnectSnappetCallback() {
            @Override
            public void disconnected(SnapPets snapPet) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CameraFragment.cameraMode = CAMERA_MODE.PHONE;
                        cameraCallback.init();

                        FragmentHelper.popBackStack(getFragmentManager());
                    }
                });
            }

            @Override
            public void connected(SnapPets snapPet) {
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
                // TODO Auto-generated method stub

            }

            @Override
            public void didDeletePhoto(int id) {
                // TODO Auto-generated method stub

            }
        };
    }

    private void enableResetButton(View view, boolean isEnabled) {
        LinearLayout row = ((LinearLayout) view.findViewById(R.id.row_general_reset));
        if (isEnabled) {
            row.setVisibility(View.VISIBLE);
        } else {
            row.setVisibility(View.GONE);
        }
    }

    private void setupResetButton(View view) {
        View resetButton = view.findViewById(R.id.reset);
        ViewHelper.setButtonTouchListener(resetButton);
        resetButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentHelper.switchFragment(getFragmentManager(), new ResetFragment(), R.id.layout_core, true);
            }
        });
    }

    private void enablePin(View view, boolean isEnabled) {
        LinearLayout row = ((LinearLayout) view.findViewById(R.id.row_customization));
        if (isEnabled) {
            row.setVisibility(View.VISIBLE);
        } else {
            row.setVisibility(View.GONE);
        }
    }

    private void setupPin(View view) {
        TextView snappetPin = ((TextView) view.findViewById(R.id.snapPetPIN));
        View row = (View) view.findViewById(R.id.row_pin);
        //snappetPin.setText(getString(R.string.Snap_Pet_Pin) + " ");
        snappetPin.setText(getString(R.string.snappet_pin));
        ViewHelper.setButtonTouchListener(snappetPin);
        snappetPin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentHelper.switchFragment(getFragmentManager(), new SetupPinFragment(), R.id.layout_core, true);
            }
        });
        if (SnappetsHelper.IS_ENABLE_PIN_CHECKING) {

        } else {
//            snappetPin.setVisibility(View.GONE);
            row.setVisibility(View.GONE);
        }
    }

    private void setupSnappetName(View view) {
        TextView snappetName = ((TextView) view.findViewById(R.id.snapPetName));
        //snappetName.setText(getString(R.string.Snap_Pet_Name) + " " + ConnectionManager.mName);
        snappetName.setText(getString(R.string.snappet_name));
        ViewHelper.setButtonTouchListener(snappetName);
        snappetName.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentHelper.switchFragment(getFragmentManager(), new SetupNameFragment(), R.id.layout_core, true);
            }
        });
    }

    private void setupBackButtonClicked(View view) {
        View backButton = view.findViewById(R.id.btn_back);
        ViewHelper.setButtonTouchListener(backButton);
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraCallback.init();
                FragmentHelper.popBackStack(getFragmentManager());
            }
        });
    }

    @Override
    public boolean allowBackPress() {
        cameraCallback.init();
        return super.allowBackPress();
    }
}
