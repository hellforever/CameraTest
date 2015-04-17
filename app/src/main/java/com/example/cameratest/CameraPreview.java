package com.example.cameratest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback,
        Camera.PreviewCallback {

    private int PreviewSizeWidth;
    private int PreviewSizeHeight;
    private Camera mCamera;
    private boolean TakePicture;
    // private String NowPictureFileName;
    Bitmap mBitMapGray;
    Bitmap mBitMap;
    Context mContext;
    Handler mHandler;
    SurfaceHolder mHolder;


    //初始化相机预览界面，传入时间像素尺寸
    public CameraPreview(Context context, Handler handler, int PreviewWidth, int PreviewHeight, Camera camera) {
        super(context);
        PreviewSizeWidth = PreviewWidth;
        PreviewSizeHeight = PreviewHeight;
        mContext = context;
        mHandler = handler;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mCamera = camera;

    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        // 每次相机预览界面变动，都会调用，包括初始化时，极易出错

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        try {
            //  mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        try {
            Parameters parameters;
            parameters = mCamera.getParameters();
            // Set the camera preview size
            parameters.setPreviewSize(PreviewSizeWidth, PreviewSizeHeight);
            // Set the take picture size, you can set the large size of the camera
            // supported.
            parameters.setPictureSize(PreviewSizeWidth, PreviewSizeHeight);
            // Turn on the camera flash.
            String NowFlashMode = parameters.getFlashMode();
            if (NowFlashMode != null)
                parameters.setFlashMode(Parameters.FLASH_MODE_ON);
            // Set the auto-focus.
            String NowFocusMode = parameters.getFocusMode();
            if (NowFocusMode != null)
                parameters.setFocusMode("auto");
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            Log.d("Surface change1", "Surface change1" + e.getMessage());
        }

        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d("Surface change2", "Surface change2" + e.getMessage());
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
           // mCamera.setPreviewCallback(this);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d("Surface Created Error", "Surface Created Error" + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        // TODO Auto-generated method stub
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    @Override
    public void onPreviewFrame(byte[] arg0, Camera arg1) {
        // TODO Auto-generated method stub

    }

    // Take picture interface
    public void CameraTakePicture(String FileName) {
        TakePicture = true;
        //NowPictureFileName = FileName;
        // mCamera.autoFocus(myAutoFocusCallback);
        mCamera.stopPreview();// fixed for Samsung S2
        mCamera.takePicture(shutterCallback, rawPictureCallback,
                jpegPictureCallback);
    }


    AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback() {
        public void onAutoFocus(boolean arg0, Camera NowCamera) {
            if (TakePicture) {
                NowCamera.stopPreview();// fixed for Samsung S2
                NowCamera.takePicture(shutterCallback, rawPictureCallback,
                        jpegPictureCallback);
                TakePicture = true;
            }
        }
    };

    ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() {
            // Just do nothing.
        }
    };

    PictureCallback rawPictureCallback = new PictureCallback() {
        public void onPictureTaken(byte[] arg0, Camera arg1) {
            // Just do nothing.
        }
    };

    CustomPictureCallback jpegPictureCallback = new CustomPictureCallback(mHandler);

    class CustomPictureCallback implements PictureCallback {
        Handler handler;

        CustomPictureCallback(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            mBitMap = BitmapFactory.decodeByteArray(data, 0,
                    data.length);
            //允许储存照片
//            FileOutputStream out = null;
//            try {
//                out = new FileOutputStream(NowPictureFileName);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//            bitmap2.compress(Bitmap.CompressFormat.JPEG, 90, out);
            mBitMapGray = toGrayScale(mBitMap);

            Message message = new Message();
            message.what = 1;
            mHandler.sendMessage(message);
        }
    }

    //将彩色图转化为灰度图
    public Bitmap toGrayScale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayScale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayScale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayScale;
    }


}
