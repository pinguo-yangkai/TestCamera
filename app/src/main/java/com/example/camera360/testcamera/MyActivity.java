package com.example.camera360.testcamera;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.pm.ActivityInfo;
import android.os.Bundle;


public class MyActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_my);
        addCameraFragment();
    }



    private void addCameraFragment(){
        addFragment(R.id.fragment_container,CameraFragment.newInstance());

    }

    private void addFragment(int id, Fragment fragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(id, fragment);
        transaction.commit();
    }


}
