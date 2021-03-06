package com.example.cameratest;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MyCamera extends Activity {

    //预览组件
    private CameraPreview camPreview;
    private LinearLayout mainLayout;

    private Handler mHandler;

    //曲线图需要使用的值
    ArrayList<LineDataSet> dataSets;

    //横坐标标示
    ArrayList<String> xVals;

    //图表
    LineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set this SPK Full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //Set this APK no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //加载布局
        setContentView(R.layout.activity_main);

        final int height = getResources().getDimensionPixelSize(R.dimen.chart_height);
        final int width = getResources().getDimensionPixelSize(R.dimen.chart_width);
        //允许接受拍照返回后数据，绘制图表
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        if (camPreview.mBitMapGray == null) {
                            Toast.makeText(MyCamera.this, "BitMap null", Toast.LENGTH_SHORT).show();
                            camPreview = null;
                        } else {
//                            mainLayout.removeAllViews();
//                            ImageView v = new ImageView(MyCamera.this);
//                            v.setImageBitmap(camPreview.mBitMapGray);
//                            mainLayout.addView(v, new LayoutParams(width, height));

                            drawImagePlot(camPreview.mBitMapGray);

                            camPreview = null;
                        }
                        break;
                    default:
                        camPreview = null;

                }
            }
        };

        MyInitCameraTask myInitCameraTask = new MyInitCameraTask(width, height, this, mHandler);
        Object[] myObjects = null;
        myInitCameraTask.execute(myObjects);


//        camPreview = new CameraPreview(this, mHandler, width, height, getCameraInstance());
//        //  camHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//
//        mainLayout = (LinearLayout) findViewById(R.id.linearLayout1);
//        mainLayout.addView(camPreview, new LayoutParams(width, height));

        //初始化图表
        chart = (LineChart) findViewById(R.id.chart);
        initChart();


    }

    class MyInitCameraTask extends AsyncTask {

        int width;
        int height;
        Context context;
        Handler handler;

        MyInitCameraTask(int width, int height, Context context, Handler handler) {
            this.width = width;
            this.height = height;
            this.context = context;
            this.handler = handler;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            //初始化相机预览界面

            Camera camera = null;
            while (camera == null) {
                if (checkCameraHardware(MyCamera.this)) {
                    camera = getCameraInstance();
                }
                if (camera == null) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return camera;
        }

        @Override
        protected void onPostExecute(Object o) {


            Camera camera = (Camera) o;
            camPreview = new CameraPreview(context, handler, width, height, camera);
            //  camHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

            mainLayout = (LinearLayout) findViewById(R.id.linearLayout1);
            mainLayout.addView(camPreview, new LayoutParams(width, height));


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.camPreview = null;
    }

    //接触屏幕后调用预览界面的拍照函数
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (camPreview != null && camPreview.mCamera != null && camPreview.TakePicture) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                int Y = (int) event.getY();
                if (Y <= getWindowManager().getDefaultDisplay().getHeight())
                    mHandler.postDelayed(TakePicture, 300);
            }

        }
        return true;
    }

    private Runnable TakePicture = new Runnable() {
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        String MyDirectory_path = extStorageDirectory;
        String PictureFileName;

        public void run() {
//           保存图片的相关设置，这里不需要
            File file = new File(MyDirectory_path);
            if (!file.exists())
                file.mkdirs();
            PictureFileName = MyDirectory_path + "/MyPicture.jpg";

            camPreview.CameraTakePicture(PictureFileName);
        }
    };


    //初始化图表
    private void initChart() {
        dataSets = new ArrayList<LineDataSet>();
        xVals = new ArrayList<String>();

        //设置X轴Y轴样式，详细见Android MPChart开源组件
        LineChart chart = (LineChart) findViewById(R.id.chart);
        YAxis leftAxis = chart.getAxisLeft();
//        LimitLine ll = new LimitLine(140f, "Critical Blood Pressure");
//        ll.setLineColor(Color.RED);
//        ll.setLineWidth(4f);
//        ll.setTextColor(Color.BLACK);
//        ll.setTextSize(12f);
//        leftAxis.addLimitLine(ll);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(true);

    }

    //传入一组Y轴数据，X轴默认从1开始
    public void setParameters(List<Float> list) {
        dataSets.clear();
        ArrayList<Entry> valsComp = new ArrayList<Entry>();
        for (int i = 0; i < list.size(); i++) {
            valsComp.add(new Entry(list.get(i), i));
        }
        LineDataSet setComp = new LineDataSet(valsComp, "Author: LEI Yu");
        setComp.setDrawFilled(false);
        setComp.setDrawCircleHole(false);
        setComp.setDrawCircles(false);
        setComp.setColor(Color.BLUE);

        dataSets.add(setComp);
    }

    //设置X轴显示的数据，与上面Y的数据对应
    public void setxVals(ArrayList<String> list) {
        xVals.clear();
        xVals = list;
    }

    //设置好参数后绘制图表
    public void drawPlot() {
        LineData data = new LineData(xVals, dataSets);
        chart.setData(data);
        chart.invalidate();
    }

    //图像处理算法，提取图片对应轴线的亮度值 0-255
    public List<Float> imageProcess(Bitmap bitmap) {
        int m = bitmap.getWidth();
        int n = bitmap.getHeight();
        int y_sum = 0;
        int t = 1;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                //阙值为70效果还行
                if (Color.blue(bitmap.getPixel(i, j)) > 70) {
                    t++;
                    y_sum += j;
                }
            }
        }
        int y_moyen = y_sum / t;
        Log.d("Y_MOYEN", String.valueOf(y_moyen));
        List<Float> list = new ArrayList<Float>();
        int start = (int)(146f/400f*m);
        int end = (int)(236f/400f*m);
        for (int i = start; i < end; i++) {
            float temp = Color.blue(bitmap.getPixel(i, y_moyen));
            list.add(temp);
        }
        return list;
    }

    public void drawImagePlot(Bitmap bitmap){
        List<Float> list = imageProcess(bitmap);
        setParameters(list);
        ArrayList<String> stringArray = new ArrayList<String>();
        float step = 340f/list.size();
        for(int i=0; i < list.size();i++){
            stringArray.add(String.valueOf(720-i*step));
        }
        setxVals(stringArray);
        drawPlot();
    }

    //检查相机
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(0); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Log.d("Debug", "camera open 0 error" + e.getMessage());
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (camPreview != null && camPreview.mCamera != null && camPreview.isCameraOpen == true) {
            camPreview.mCamera.release();
            camPreview.isCameraOpen = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camPreview != null && camPreview.mCamera != null && camPreview.isCameraOpen) {
            camPreview.mCamera.stopPreview();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (camPreview != null && camPreview.mCamera != null && camPreview.isCameraOpen == true) {
            camPreview.mCamera.release();
            camPreview.isCameraOpen = false;
        }
    }
}
