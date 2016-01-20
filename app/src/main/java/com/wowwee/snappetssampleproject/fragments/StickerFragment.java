package com.wowwee.snappetssampleproject.fragments;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.wowwee.snappetssampleproject.R;
import com.wowwee.snappetssampleproject.util.DialogUtil;
import com.wowwee.snappetssampleproject.util.DrawableUtil;
import com.wowwee.snappetssampleproject.util.FragmentHelper;
import com.wowwee.snappetssampleproject.util.ShareUtil;
import com.wowwee.snappetssampleproject.util.SnappetsScreenReference;
import com.wowwee.snappetssampleproject.util.ViewHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StickerFragment extends BaseFragment {
    private File imageFile;

    public StickerFragment() {
        initializeFragment(R.layout.fragment_sticker, new SnappetsScreenReference());
    }

    public void setImageFile(File imageFile) {
        this.imageFile = imageFile;
    }

    @Override
    public void _onCreateView(View view) {
        setupBackButton(view);
        setupImageView(view);
        setupShareButton(view);
        setupDeleteButton(view);

        System.gc();
    }

    public void onStart() {
        super.onStart();
    }

    public void onStop() {
        super.onStart();
    }

    private void sharePhotoOnDisk(Bitmap mutableBitmap) {
        String file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        File dir = new File(file_path);

        if (!dir.exists())
            dir.mkdirs();

        List<File> imageFiles = new ArrayList<>();
        File file = new File(dir, "temp.png");
        try {
            file.createNewFile();
            FileOutputStream fOut = null;

            fOut = new FileOutputStream(file);
            mutableBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);

            fOut.flush();
            fOut.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//                imageFiles.add(imageFile);
        imageFiles.add(file);
//        ShareUtil.shareTextWithMultipleImages(context, getString(R.string.share_msg), imageFiles);
        ShareUtil.shareTextWithMultipleImages(context, "", imageFiles);
    }

    private void setupDeleteButton(View view) {
        View deleteButton = view.findViewById(R.id.btn_delete);
        ViewHelper.setButtonTouchListener(deleteButton);
        deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtil.showYesNoDialog(
                        context,
                        getString(R.string.photos_will_be_deleted_on_snappet_and_app),
                        getString(R.string.yes),
                        getString(R.string.no),
                        new DialogUtil.Callback() {
                            @Override
                            public void callback(DialogInterface dialog) {
                                imageFile.delete();
                                FragmentHelper.popBackStack(getFragmentManager());
                            }
                        },
                        null);
            }
        });
    }

    private void setupShareButton(View view) {
        View shareButton = view.findViewById(R.id.btn_share);
        ViewHelper.setButtonTouchListener(shareButton);
        shareButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(imageFile), null, options);
                    Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    sharePhotoOnDisk(mutableBitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void setupImageView(View view) {
        ImageView imageView = (ImageView) view.findViewById(R.id.img_camera_preview);
        Drawable drawable = DrawableUtil.getDrawableFromFile(context, imageFile);
        if (drawable != null)
            imageView.setImageDrawable(drawable);
    }

    private void setupBackButton(View view) {
        View backButton = view.findViewById(R.id.btn_back);
        ViewHelper.setButtonTouchListener(backButton);
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentHelper.popBackStack(getFragmentManager());
            }
        });
    }
}
