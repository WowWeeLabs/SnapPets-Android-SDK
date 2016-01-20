package com.wowwee.snappetssampleproject.enums;

public enum CAMERA_MODE {
    PHONE(0),
    SNAPPET(1);

    public final int mode;

    CAMERA_MODE(int mode) {
        this.mode = mode;
    }

    public CAMERA_MODE swap() {
        if (this == PHONE) {
            return SNAPPET;
        } else {
            return PHONE;
        }
    }
}
