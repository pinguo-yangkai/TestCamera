package com.example.camera360.testcamera;

import android.app.Activity;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.List;


public class ResizableCameraPreview extends CameraPreview {

    private static final String TAG = "ResizableCameraPreview";

    public ResizableCameraPreview(Activity activity, int cameraId, LayoutMode mode) {
        super(activity, cameraId, mode);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        stopPreview();
        Camera.Parameters cameraParams = mCamera.getParameters();
        boolean portrait = isPortrait();

        if (!mSurfaceConfiguring) {
            Camera.Size previewSize = determinePreviewSize(portrait, width, height);
            Camera.Size pictureSize = determinePictureSize(previewSize);
            Log.d(TAG, "宽高 w: " + width     + ", h: " + height);
            mPreviewSize = previewSize;
            mPictureSize = pictureSize;
            mSurfaceConfiguring = adjustSurfaceLayoutSize(previewSize, portrait, width, height);
            if (mSurfaceConfiguring) {
                return;
            }
        }

        configureCameraParameters(cameraParams, portrait);
        mSurfaceConfiguring = false;

        try {
            startPreView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 切换预览尺寸
     *
     * @param index
     * @param width
     * @param height
     */
    public void setPreviewSize(int index, int width, int height) {
        stopPreview();

        Camera.Parameters cameraParams = mCamera.getParameters();
        boolean portrait = isPortrait();

        Camera.Size previewSize = mPreviewSizeList.get(index);
        Camera.Size pictureSize = determinePictureSize(previewSize);

        Log.d(TAG, "preview - w: " + previewSize.width + ", h: " + previewSize.height);

        mPreviewSize = previewSize;
        mPictureSize = pictureSize;
        boolean layoutChanged = adjustSurfaceLayoutSize(previewSize, portrait, width, height);
        if (layoutChanged) {
            mSurfaceConfiguring = true;
            return;
        }

        configureCameraParameters(cameraParams, portrait);
        try {
            startPreView();
        } catch (Exception e) {
//            e.printStackTrace();
        }
        mSurfaceConfiguring = false;
    }

    /**
     * 得到相机所致吃的预览尺寸集合
     *
     * @return
     */
    public List<Camera.Size> getSupportedPreivewSizes() {

        return mPreviewSizeList;
    }
}
