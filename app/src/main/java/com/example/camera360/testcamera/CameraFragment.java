package com.example.camera360.testcamera;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;


public class CameraFragment extends Fragment implements
        SurfaceHolder.Callback, View.OnClickListener, Camera.AutoFocusCallback {

    SurfaceView surfaceView;
    ImageButton takePhotoBtn;
    SurfaceHolder holder;
    Camera camera;


    // TODO: Rename and change types and number of parameters
    public static CameraFragment newInstance() {
        CameraFragment fragment = new CameraFragment();
        return fragment;
    }

    public CameraFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_camera, null);
        initView(view);
        return view;
    }

    /**
     * 初始化控件
     */
    private void initView(View view) {
        takePhotoBtn = (ImageButton) view.findViewById(R.id.takephoto_btn);
        takePhotoBtn.setOnClickListener(this);

        surfaceView = (SurfaceView) view.findViewById(R.id.cameraview);
        holder = surfaceView.getHolder();
        holder.setFixedSize(1280, 720);
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }


    @Override
    public void onAutoFocus(boolean b, Camera camera) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (camera == null) {
            camera = Camera.open();
            try {
                camera.setPreviewDisplay(holder);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                camera = null;
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        if (null!=camera){
            camera.setDisplayOrientation(getPreviewDegree(getActivity()));
            Camera.Parameters parameters = camera.getParameters(); // 获取各项参数
            parameters.setPictureFormat(PixelFormat.JPEG); // 设置图片格式
            parameters.setPreviewSize(i2, i3); // 设置预览大小
            parameters.setPictureSize(i2, i3); // 设置保存的图片尺寸
            parameters.setJpegQuality(80); // 设置照片质量
            camera.startPreview();
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.takephoto_btn:
                Toast.makeText(getActivity(), "test", Toast.LENGTH_SHORT).show();
                break;

        }
    }

    // 提供一个静态方法，用于根据手机方向获得相机预览画面旋转的角度
    public static int getPreviewDegree(Activity activity) {
        // 获得手机的方向
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degree = 0;
        // 根据手机的方向计算相机预览画面应该选择的角度
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 90;
                break;
            case Surface.ROTATION_90:
                degree = 0;
                break;
            case Surface.ROTATION_180:
                degree = 270;
                break;
            case Surface.ROTATION_270:
                degree = 180;
                break;
        }
        return degree;
    }
}
