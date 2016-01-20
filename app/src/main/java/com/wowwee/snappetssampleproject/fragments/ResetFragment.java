package com.wowwee.snappetssampleproject.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;

import com.wowwee.bluetoothrobotcontrollib.snappets.SnapPets;
import com.wowwee.snappetssampleproject.R;
import com.wowwee.snappetssampleproject.snappethelper.SnappetsHelper;
import com.wowwee.snappetssampleproject.util.FragmentHelper;
import com.wowwee.snappetssampleproject.util.SnappetsScreenReference;
import com.wowwee.snappetssampleproject.util.ViewHelper;

public class ResetFragment extends BaseFragment {

    private SnapPets snappet;
    private View confirmResetLayout;
    private View resetPetButton;

    public ResetFragment() {
        initializeFragment(R.layout.fragment_reset, new SnappetsScreenReference());
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

        confirmResetLayout = view.findViewById(R.id.confirmResetLayout);
        ViewHelper.hideViewWithAnimation(getActivity(), confirmResetLayout);
        resetPetButton = view.findViewById(R.id.resetSnapPet);
        ViewHelper.setButtonTouchListener(resetPetButton);
        resetPetButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.reset));
                builder.setMessage(getString(R.string.reset_press_button_on_top_of_snappet));
                builder.setPositiveButton(getString(R.string.popup_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        showConfirmDialog();
                        snappet.resetAsciiPinCodeAndEraseFlash();
                        snappet.disconnect();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        FragmentHelper.popBackStack(getFragmentManager());
                        FragmentHelper.popBackStack(getFragmentManager());
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });
    }

}
