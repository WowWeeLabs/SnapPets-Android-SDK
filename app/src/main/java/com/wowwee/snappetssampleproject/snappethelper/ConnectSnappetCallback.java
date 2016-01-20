package com.wowwee.snappetssampleproject.snappethelper;

import com.wowwee.bluetoothrobotcontrollib.snappets.SnapPets;

public interface ConnectSnappetCallback {
    void connected(SnapPets snapPet);

    void disconnected(SnapPets snapPet);

    void receiveImageTotalSize(int size);

    void receivedImagePieceSize(int size);

    void receivedImageBuffer(byte[] buffer);

    void didPressedButton();

    void receivedPhotoCount(int count);

    void didDeletePhoto(int id);
}
