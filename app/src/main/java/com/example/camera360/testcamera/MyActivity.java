package com.example.camera360.testcamera;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.KeyEvent;


public class MyActivity extends Activity {

    CameraFragment cameraFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        addCameraFragment();
    }


    private void addCameraFragment() {
        addFragment(R.id.fragment_container, cameraFragment=CameraFragment.newInstance());

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
                if(null!=cameraFragment){
                    cameraFragment.zoomDown();
                }

                return true;

            case KeyEvent.KEYCODE_VOLUME_UP:
                if(null!=cameraFragment){
                    cameraFragment.zoomUp();
                }

                return true;
        }
        return super.onKeyDown(keyCode, event);
    }



}
