package com.example.cameratest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
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

    private CameraPreview camPreview;
    private LinearLayout mainLayout;

    private Handler mHandler;

    ArrayList<LineDataSet> dataSets;
    ArrayList<String> xVals;
    LineChart chart;
    boolean isTakenPhoto = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set this SPK Full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //Set this APK no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        SurfaceView camView = new SurfaceView(this);
        final SurfaceHolder camHolder = camView.getHolder();
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        if (camPreview.mBitMapGray == null) {
                            Toast.makeText(MyCamera.this, "BitMap null", Toast.LENGTH_SHORT).show();
                        } else {
                            mainLayout.removeAllViews();
                            ImageView v = new ImageView(MyCamera.this);
                            v.setImageBitmap(camPreview.mBitMap);
                            mainLayout.addView(v);
                            List<Float> list = MyCamera.this.imageProcess(camPreview.mBitMapGray);
                            MyCamera.this.setParameters(list);

                            ArrayList<String> xMarks = new ArrayList<>();
                            for (int i = 0; i < list.size(); i++) {
                                xMarks.add(String.valueOf(i));
                            }

                            MyCamera.this.setxVals(xMarks);
                            MyCamera.this.drawPlot();
                        }

                }
            }
        };
        camPreview = new CameraPreview(this, mHandler, 320, 240);

        camHolder.addCallback(camPreview);
        //  camHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mainLayout = (LinearLayout) findViewById(R.id.linearLayout1);
        mainLayout.addView(camView, new LayoutParams(320, 240));
        chart = (LineChart) findViewById(R.id.chart);
        initChart();


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
    public boolean onTouchEvent(MotionEvent event) {
        if(!isTakenPhoto) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                int Y = (int) event.getY();
                if (Y <= getWindowManager().getDefaultDisplay().getHeight())
                    mHandler.postDelayed(TakePicture, 300);
                else
                    camPreview.CameraStartAutoFocus();
            }
            isTakenPhoto = true;
        }
        return true;
    }

    private Runnable TakePicture = new Runnable() {
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        String MyDirectory_path = extStorageDirectory;
        String PictureFileName;


        public void run() {
            File file = new File(MyDirectory_path);
            if (!file.exists())
                file.mkdirs();
            PictureFileName = MyDirectory_path + "/MyPicture.jpg";
            camPreview.CameraTakePicture(PictureFileName);
        }
    };

    private void initChart() {
        dataSets = new ArrayList<>();
        xVals = new ArrayList<>();

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
//
//        Float[] _float = {2.3f, 4.5f, 3.3f, 5.7f, 1.3f, 8.7f};
//        List<Float> list = new ArrayList<>();
//        Collections.addAll(list, _float);
//        setParameters(list);
//
//        ArrayList<String> x = new ArrayList<>();
//        for(int i = 0; i < 8; i++){
//            x.add(String.valueOf(i));
//        }
//        setxVals(x);
//        drawPlot();
    }

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

        dataSets.add(setComp);
    }

    public void setxVals(ArrayList<String> list) {
        xVals.clear();
        xVals = list;
    }

    public void drawPlot() {
        LineData data = new LineData(xVals, dataSets);
        chart.setData(data);
        chart.invalidate();
    }

    public List<Float> imageProcess(Bitmap bitmap) {
        int m = bitmap.getWidth();
        int n = bitmap.getHeight();
        int y_sum = 0;
        int t = 1;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (Color.blue(bitmap.getPixel(i, j)) > 70) {
                    t++;
                    y_sum += j;
                }
            }
        }
        int y_moyen = y_sum / t;
        System.err.println(y_moyen);
        List<Float> list = new ArrayList<>();
        for (int i = 0; i < m; i++) {
            float temp = Color.blue(bitmap.getPixel(i, y_moyen));
            list.add(temp);
        }
        return list;
    }
}
