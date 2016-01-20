package com.wowwee.snappetssampleproject.util;

import com.wowwee.snappetssampleproject.util.ViewUtility.ScreenReferenceInterface;

/**
 * this screen info is based on 5.0" full HD screen
 *
 * @author ericbear
 */
public final class SnappetsScreenReference implements ScreenReferenceInterface {

    @Override
    public float getReferenceDensity() {
        return 480;
    }

    @Override
    public float getReferenceWidthPixels() {
        return 1080;
    }

    @Override
    public float getReferenceHeightPixels() {
        return 1920;
    }

}
