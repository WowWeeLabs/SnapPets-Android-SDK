package com.wowwee.snappetssampleproject.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.wowwee.snappetssampleproject.R;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class GaleryAdapter extends android.widget.BaseAdapter {
    private final int PREVIEW_IMAGE_WIDTH = 480;
    private final Context context;
    private final View.OnClickListener onClickListener;
    private final List<File> selectedFiles;
    private List<File> galleryFiles;
    private HashMap<String, BitmapDrawable> mapDrawable;

    public GaleryAdapter(Context context, List<File> galleryFiles, List<File> selectedFiles, View.OnClickListener onItemClickListener) {
        this.context = context;
        this.galleryFiles = galleryFiles;
        this.selectedFiles = selectedFiles;
        this.onClickListener = onItemClickListener;
        this.mapDrawable = new HashMap<String, BitmapDrawable>();
    }

    public void clearCache() {
        mapDrawable.clear();
        System.gc();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View grid;

        if (convertView == null) {
            grid = ((LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.grid_gallery_photo, parent, false);
        } else
            grid = convertView;

        File photo = galleryFiles.get(position);
        grid.setTag(photo);
        Button photoImageView = (Button) grid.findViewById(R.id.img_photo);
        if (mapDrawable.containsKey(photo.getAbsolutePath()))
            photoImageView.setBackground(mapDrawable.get(photo.getAbsolutePath()));
        else {
            BitmapDrawable drawable = getBitmapDrawable(photo);
            mapDrawable.put(photo.getAbsolutePath(), drawable);
            photoImageView.setBackground(drawable);
        }

        grid.setOnClickListener(onClickListener);
//				}
//				else {
//					grid = convertView;
//				}
        grid.findViewById(R.id.img_select).setVisibility(View.GONE);
        for (File selectedPhoto : selectedFiles) {
            if (selectedPhoto == photo) {
                grid.findViewById(R.id.img_select).setVisibility(View.VISIBLE);
            }
        }

        return grid;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return galleryFiles.get(position);
    }

    @Override
    public int getCount() {
        return galleryFiles.size();
    }

    private BitmapDrawable getBitmapDrawable(File file) {
        //create bitmap from path
        Bitmap photoBitmap = BitmapFactory.decodeFile(file.getPath());

        //rescale the bitmap
        try {
            float scaleRatio = PREVIEW_IMAGE_WIDTH / (float) photoBitmap.getWidth();
            Matrix photoTransform = new Matrix();
            photoTransform.setScale(scaleRatio, scaleRatio);
            photoBitmap = Bitmap.createBitmap(photoBitmap, 0, 0, PREVIEW_IMAGE_WIDTH, PREVIEW_IMAGE_WIDTH, photoTransform, true);
        } catch (Exception ignored) {
        }

        return new BitmapDrawable(context.getResources(), photoBitmap);
    }

    public void setGalleryFiles(List<File> galleryFiles) {
        this.galleryFiles = galleryFiles;
    }
}
