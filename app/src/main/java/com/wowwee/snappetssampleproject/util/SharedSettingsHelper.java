package com.wowwee.snappetssampleproject.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedSettingsHelper {
    private static final String SP_NAME = "SP_NAME";
    private static final String LAST_DEVICE_NAME = "LAST_DEVICE_NAME";
    private static final String LAST_PIN = "LAST_PIN";

    public static void saveSnappetAndPin(Context context, String snapPetsDisplayName, String pin) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(LAST_DEVICE_NAME, snapPetsDisplayName);
        editor.putString(LAST_PIN, pin);
        editor.commit();
    }

    public static String getLastName(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return sp.getString(LAST_DEVICE_NAME, null);
    }

    public static String getLastPin(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return sp.getString(LAST_PIN, null);
    }
}
