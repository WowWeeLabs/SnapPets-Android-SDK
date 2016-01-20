package com.wowwee.snappetssampleproject.enums;


public enum PICTURE_EVERY_TIMER {
    OFF(0, 0),
    THIRTY_SECONDS(31000, 1),
    TWO_MINUTES(121000, 2),
    FIVE_MINUTES(301000, 3);

    final int waitTime;
    final int index;

    final int INDEX_COUNT = 4;

    PICTURE_EVERY_TIMER(int waitTime, int index) {
        this.waitTime = waitTime;
        this.index = index;
    }

    public PICTURE_EVERY_TIMER next() {
        return getByIndex((index + 1) % INDEX_COUNT);
    }

    private PICTURE_EVERY_TIMER getByIndex(int index) {
        for (PICTURE_EVERY_TIMER c : PICTURE_EVERY_TIMER.values()) {
            if (c.index == index) {
                return c;
            }
        }

        return OFF;
    }
}
