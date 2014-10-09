package com.example.camera360.testcamera;

import android.app.Fragment;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class CameraFragment extends Fragment implements
        View.OnClickListener, View.OnLongClickListener, CameraPreview.PreviewReadyCallback, AdapterView.OnItemSelectedListener {

    private String TAG = CameraFragment.this.getClass().getSimpleName();

    private RelativeLayout mainlayout;
    private ImageButton takePhotoBtn;
    private ResizableCameraPreview mPreview;
    private SeekBar zoomSeekbar;
    private File saveFile;
    private boolean isLongPress = false;
    //预览尺寸Adapter
    private ArrayAdapter<String> sizeAdapter;
    private Spinner sizeSpinner;

    public static CameraFragment newInstance() {
        CameraFragment fragment = new CameraFragment();
        return fragment;
    }

    public CameraFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createSaveFile();
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
        mainlayout = (RelativeLayout) view;

        takePhotoBtn = (ImageButton) view.findViewById(R.id.takephoto_btn);
        takePhotoBtn.setOnClickListener(this);
        takePhotoBtn.setOnLongClickListener(this);
        zoomSeekbar = (SeekBar) view.findViewById(R.id.zoom_seekbar);
        zoomSeekbar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        showSeekbar();
        zoomSeekbar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                showSeekbar();
                return false;
            }
        });

        sizeSpinner = (Spinner) view.findViewById(R.id.size_spinner);
        sizeAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item);
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sizeSpinner.setAdapter(sizeAdapter);
        sizeSpinner.setOnItemSelectedListener(this);
    }

    /**
     * 现实便教条两秒
     */
    public void showSeekbar() {
        handler.removeCallbacks(runnable);
        zoomSeekbar.setVisibility(View.VISIBLE);
        handler.postDelayed(runnable, 2000);
    }

    Handler handler = new Handler() {

    };

    /**
     * 对焦seekbar消失Runable
     */
    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            zoomSeekbar.setVisibility(View.GONE);
        }
    };


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.size_spinner:
                Rect rect = new Rect();
                mainlayout.getDrawingRect(rect);

                if (0 == i) {
                    mPreview.surfaceChanged(null, 0, rect.width(), rect.height());
                } else {
                    mPreview.setPreviewSize(i - 1, rect.width(), rect.height());
                }
                break;

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        mPreview = new ResizableCameraPreview(getActivity(), 0, CameraPreview.LayoutMode.FitToParent);
        mPreview.setOnPreviewReady(this);
        mPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSeekbar();
            }
        });
        RelativeLayout.LayoutParams previewLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        mainlayout.addView(mPreview, 0, previewLayoutParams);
        zoomSeekbar.setMax(mPreview.getMaxZoom());

        sizeAdapter.clear();
        sizeAdapter.add("默认");
        List<Camera.Size> sizes = mPreview.getSupportedPreivewSizes();
        for (Camera.Size size : sizes) {
            sizeAdapter.add(size.width + " x " + size.height);
        }
    }


    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        mPreview.stop();
        mainlayout.removeView(mPreview);
        mPreview = null;
    }


    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            Log.d(TAG, i + "+" + b);

            if (null != mPreview && mPreview.isZoomSupport()) {
                mPreview.setZoom(i);
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.takephoto_btn:
                if (isLongPress) {
                    mPreview.takePacture(mPicture);
                }
                break;

        }
    }


    @Override
    public boolean onLongClick(View view) {

        switch (view.getId()) {
            case R.id.takephoto_btn:
                Log.d(TAG, "onLongClick");
                mPreview.autoFocus();
                Toast.makeText(getActivity(), "开始聚焦", Toast.LENGTH_SHORT).show();
                isLongPress = true;
                break;

        }
        return false;
    }


    /**
     * 照相回调
     */
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "PictureCallback＝" + data.length);
            if (saveFile == null) {

                return;
            }
            FileOutputStream outSteam = null;

            try {

                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                String times = format.format((new Date()));
                String picpath = saveFile.getPath() + File.separator + times + ".jpg";
                Log.d(TAG, picpath);

                outSteam = new FileOutputStream(picpath);
                outSteam.write(data);
                outSteam.close();
                Toast.makeText(getActivity(), "保存完成", Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();

            } catch (IOException e) {

                e.printStackTrace();
            }

            mPreview.startPreView();
        }

    };


    /**
     * 创建保存照片的文件夹
     */
    public void createSaveFile() {
        if (!hasSDCard()) {
            Toast.makeText(getActivity(), "没有找到SD卡", Toast.LENGTH_SHORT).show();
            return;
        }

        String savepath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "camerademo";
        Log.d(TAG, "path＝" + savepath);
        saveFile = new File(savepath);
        if (!saveFile.exists()) {
            saveFile.mkdir();
        }

    }


    /**
     * 判断手机是否有SD卡。
     *
     * @return 有SD卡返回true，没有返回false。
     */
    public static boolean hasSDCard() {
        return Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState());
    }


    @Override
    public void onPreviewReady() {

        int progress = zoomSeekbar.getProgress();
        int zoom = (progress <= mPreview.getMaxZoom() ? progress : (progress = 0));
        if (null != mPreview && mPreview.isZoomSupport()) {
            mPreview.setZoom(progress);
        }
    }


    /**
     * 变焦每次增加一
     */
    public void zoomUp() {
        showSeekbar();
        int progress = zoomSeekbar.getProgress();
        if (progress < zoomSeekbar.getMax()) {
            progress++;
            zoomSeekbar.setProgress(progress);
        }
    }

    /**
     * 变焦每次减小1
     */
    public void zoomDown() {
        showSeekbar();
        int progress = zoomSeekbar.getProgress();
        if (progress > 0) {
            progress--;
            zoomSeekbar.setProgress(progress);
        }
    }


}
