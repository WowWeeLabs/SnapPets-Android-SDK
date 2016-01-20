package com.wowwee.snappetssampleproject.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.view.WindowManager;

import com.wowwee.snappetssampleproject.R;

public class SplashActivity extends FragmentActivity implements Runnable {
    private static final int kSHOW_SPLASH_PAGE_TIME = 3000;

    private boolean m_startedHandler = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_splash);

        setupWindow();

        Intent intent = getIntent();
        boolean shouldFinishApp = intent.getBooleanExtra("finishapp", false);
        if (shouldFinishApp) {
            finish();
            return;
        }

        //setup the timer for splash page
        if (!this.m_startedHandler) {
            this.m_startedHandler = true;
            (new Handler()).postDelayed(this, kSHOW_SPLASH_PAGE_TIME);
        }
    }

    private void setupWindow() {
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onResume() {
        super.onResume();
        setupWindow();
    }

    @Override
    public void run() {
        startActivity(new Intent(this, CoreActivity.class));
    }
}
