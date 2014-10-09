package com.example.camera360.testcamera;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;


public class MyActivity extends Activity {

    CameraFragment cameraFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        addCameraFragment();
        initImageLoader(this);
    }


    private void addCameraFragment() {
        addFragment(R.id.fragment_container, cameraFragment = CameraFragment.newInstance());

    }

    private void addFragment(int id, Fragment fragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(id, fragment);
        transaction.commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {

            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (null != cameraFragment) {
                    cameraFragment.zoomDown();
                }

                return true;

            case KeyEvent.KEYCODE_VOLUME_UP:
                if (null != cameraFragment) {
                    cameraFragment.zoomUp();
                }

                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public static void initImageLoader(Context context) {

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(50 * 1024 * 1024) // 50 Mb
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs() // Remove for release app
                .build();

        ImageLoader.getInstance().init(config);
    }


}
