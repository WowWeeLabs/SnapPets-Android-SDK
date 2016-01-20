package com.wowwee.snappetssampleproject.fragments;

import android.graphics.Typeface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.wowwee.bluetoothrobotcontrollib.snappets.SnapPets;
import com.wowwee.snappetssampleproject.R;
import com.wowwee.snappetssampleproject.snappethelper.SnappetsHelper;
import com.wowwee.snappetssampleproject.util.ConnectionManager;
import com.wowwee.snappetssampleproject.util.FragmentHelper;
import com.wowwee.snappetssampleproject.util.SnappetsScreenReference;
import com.wowwee.snappetssampleproject.util.ViewHelper;

public class SetupNameFragment extends BaseFragment {

    private SnapPets snappet;
    private EditText nameEditor;
    private Button okButton;

    public SetupNameFragment() {
        initializeFragment(R.layout.fragment_settings_setup_name, new SnappetsScreenReference());
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

        nameEditor = (EditText) view.findViewById(R.id.petName);
        Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/swanse_bold.ttf");
        nameEditor.setTypeface(typeFace);
        nameEditor.setText(ConnectionManager.mName);

        okButton = (Button) view.findViewById(R.id.okButton);
        ViewHelper.setButtonTouchListener(okButton);
        okButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String newName = String.valueOf(nameEditor.getText().toString());
                if (!newName.isEmpty()) {
                    if (ConnectionManager.mRobot.isConnected()) {
                        //Change the real name
                        ConnectionManager.mName = newName;
                        SnapPets currentSnapPet = SnappetsHelper.getInstance().getConnectedSnappet();
                        if (currentSnapPet != null)
                            snappet.writeModuleInfoSnappetDisplayName(newName);
                    }
                    FragmentHelper.popBackStack(getFragmentManager());
                }
            }
        });
    }

}
