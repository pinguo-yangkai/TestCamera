package com.example.camera360.testcamera;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;


public class PhotosActivity extends Activity {

    private static final String TAG = "PhotosActivity";

    private GridView photosGridview;


    DisplayImageOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);
        initView();
        initOptions();
        initData();
    }

    /**
     * 初始化View
     */
    private void initView() {
        photosGridview = (GridView) findViewById(R.id.photos_gridview);
    }

    /**
     * 初始化ImageLaoder配置
     */
    private void initOptions() {
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    /**
     * 初始化数据
     */
    private void initData() {

        String[] proj = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.TITLE};
        String selecttions = MediaStore.Images.Media.DESCRIPTION + " = ?";
        String[] selectionArgs = {CameraFragment.NAME_SIGN};

        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj, selecttions, selectionArgs, null);

        photosGridview.setAdapter(new PhotoCursorAdapter(this, cursor));
    }


    class PhotoCursorAdapter extends CursorAdapter {


        public PhotoCursorAdapter(Context context, Cursor cursor) {
            super(context, cursor);

        }

        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.photos_item, null);


            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ImageView photoView = (ImageView) view.findViewById(R.id.photos_item_imageview);
            TextView titleView = (TextView) view.findViewById(R.id.photos_item_title);

            String path = cursor.getString(1);

            File file = new File(path);
            Uri uri = Uri.fromFile(file);


            ImageLoader.getInstance().displayImage(uri.toString().trim(), photoView, options);


            titleView.setText(cursor.getString(2));
        }
    }


}
