package com.wowwee.snappetssampleproject.fragments.popups;

import android.graphics.Typeface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.wowwee.bluetoothrobotcontrollib.snappets.SnapPets;
import com.wowwee.snappetssampleproject.R;
import com.wowwee.snappetssampleproject.fragments.BaseFragment;
import com.wowwee.snappetssampleproject.snappethelper.SnappetsHelper;
import com.wowwee.snappetssampleproject.util.CameraInstance;
import com.wowwee.snappetssampleproject.util.ConnectionManager;
import com.wowwee.snappetssampleproject.util.FragmentHelper;
import com.wowwee.snappetssampleproject.util.SnappetsScreenReference;
import com.wowwee.snappetssampleproject.util.TypeFaceHelper;
import com.wowwee.snappetssampleproject.util.ViewHelper;

public class SetupNamePopup extends BaseFragment {

    private SnapPets snapPet;
    private CameraInstance.Callback cameraCallback;
    private EditText nameEdit;


    public SetupNamePopup() {
        initializeFragment(R.layout.fragment_popup_setup_name, new SnappetsScreenReference());
    }

    public void setSnappet(SnapPets snappet) {
        this.snapPet = snappet;
    }

    public void setCameraFragment(CameraInstance.Callback cameraCallback) {
        this.cameraCallback = cameraCallback;
    }

    @Override
    public void _onCreateView(View view) {
        disableClickUnderBG(view);
        setupNameEditText(view);
        setupNextButton(view);
    }

    private void setupNextButton(View view) {
        final View nextButton = view.findViewById(R.id.btn_popup_next);
        ViewHelper.setButtonTouchListener(nextButton);
        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEdit.getText().toString();
                if (validateName(name)) {
                    setupNameToPets(name);
                    startSetupPinDialog();
                }
            }
        });
    }

    private void startSetupPinDialog() {
        SetPinPopup fragment = new SetPinPopup();
        fragment.setSnapPet(snapPet);
        fragment.setCameraCallback(cameraCallback);
        FragmentHelper.showWithoutSlideAnimation(getFragmentManager(), fragment, R.id.layout_popup, true);
    }

    private void setupNameToPets(String name) {
        ConnectionManager.mName = name;
        //SnappetsHelper.getInstance().getConnectedSnappet().writeModuleInfoSnappetDisplayName(ConnectionManager.mName);
        SnappetsHelper.getInstance().getConnectedSnappet().writeModuleInfoSnappetDisplayName(ConnectionManager.mName);
        ConnectionManager.mRobot.setSnapPetsDisplayName(ConnectionManager.mName);

    }

    private boolean validateName(String name) {
        //TODO VALIDATION FOR NAME
        return true;
    }

    private void setupNameEditText(View view) {
        nameEdit = (EditText) view.findViewById(R.id.edit_name);
        nameEdit.setText(ConnectionManager.mName);
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), TypeFaceHelper.SWANSE_BOLD);
        nameEdit.setTypeface(typeface);

    }

    private void disableClickUnderBG(View view) {
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    @Override
    public boolean allowBackPress() {
        return false;
    }

}
