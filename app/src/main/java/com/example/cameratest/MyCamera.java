package com.example.cameratest;

import android.app.Activity;
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
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyCamera extends Activity {

    private CameraPreview camPreview;
    private LinearLayout mainLayout;

    private Handler mHandler;

    ArrayList<LineDataSet> dataSets;
    ArrayList<String> xVals;
    LineChart chart;

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
        camPreview = new CameraPreview(640, 480);

        camHolder.addCallback(camPreview);
        //  camHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mainLayout = (LinearLayout) findViewById(R.id.linearLayout1);
        mainLayout.addView(camView, new LayoutParams(640, 480));
        chart = (LineChart) findViewById(R.id.chart);
        initChart();
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        mainLayout.removeAllViews();
                        ImageView v = new ImageView(MyCamera.this);
                        if(camPreview.mBitMap == null){
                            Toast.makeText(MyCamera.this,"fdsafdsafa",Toast.LENGTH_SHORT).show();
                        }
                        v.setImageBitmap(camPreview.mBitMap);
                        mainLayout.addView(v);
                }
            }
        };

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
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int X = (int) event.getX();
            if (X >= 640)
                mHandler.postDelayed(TakePicture, 300);
            else
                camPreview.CameraStartAutoFocus();
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
            Toast.makeText(MyCamera.this, MyDirectory_path, Toast.LENGTH_SHORT).show();
            PictureFileName = MyDirectory_path + "/MyPicture.jpg";
            camPreview.CameraTakePicture(PictureFileName);
            Message message = new Message();
            message.what = 1;
            mHandler.sendMessage(message);
        }
    };

    private void initChart() {
        dataSets = new ArrayList<>();
        xVals = new ArrayList<>();

        LineChart chart = (LineChart) findViewById(R.id.chart);
        YAxis leftAxis = chart.getAxisLeft();
        LimitLine ll = new LimitLine(140f, "Critical Blood Pressure");
        ll.setLineColor(Color.RED);
        ll.setLineWidth(4f);
        ll.setTextColor(Color.BLACK);
        ll.setTextSize(12f);
        leftAxis.addLimitLine(ll);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.RED);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);

        Float[] _float = {2.3f, 4.5f, 3.3f, 5.7f, 1.3f, 8.7f};
        List<Float> list = new ArrayList<Float>();
        Collections.addAll(list, _float);
        setParameters(list);

        ArrayList<String> x = new ArrayList<String>();
        x.add("1.Q");
        x.add("2.Q");
        x.add("3.Q");
        x.add("4.Q");
        x.add("5.Q");
        x.add("6.Q");
        setxVals(x);

        drawPlot();
    }

    public void setParameters(List<Float> list) {
        dataSets.clear();
        ArrayList<Entry> valsComp = new ArrayList<Entry>();
        for (int i = 0; i < list.size(); i++) {
            valsComp.add(new Entry(list.get(i), i));
        }
        LineDataSet setComp = new LineDataSet(valsComp, "中心光谱");
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


}
