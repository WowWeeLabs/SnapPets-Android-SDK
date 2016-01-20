package com.wowwee.snappetssampleproject.activities;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import com.wowwee.bluetoothrobotcontrollib.BluetoothRobot;
import com.wowwee.bluetoothrobotcontrollib.snappets.SnapPets;
import com.wowwee.bluetoothrobotcontrollib.snappets.SnapPetsFinder;
import com.wowwee.snappetssampleproject.R;
import com.wowwee.snappetssampleproject.SnappetsApplication;
import com.wowwee.snappetssampleproject.fragments.BaseFragment;
import com.wowwee.snappetssampleproject.fragments.CameraFragment;
import com.wowwee.snappetssampleproject.snappethelper.SnappetsHelper;
import com.wowwee.snappetssampleproject.util.FragmentHelper;
import com.wowwee.snappetssampleproject.util.SimpleAudioPlayer;

public class CoreActivity extends FragmentActivity {

    private final long QUIT_APP_TIME_INTERVAL = 2000;
    private long pressBackButtonTime = 0;
    private Handler quitAppHandler;
    private Runnable quitAppRunnable;
    private boolean isQuitedApp;

    public static boolean isAmazonDevice() {
        String manufacturer = android.os.Build.MANUFACTURER;    //Amazon
        if (manufacturer != null && manufacturer.equalsIgnoreCase("Amazon")) {
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_core);
        setupWindow();
        FragmentHelper.switchFragment(getFragmentManager(), new CameraFragment(), R.id.layout_core, false);

        SnappetsHelper.createInstance(this);
        setupSimpleAudioPlayer();
    }

    private void setupSimpleAudioPlayer() {
        SimpleAudioPlayer.getInstance().setActivityContext(this);
    }

    private void setupWindow() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.grey_color));
        }
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = FragmentHelper.getCurrentFragment();
        if (currentFragment instanceof BaseFragment && ((BaseFragment) currentFragment).allowBackPress() == false) {
            return;
        }

        if (FragmentHelper.popBackStack(getFragmentManager())) {
        } else {
            if (System.currentTimeMillis() - pressBackButtonTime < QUIT_APP_TIME_INTERVAL) {
                super.onBackPressed();
            } else {
                pressBackButtonTime = System.currentTimeMillis();
                Toast aToast = Toast.makeText(getBaseContext(), getString(R.string.quit_app_confirm), Toast.LENGTH_SHORT);
                TextView v = (TextView) aToast.getView().findViewById(android.R.id.message);
                v.setTextColor(Color.WHITE);
                aToast.show();
            }
        }
    }

    public void onStart() {
        super.onStart();
        if (quitAppHandler != null && quitAppRunnable != null) {
            quitAppHandler.removeCallbacks(quitAppRunnable);
        }
        isQuitedApp = false;
    }

    @Override
    public void onStop() {
        try {
            SnappetsApplication.getApplicationClass(this).getDownloadService().stopThread();
            SnappetsHelper.getInstance().stopSearch();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (quitAppHandler != null) {
            if (quitAppRunnable != null) {
                quitAppHandler.removeCallbacks(quitAppRunnable);
            }
            quitAppHandler = null;
        }

        //---for fire
        if (isAmazonDevice()) {
            for (Object obj : SnapPetsFinder.getInstance().getDevicesConnected()) {
                SnapPets robot = (SnapPets) obj;
                robot.disconnect();
            }
            //
//			 BluetoothRobot.unbindBluetoothLeService(CoreActivity.this);
        } else {


            if (quitAppRunnable != null) {
                quitAppRunnable = null;
            }

            quitAppRunnable = new Runnable() {

                @Override
                public void run() {

                    if (!isQuitedApp) {
                        for (Object obj : SnapPetsFinder.getInstance().getDevicesConnected()) {
                            SnapPets robot = (SnapPets) obj;
                            if (robot != null) {
                                robot.disconnect();
                                SnapPetsFinder.getInstance().robotDidDisconnect(robot); // Fixed a DownloadService crash since the system don't have enough time to remove the robot from finder
                            }
                        }
                        //
                        SnappetsHelper.getInstance().stopSearch();
                    }
//					BluetoothRobot.unbindBluetoothLeService(CoreActivity.this);
                    // change the view to select MiP view
                }
            };
//            SnappetsApplication.getApplicationClass(this).getDownloadService().stopSelf();
            quitAppHandler = new Handler();
            quitAppHandler.postDelayed(quitAppRunnable, QUIT_APP_TIME_INTERVAL);
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        SnappetsApplication.getApplicationClass(CoreActivity.this).getDownloadService().stopThread();
        Log.d("BluetoothLeService", "CoreActivity onDestroy, unbind BLE le service");
        for (Object obj : SnapPetsFinder.getInstance().getDevicesConnected()) {
            SnapPets robot = (SnapPets) obj;
            if (robot != null) {
                robot.disconnect();
                SnapPetsFinder.getInstance().robotDidDisconnect(robot); // Fixed a DownloadService crash since the system don't have enough time to remove the robot from finder.
            }
        }
        if (!isAmazonDevice()) {
            SnappetsHelper.getInstance().stopSearch();
            BluetoothRobot.unbindBluetoothLeService(CoreActivity.this);
//                  SnappetsApplication.getApplicationClass(this).getDownloadService().stopSelf();
            isQuitedApp = true;
        }

        super.onDestroy();
    }
}
