package com.example.camera360.testcamera;

import android.app.Activity;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;


public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static boolean DEBUGGING = true;
    private static final String LOG_TAG = "CameraPreviewSample";
    private static final String CAMERA_PARAM_ORIENTATION = "orientation";
    private static final String CAMERA_PARAM_LANDSCAPE = "landscape";
    private static final String CAMERA_PARAM_PORTRAIT = "portrait";
    protected Activity mActivity;
    private SurfaceHolder mHolder;
    protected Camera mCamera;
    protected List<Camera.Size> mPreviewSizeList;
    protected List<Camera.Size> mPictureSizeList;
    protected Camera.Size mPreviewSize;
    protected Camera.Size mPictureSize;
    private int mSurfaceChangedCallDepth = 0;
    private int mCameraId;
    private LayoutMode mLayoutMode;
    private int mCenterPosX = -1;
    private int mCenterPosY;
    private int cameraId=0;

    PreviewReadyCallback mPreviewReadyCallback = null;


    public static enum LayoutMode {
        FitToParent,
        NoBlank
    }

    ;

    public interface PreviewReadyCallback {
        public void onPreviewReady();
    }


    protected boolean mSurfaceConfiguring = false;

    public CameraPreview(Activity activity, LayoutMode mode) {
        super(activity); // Always necessary
        mActivity = activity;
        mLayoutMode = mode;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /**
     * 初始化相机（主要是在Fragment或者activity的onResume里调用）
     */
    public void initCamera(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            if (Camera.getNumberOfCameras() > cameraId) {
                mCameraId = cameraId;
            } else {
                mCameraId = 0;
            }
        } else {
            mCameraId = 0;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            mCamera = Camera.open(mCameraId);
        } else {
            mCamera = Camera.open();
        }
        Camera.Parameters cameraParams = mCamera.getParameters();
        mPreviewSizeList = cameraParams.getSupportedPreviewSizes();
        mPictureSizeList = cameraParams.getSupportedPictureSizes();
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("11111111111","surfaceCreated");
        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            mCamera.release();
            mCamera = null;
        }
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d("11111111111","surfaceChanged");
        mSurfaceChangedCallDepth++;
        doSurfaceChanged(width, height);
        mSurfaceChangedCallDepth--;
    }

    private void doSurfaceChanged(int width, int height) {
        stopPreview();

        Camera.Parameters cameraParams = mCamera.getParameters();
        boolean portrait = isPortrait();

        if (!mSurfaceConfiguring) {
            Camera.Size previewSize = determinePreviewSize(portrait, width, height);
            Camera.Size pictureSize = determinePictureSize(previewSize);
            if (DEBUGGING) {
                Log.v(LOG_TAG, "Desired Preview Size - w: " + width + ", h: " + height);
            }
            mPreviewSize = previewSize;
            mPictureSize = pictureSize;
            mSurfaceConfiguring = adjustSurfaceLayoutSize(previewSize, portrait, width, height);

            if (mSurfaceConfiguring && (mSurfaceChangedCallDepth <= 1)) {
                return;
            }
        }

        configureCameraParameters(cameraParams, portrait);
        mSurfaceConfiguring = false;

        try {
            startPreView();
        } catch (Exception e) {

            mPreviewSizeList.remove(mPreviewSize);
            mPreviewSize = null;

            // Reconfigure
            if (mPreviewSizeList.size() > 0) { // prevent infinite loop
                surfaceChanged(null, 0, width, height);
            } else {
                Toast.makeText(mActivity, "不能开启预览", Toast.LENGTH_LONG).show();

            }
        }

        if (null != mPreviewReadyCallback) {
            mPreviewReadyCallback.onPreviewReady();
        }
    }


    protected Camera.Size determinePreviewSize(boolean portrait, int reqWidth, int reqHeight) {

        int reqPreviewWidth;
        int reqPreviewHeight;
        if (portrait) {
            reqPreviewWidth = reqHeight;
            reqPreviewHeight = reqWidth;
        } else {
            reqPreviewWidth = reqWidth;
            reqPreviewHeight = reqHeight;
        }

        if (DEBUGGING) {
            Log.v(LOG_TAG, "监听大小");
            for (Camera.Size size : mPreviewSizeList) {
                Log.v(LOG_TAG, "  w: " + size.width + ", h: " + size.height);
            }
            Log.v(LOG_TAG, "所有支持的尺寸");
            for (Camera.Size size : mPictureSizeList) {
                Log.v(LOG_TAG, "  w: " + size.width + ", h: " + size.height);
            }
        }

        // Adjust surface size with the closest aspect-ratio
        float reqRatio = ((float) reqPreviewWidth) / reqPreviewHeight;
        float curRatio, deltaRatio;
        float deltaRatioMin = Float.MAX_VALUE;
        Camera.Size retSize = null;
        for (Camera.Size size : mPreviewSizeList) {
            curRatio = ((float) size.width) / size.height;
            deltaRatio = Math.abs(reqRatio - curRatio);

            if (deltaRatio < deltaRatioMin) {
                deltaRatioMin = deltaRatio;
                retSize = size;
                Log.d(LOG_TAG, " reqRatio: " + reqRatio + ", curRatio: " + curRatio + ", deltaRatio: " + deltaRatio);
            }
        }

        return retSize;
    }

    protected Camera.Size determinePictureSize(Camera.Size previewSize) {
        Camera.Size retSize = null;
        for (Camera.Size size : mPictureSizeList) {
            if (size.equals(previewSize)) {
                return size;
            }
        }


        // if the preview size is not supported as a picture size
        float reqRatio = ((float) previewSize.width) / previewSize.height;
        float curRatio, deltaRatio;
        float deltaRatioMin = Float.MAX_VALUE;
        for (Camera.Size size : mPictureSizeList) {
            curRatio = ((float) size.width) / size.height;
            deltaRatio = Math.abs(reqRatio - curRatio);
            if (deltaRatio < deltaRatioMin) {
                deltaRatioMin = deltaRatio;
                retSize = size;
            }
        }

        return retSize;
    }

    protected boolean adjustSurfaceLayoutSize(Camera.Size previewSize, boolean portrait,
                                              int availableWidth, int availableHeight) {
        float tmpLayoutHeight, tmpLayoutWidth;
        if (portrait) {
            tmpLayoutHeight = previewSize.width;
            tmpLayoutWidth = previewSize.height;
        } else {
            tmpLayoutHeight = previewSize.height;
            tmpLayoutWidth = previewSize.width;
        }

        float factH, factW, fact;
        factH = availableHeight / tmpLayoutHeight;
        factW = availableWidth / tmpLayoutWidth;
        if (mLayoutMode == LayoutMode.FitToParent) {

            if (factH < factW) {
                fact = factH;
            } else {
                fact = factW;
            }
        } else {
            if (factH < factW) {
                fact = factW;
            } else {
                fact = factH;
            }
        }

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.getLayoutParams();

        int layoutHeight = (int) (tmpLayoutHeight * fact);
        int layoutWidth = (int) (tmpLayoutWidth * fact);
        if (DEBUGGING) {
            Log.v(LOG_TAG, "预览 Size - w: " + layoutWidth + ", h: " + layoutHeight);
            Log.v(LOG_TAG, "缩放: " + fact);
        }

        boolean layoutChanged;
        if ((layoutWidth != this.getWidth()) || (layoutHeight != this.getHeight())) {
            layoutParams.height = layoutHeight;
            layoutParams.width = layoutWidth;
            if (mCenterPosX >= 0) {
                layoutParams.topMargin = mCenterPosY - (layoutHeight / 2);
                layoutParams.leftMargin = mCenterPosX - (layoutWidth / 2);
            }
            this.setLayoutParams(layoutParams);
            layoutChanged = true;
        } else {
            layoutChanged = false;
        }

        return layoutChanged;
    }


    public void setCenterPosition(int x, int y) {
        mCenterPosX = x;
        mCenterPosY = y;
    }

    protected void configureCameraParameters(Camera.Parameters cameraParams, boolean portrait) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) { // for 2.1 and before
            if (portrait) {
                cameraParams.set(CAMERA_PARAM_ORIENTATION, CAMERA_PARAM_PORTRAIT);
            } else {
                cameraParams.set(CAMERA_PARAM_ORIENTATION, CAMERA_PARAM_LANDSCAPE);
            }
        } else { //  2.2
            int angle;
            Display display = mActivity.getWindowManager().getDefaultDisplay();
            switch (display.getRotation()) {
                case Surface.ROTATION_0:
                    angle = 90;
                    break;
                case Surface.ROTATION_90:
                    angle = 0;
                    break;
                case Surface.ROTATION_180:
                    angle = 270;
                    break;
                case Surface.ROTATION_270:
                    angle = 180;
                    break;
                default:
                    angle = 90;
                    break;
            }
            Log.v(LOG_TAG, "angle: " + angle);
            mCamera.setDisplayOrientation(angle);
        }

        cameraParams.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        cameraParams.setPictureSize(mPictureSize.width, mPictureSize.height);
        if (DEBUGGING) {
            Log.v(LOG_TAG, "Preview Actual Size - w: " + mPreviewSize.width + ", h: " + mPreviewSize.height);
            Log.v(LOG_TAG, "Picture Actual Size - w: " + mPictureSize.width + ", h: " + mPictureSize.height);
        }

        mCamera.setParameters(cameraParams);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("11111111111","surfaceDestroyed");
    }


    /**
     * 释放相机
     */
    public void stop() {
        if (null == mCamera) {
            return;
        }
        stopPreview();
        mCamera.release();
        mCamera = null;
    }

    /**
     * 停止预览
     */
    public void stopPreview(){
        if (null == mCamera) {
            return;
        }
        mCamera.stopPreview();
    }


    public boolean isPortrait() {
        return (mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
    }

    public void setOneShotPreviewCallback(PreviewCallback callback) {
        if (null == mCamera) {
            return;
        }
        mCamera.setOneShotPreviewCallback(callback);
    }

    public void setPreviewCallback(PreviewCallback callback) {
        if (null == mCamera) {
            return;
        }
        mCamera.setPreviewCallback(callback);
    }

    public Camera.Size getPreviewSize() {
        return mPreviewSize;
    }

    public void setOnPreviewReady(PreviewReadyCallback cb) {
        mPreviewReadyCallback = cb;
    }


    /**
     * 自动聚焦
     */
    public void autoFocus() {
        if (null != mCamera) {
            mCamera.autoFocus(null);
        }
    }

    /**
     * 拍照
     *
     * @param jpegCallback
     */
    public void takePacture(Camera.PictureCallback jpegCallback) {
        if (null != mCamera) {
            mCamera.takePicture(null, null, jpegCallback);
        }
    }


    /**
     *
     */
    public void startPreView() {
        if (null != mCamera) {
            mCamera.startPreview();
        }
    }

    /**
     * 是否支持变焦
     *
     * @return
     */
    public Boolean isZoomSupport() {
        Boolean isSupport = false;
        if (null != mCamera) {
            isSupport = mCamera.getParameters().isZoomSupported();
        }
        return isSupport;
    }


    /**
     * 得到相机的缩放程度
     *
     * @return
     */
    public int getMaxZoom() {
        int maxzoom = 0;
        if (null != mCamera) {
            Camera.Parameters parameters = mCamera.getParameters();
            maxzoom = parameters.getMaxZoom();
        }
        return maxzoom;
    }

    /**
     * 得到当前缩放
     *
     * @return
     */
    public int getCurrentZoom() {
        int zoom = 0;
        if (null != mCamera) {
            Camera.Parameters parameters = mCamera.getParameters();
            zoom = parameters.getZoom();
        }
        return zoom;
    }


    /**
     * 设置缩放程度
     * @param zoom
     */
    public void setZoom(int zoom) {
        if (null != mCamera) {
            Camera.Parameters parameters = mCamera.getParameters();
            if (zoom < 0 || zoom > getMaxZoom())
                return;
            Log.d("zoom",zoom+"");
            parameters.setZoom(zoom);
            mCamera.setParameters(parameters);
        }
    }


}
