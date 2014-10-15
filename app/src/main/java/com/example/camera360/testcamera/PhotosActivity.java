package com.example.camera360.testcamera;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.List;


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
//        File file = new File(CameraFragment.SAVE_PATH);
//        List<PhotoEntry> photos = null;
//        if (file.exists()) {
//            photos = new ArrayList<PhotoEntry>();
//            File[] files = file.listFiles();
//
//            int size = files.length;
//            for (int i = 0; i < size; i++) {
//                PhotoEntry photoEntry = new PhotoEntry();
//                photoEntry.uri =   Uri.fromFile(files[i]);
//                Log.d(TAG, files[i].getAbsolutePath());
//                photos.add(photoEntry);
//            }
//        }
        String[] proj = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.TITLE};
        String selecttions = MediaStore.Images.Media.DESCRIPTION + " = ?";
        String[] selectionArgs = {CameraFragment.NAME_SIGN};

        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj, selecttions, selectionArgs, null);

        photosGridview.setAdapter(new PhotoCursorAdapter(this, cursor));
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
                view = LayoutInflater.from(context).inflate(R.layout.photos_item, null);
                view.setTag(holder = new PhotosHolder());
                holder.photoView = (ImageView) view.findViewById(R.id.photos_item_imageview);
            } else {
                holder = (PhotosHolder) view.getTag();
            }
            PhotoEntry photoEntry = (PhotoEntry) getItem(i);

            ImageLoader.getInstance().displayImage(photoEntry.uri.toString(), holder.photoView, options);

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

        ContentResolver cr = getContentResolver();

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

//            ImageLoader.getInstance().displayImage(uri.toString().trim(), photoView, options, new PhotoListener());

            titleView.setText(cursor.getString(2));
        }
    }

//
//    class PhotoListener implements ImageLoadingListener {
//
//
//        @Override
//        public void onLoadingStarted(String s, View view) {
//
//        }
//
//        @Override
//        public void onLoadingFailed(String s, View view, FailReason failReason) {
//
//        }
//
//        @Override
//        public void onLoadingComplete(String s, View view, Bitmap bitmap) {
//
//            Uri uri = Uri.parse(s);
//
//            try {
//                //android读取图片EXIF信息
//                ExifInterface exifInterface = new ExifInterface(uri.getPath());
//
//                String width = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
//                String height = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
//                Log.d(TAG, width + "+" + height);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//
////            ImageView imageView = (ImageView) view;
////            imageView.setImageBitmap(ImageCrop(bitmap));
//
//
//        }
//
//        @Override
//        public void onLoadingCancelled(String s, View view) {
//
//        }
//    }
//
//    /**
//     * 按正方形裁切图片
//     */
//    public static Bitmap ImageCrop(Bitmap bitmap) {
//        int w = bitmap.getWidth(); // 得到图片的宽，高
//        int h = bitmap.getHeight();
//
//        int wh = w > h ? h : w;// 裁切后所取的正方形区域边长
//
//        int retX = w > h ? (w - h) / 2 : 0;//基于原图，取正方形左上角x坐标
//        int retY = w > h ? 0 : (h - w) / 2;
//
//        //下面这句是关键
//        return Bitmap.createBitmap(bitmap, retX, retY, wh, wh, null, false);
//    }

}
