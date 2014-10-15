package com.example.camera360.testcamera;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
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
        View.OnClickListener, View.OnLongClickListener, AdapterView.OnItemSelectedListener {


    public static final String SAVE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "camerademo";

    public static final String NAME_SIGN = "camerademo";

    private String TAG = "CameraFragment";

    private RelativeLayout mainlayout;
    private ImageButton takePhotoBtn;
    private ResizableCameraPreview mPreview;

    private MySeekBar mySeekBar;

    private File saveFile;
    private boolean isLongPress = false;
    //预览尺寸Adapter
    private ArrayAdapter<String> sizeAdapter;
    private Spinner sizeSpinner;

    private Button photoButton;

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

        mySeekBar = (MySeekBar) view.findViewById(R.id.myseekbar);
        mySeekBar.setSeekBarListener(new MySeekBar.OnSeekListener() {
            @Override
            public void onProgressChanged(MySeekBar seekBar, int i) {
                if (null != mPreview && mPreview.isZoomSupport()) {
                    mPreview.setZoom(i);
                }
            }
        });
        mySeekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                showSeekbar();
                return false;
            }
        });

        showSeekbar();

        sizeSpinner = (Spinner) view.findViewById(R.id.size_spinner);
        sizeAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item);
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sizeSpinner.setAdapter(sizeAdapter);
        sizeSpinner.setOnItemSelectedListener(this);

        photoButton = (Button) view.findViewById(R.id.photo_btn);
        photoButton.setOnClickListener(this);
    }

    /**
     * 现实便教条两秒
     */
    public void showSeekbar() {
        handler.removeCallbacks(runnable);
        mySeekBar.setVisibility(View.VISIBLE);
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
            mySeekBar.setVisibility(View.GONE);
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

        mPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSeekbar();
            }
        });
        RelativeLayout.LayoutParams previewLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        mainlayout.addView(mPreview, 0, previewLayoutParams);
        mySeekBar.setMax(mPreview.getMaxZoom());
        mySeekBar.setProgress(0);

        sizeAdapter.clear();
        sizeAdapter.add("默认");
        List<Camera.Size> sizes = mPreview.getSupportedPreivewSizes();
        for (Camera.Size size : sizes) {
            sizeAdapter.add(size.width + " x " + size.height);
        }
        sizeAdapter.notifyDataSetChanged();
        sizeSpinner.setAdapter(sizeAdapter);
    }


    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        mPreview.stop();
        mainlayout.removeView(mPreview);
        mPreview = null;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.takephoto_btn:
                if (isLongPress && null != mPreview) {
                    mPreview.takePacture(mPicture);
                    isLongPress = false;
                }
                break;
            case R.id.photo_btn:
                Intent intent = new Intent(getActivity(), PhotosActivity.class);
                startActivity(intent);

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

            new SaveImageTask().execute(data);
            mPreview.startPreView();
        }

    };


    private class SaveImageTask extends AsyncTask<byte[], Void, Void> {

        @Override
        protected Void doInBackground(byte[]... data) {
            FileOutputStream outStream = null;

            // Write to SD Card
            try {
                createSaveFile();


                SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
                String times = format.format((new Date()));

                String fileName = NAME_SIGN + "_" + times;

                File outFile = new File(saveFile, fileName + ".jpg");

                outStream = new FileOutputStream(outFile);
                outStream.write(data[0]);
                outStream.flush();
                outStream.close();


                MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), outFile.getPath(), fileName, NAME_SIGN);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
            return null;
        }
    }


    /**
     * 创建保存照片的文件夹
     */
    public void createSaveFile() {
        if (!hasSDCard()) {
            Toast.makeText(getActivity(), "没有找到SD卡", Toast.LENGTH_SHORT).show();
            return;
        }


        Log.d(TAG, "path＝" + SAVE_PATH);
        saveFile = new File(SAVE_PATH);
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


    /**
     * 变焦每次增加一
     */
    public void zoomUp() {
        showSeekbar();
        int progress = mySeekBar.getProgress();
        if (progress < mySeekBar.getMax()) {
            progress++;
            mySeekBar.setProgress(progress);
        }
    }

    /**
     * 变焦每次减小1
     */
    public void zoomDown() {
        showSeekbar();
        int progress = mySeekBar.getProgress();
        if (progress > 0) {
            progress--;
            mySeekBar.setProgress(progress);
        }
    }


}
