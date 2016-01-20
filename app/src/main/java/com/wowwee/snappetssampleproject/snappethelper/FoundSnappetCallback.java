package com.wowwee.snappetssampleproject.snappethelper;

import com.wowwee.bluetoothrobotcontrollib.snappets.SnapPets;

import java.util.List;

public interface FoundSnappetCallback {
    void callback(List<SnapPets> snappets);
}
