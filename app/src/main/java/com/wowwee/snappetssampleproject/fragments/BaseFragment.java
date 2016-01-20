package com.wowwee.snappetssampleproject.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wowwee.snappetssampleproject.util.ViewUtility.ScreenReferenceInterface;

public class BaseFragment extends Fragment {
    protected Context context;
    protected int layoutId;

    private ScreenReferenceInterface screenReferenceInterface;

    public void initializeFragment(int layoutId, ScreenReferenceInterface screenReferenceInterface) {
        this.layoutId = layoutId;
        this.screenReferenceInterface = screenReferenceInterface;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null)
            return null;
        final View view = inflater.inflate(layoutId, container, false);
        _onCreateView(view);

        return view;
    }

    public void _onCreateView(View view) {

    }

    public boolean allowBackPress() {
        return true;
    }
}
