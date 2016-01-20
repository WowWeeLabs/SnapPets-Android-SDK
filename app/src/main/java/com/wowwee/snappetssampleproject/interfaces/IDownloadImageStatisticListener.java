package com.wowwee.snappetssampleproject.interfaces;

public interface IDownloadImageStatisticListener {

    void finishDownload();

    void downloadedPictureProgress(final int pictruesCount, final int picturesTotalCount, final int imageBufferSize, final int imageTotalSize);
}
