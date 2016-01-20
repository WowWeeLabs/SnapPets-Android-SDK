package com.wowwee.snappetssampleproject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.multidex.MultiDexApplication;
import com.wowwee.snappetssampleproject.services.DownloadService;

public class SnappetsApplication extends MultiDexApplication /*implements IAviaryClientCredentials*/ {
    public static int DAYS_BEFORE_RATE;
    public static int PHOTOS_BEFORE_RATE;
    private static boolean IS_DEBUG = true;
    private DownloadService mService;
    private boolean mBound;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            DownloadService.LocalBinder binder = (DownloadService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public static boolean isDebug() {
        return IS_DEBUG;
    }

    public static SnappetsApplication getApplicationClass(Activity context) {
        return (SnappetsApplication) context.getApplication();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IS_DEBUG = getResources().getBoolean(R.bool.is_debug);
        DAYS_BEFORE_RATE = getResources().getInteger(R.integer.days_before_rate);
        PHOTOS_BEFORE_RATE = getResources().getInteger(R.integer.photos_before_rate);

        bindService(new Intent(this, DownloadService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    public DownloadService getDownloadService() {
        return mService;
    }

    /*
    @Override
    public String getBillingKey() {
        return "";
    }

    @Override
    public String getClientID() {
        return "32b2b1abde004b73b351f9a60bb8dc58";
    }

    @Override
    public String getClientSecret() {
        return "e6ec21ee-7c95-4007-ad62-3dcd75030d6c";
    }
    */
}