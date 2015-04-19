package com.example.cameratest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback,
        Camera.PreviewCallback {

    private int PreviewSizeWidth;
    private int PreviewSizeHeight;
    Camera mCamera;
    boolean TakePicture = false;
    boolean isCameraOpen;
    private String NowPictureFileName;
    Bitmap mBitMapGray;
    Bitmap mBitMap;
    Context mContext;
    Handler mHandler;
    SurfaceHolder mHolder;


    //初始化相机预览界面，传入时间像素尺寸
    public CameraPreview(Context context, Handler handler, int PreviewWidth, int PreviewHeight, Camera camera) throws NullPointerException {
        super(context);
        if (camera == null) {
            throw new NullPointerException("Camera null");
        } else {
            isCameraOpen = true;
        }
        PreviewSizeWidth = PreviewWidth;
        PreviewSizeHeight = PreviewHeight;
        mContext = context;
        mHandler = handler;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
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
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        try {
            Camera.Parameters parameters;
            parameters = mCamera.getParameters();

            List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
            Camera.Size optimalSize = getOptimalPreviewSize(sizes, PreviewSizeWidth, PreviewSizeHeight);

            // Set the camera preview size
            parameters.setPreviewSize(optimalSize.width, optimalSize.height);
            // Set the take picture size, you can set the large size of the camera
            // supported.
            parameters.setPictureSize(optimalSize.width, optimalSize.height);

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
            TakePicture = true;
        } catch (Exception e) {
            Log.d("Surface change2", "Surface change2" + e.getMessage());
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        try {
            if (!isCameraOpen)
                return;
            mCamera.setPreviewDisplay(holder);
            // mCamera.setPreviewCallback(this);
            mHolder = holder;
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d("Surface Created Error", "Surface Created Error" + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        // TODO Auto-generated method stub
//        mCamera.setPreviewCallback(null);
//        mCamera.stopPreview();
        //mCamera.release();
//        if(mCamera != null){
//            mCamera.release();
//        }
    }

    @Override
    public void onPreviewFrame(byte[] arg0, Camera arg1) {
        // TODO Auto-generated method stub

    }

    // Take picture interface
    public void CameraTakePicture(String FileName) {

        NowPictureFileName = FileName;
        // mCamera.autoFocus(myAutoFocusCallback);

        if (TakePicture) {
            Log.d("huuh", "ccccccccccccccccccccccccccccccc");
            System.err.print("nhiuhiuhniuhiuhiuhiuhiuhuiuhiuopkokoijiji");
            TakePicture = false;
            //  mCamera.stopPreview();
            Log.d("huuh", "uuuuuuuuuuuuuuuuu");

            mCamera.takePicture(shutterCallback, rawPictureCallback,
                    jpegPictureCallback);
            Log.d("huuh", "yyyyyyyyyy");

        }
    }


//    AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback() {
//        public void onAutoFocus(boolean arg0, Camera NowCamera) {
//            if (TakePicture) {
//                NowCamera.stopPreview();// fixed for Samsung S2
//                NowCamera.takePicture(shutterCallback, rawPictureCallback,
//                        jpegPictureCallback);
//                TakePicture = true;
//            }
//        }
//    };

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
            Log.d("huuh", "dddddddddddddddddddd");
            //允许储存照片
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(NowPictureFileName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            mBitMap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            mBitMapGray = toGrayScale(mBitMap);

            Message message = new Message();
            message.what = 1;
            mHandler.sendMessage(message);
            mCamera.stopPreview();
            mCamera.release();
            isCameraOpen = false;
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

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;
        if (sizes == null)
            return null;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
}
