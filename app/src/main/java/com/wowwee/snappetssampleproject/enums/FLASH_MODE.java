package com.wowwee.snappetssampleproject.enums;

import android.hardware.Camera;

public enum FLASH_MODE {
    OFF(Camera.Parameters.FLASH_MODE_OFF),
    ON(Camera.Parameters.FLASH_MODE_ON),
    AUTO(Camera.Parameters.FLASH_MODE_AUTO);

    public final String value;

    FLASH_MODE(String value) {
        this.value = value;
    }
}