package com.wowwee.snappetssampleproject.enums;

public enum CAMERA_TIMER {
    OFF(0, 0),
    THREE_SECONDS(4000, 1),
    SEVEN_SECONDS(8000, 2),
    TEN_SECONDS(11000, 3);

    public final int waitTime;
    public final int index;

    final int INDEX_COUNT = 4;

    CAMERA_TIMER(int waitTime, int index) {
        this.waitTime = waitTime;
        this.index = index;
    }

    public CAMERA_TIMER next() {
        return getByIndex((index + 1) % INDEX_COUNT);
    }

    private CAMERA_TIMER getByIndex(int index) {
        for (CAMERA_TIMER c : CAMERA_TIMER.values()) {
            if (c.index == index) {
                return c;
            }
        }
        return OFF;
    }

    public int getIndex() {
        return index;
    }
}
