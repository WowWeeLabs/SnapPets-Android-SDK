package com.wowwee.snappetssampleproject.enums;


public enum CAMERA_FLASH {

    OFF(FLASH_MODE.OFF, 0),

    ON(FLASH_MODE.ON, 1),

    AUTO(FLASH_MODE.AUTO, 2),

    DISABLE(null, -1);

    public final FLASH_MODE mode;
    final int index;

    final int INDEX_COUNT = 3;

    CAMERA_FLASH(FLASH_MODE mode, int index) {
        this.mode = mode;
        this.index = index;
    }

    public CAMERA_FLASH next() {
        if (index == -1) {
            return DISABLE;
        } else {
            return getByIndex((index + 1) % INDEX_COUNT);
        }
    }

    private CAMERA_FLASH getByIndex(int index) {
        for (CAMERA_FLASH c : CAMERA_FLASH.values()) {
            if (c.index == index) {
                return c;
            }
        }

        return DISABLE;
    }
}
