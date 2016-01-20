package com.wowwee.snappetssampleproject.fragments;

import android.graphics.Typeface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.wowwee.bluetoothrobotcontrollib.snappets.SnapPets;
import com.wowwee.snappetssampleproject.R;
import com.wowwee.snappetssampleproject.fragments.popups.SetPinPopup;
import com.wowwee.snappetssampleproject.snappethelper.SnappetsHelper;
import com.wowwee.snappetssampleproject.util.ConnectionManager;
import com.wowwee.snappetssampleproject.util.FragmentHelper;
import com.wowwee.snappetssampleproject.util.SharedSettingsHelper;
import com.wowwee.snappetssampleproject.util.SnappetsScreenReference;
import com.wowwee.snappetssampleproject.util.ViewHelper;

public class SetupPinFragment extends BaseFragment {

    private SnapPets snappet;
    private EditText pinEditor;
    private Button okButton;

    public SetupPinFragment() {
        initializeFragment(R.layout.fragment_settings_setup_pin, new SnappetsScreenReference());
    }

    public void setSnappet(SnapPets snappet) {
        this.snappet = snappet;
    }

    @Override
    public void _onCreateView(View view) {
        this.snappet = SnappetsHelper.getInstance().getConnectedSnappet();

        View backButton = view.findViewById(R.id.btn_back);
        ViewHelper.setButtonTouchListener(backButton);
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentHelper.popBackStack(getFragmentManager());
            }
        });

        pinEditor = (EditText) view.findViewById(R.id.pinEdit);
        Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/swanse_bold.ttf");
        pinEditor.setTypeface(typeFace);

        okButton = (Button) view.findViewById(R.id.okButton);
        ViewHelper.setButtonTouchListener(okButton);
        okButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String newPin = String.valueOf(pinEditor.getText().toString());
                if (!newPin.isEmpty() && SetPinPopup.validatePin(newPin)) {
                    snappet.writePinCode(newPin);
                    SharedSettingsHelper.saveSnappetAndPin(getActivity(), ConnectionManager.mName, newPin);
                    FragmentHelper.popBackStack(getFragmentManager());
                }
            }
        });
    }

}
