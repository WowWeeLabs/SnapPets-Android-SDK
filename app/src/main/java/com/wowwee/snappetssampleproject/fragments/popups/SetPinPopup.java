package com.wowwee.snappetssampleproject.fragments.popups;

import android.graphics.Typeface;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.wowwee.bluetoothrobotcontrollib.snappets.SnapPets;
import com.wowwee.snappetssampleproject.R;
import com.wowwee.snappetssampleproject.fragments.BaseFragment;
import com.wowwee.snappetssampleproject.snappethelper.SnappetsHelper;
import com.wowwee.snappetssampleproject.util.CameraInstance;
import com.wowwee.snappetssampleproject.util.ConnectionManager;
import com.wowwee.snappetssampleproject.util.FragmentHelper;
import com.wowwee.snappetssampleproject.util.SharedSettingsHelper;
import com.wowwee.snappetssampleproject.util.SnappetsScreenReference;
import com.wowwee.snappetssampleproject.util.TypeFaceHelper;
import com.wowwee.snappetssampleproject.util.ViewHelper;

public class SetPinPopup extends BaseFragment {

    private SnapPets snapPet;
    private CameraInstance.Callback cameraCallback;
    private EditText pinEdit;

    public SetPinPopup() {
        initializeFragment(R.layout.fragment_popup_setup_pin, new SnappetsScreenReference());
    }

    public static boolean validatePin(String pin) {
        return pin.length() > 0 && pin.length() <= 4 && pin.matches("[0-9]+");
    }

    public void setCameraCallback(CameraInstance.Callback cameraCallback) {
        this.cameraCallback = cameraCallback;
    }

    @Override
    public void _onCreateView(View view) {
        setupBackButton(view);
        disableTouchUnderBG(view);
        setupPinEditText(view);
        setupNextButton(view);
    }

    private void setupNextButton(View view) {
        final View nextButton = view.findViewById(R.id.btn_popup_next);
        ViewHelper.setButtonTouchListener(nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pin = pinEdit.getText().toString();
                if (validatePin(pin)) {
                    if (SnappetsHelper.IS_ENABLE_PIN_CHECKING)
//TODO - AUTHENTICATION VIA PIN - UNCOMENT THIS SECTION OF CODE
                        writePinOnSnappet(pin);
//=============================================
                    closeFragment();
                } else
                    Toast.makeText(getActivity(), getString(R.string.invalid_pin), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void closeFragment() {
        FragmentHelper.popToRoot(getFragmentManager());
        //SnappetsHelper.getInstance().isPinCodeChecked = true;
        cameraCallback.init();
    }

    private void writePinOnSnappet(String pin) {
        //TODO write pin code to Snap Pet
        snapPet.writePinCode(pin);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        snapPet.enableSnapPetsAsciiPinCode(true);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        SharedSettingsHelper.saveSnappetAndPin(getActivity(), ConnectionManager.mName, pin);
    }

    private void setupPinEditText(View view) {
        pinEdit = (EditText) view.findViewById(R.id.edit_pin);
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), TypeFaceHelper.SWANSE_BOLD);
        pinEdit.setTypeface(typeface);
    }

    private void disableTouchUnderBG(View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

    }

    private void setupBackButton(View view) {
        View backButton = view.findViewById(R.id.btn_back);
        ViewHelper.setButtonTouchListener(backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentHelper.popBackStack(getFragmentManager());
            }
        });

    }

    @Override
    public boolean allowBackPress() {
        return false;
    }

    public SnapPets getSnapPet() {
        return snapPet;
    }

    public void setSnapPet(SnapPets snapPet) {
        this.snapPet = snapPet;
    }
}
