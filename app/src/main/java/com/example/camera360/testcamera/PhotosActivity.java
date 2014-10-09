package com.example.camera360.testcamera;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class PhotosActivity extends Activity {

    private static final String TAG="PhotosActivity";

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
        File file = new File(CameraFragment.SAVE_PATH);
        List<PhotoEntry> photos = null;
        if (file.exists()) {
            photos = new ArrayList<PhotoEntry>();
            File[] files = file.listFiles();

            int size = files.length;
            for (int i = 0; i < size; i++) {
                PhotoEntry photoEntry = new PhotoEntry();
                photoEntry.uri =   Uri.fromFile(files[i]);
                Log.d(TAG, files[i].getAbsolutePath());
                photos.add(photoEntry);
            }
        }

        photosGridview.setAdapter(new PhotoAdapter(this, photos));

    }


    /**
     * 照片的Adapter
     */
    public class PhotoAdapter extends BaseAdapter

    {

        private Context context;
        private List<PhotoEntry> photos;

        public PhotoAdapter(Context context, List<PhotoEntry> photos) {
            this.context = context;
            this.photos = photos;
        }

        @Override
        public int getCount() {
            return null == photos ? 0 : photos.size();
        }

        @Override
        public Object getItem(int i) {
            return null == photos ? null : photos.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            PhotosHolder holder = null;
            if (null == view) {
                view = LayoutInflater.from(context).inflate(R.layout.phptos_item, null);
                view.setTag(holder = new PhotosHolder());
                holder.photoView = (ImageView) view.findViewById(R.id.photos_item_imageview);
            } else {
                holder = (PhotosHolder) view.getTag();
            }
            PhotoEntry photoEntry = (PhotoEntry) getItem(i);

            ImageLoader.getInstance().displayImage(photoEntry.uri.toString(), holder.photoView,options);

            return view;
        }
    }


    private class PhotosHolder {
        ImageView photoView;
    }


    /**
     * 每个照片的的实体类
     */
    public class PhotoEntry {
        public Uri uri;
    }
}
