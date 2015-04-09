package com.example.cameratest;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
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
import java.util.List;

public class MyCamera extends Activity {

    private CameraPreview camPreview;
    private LinearLayout mainLayout;

    private Handler mHandler = new Handler(Looper.getMainLooper());

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
        SurfaceHolder camHolder = camView.getHolder();
        camPreview = new CameraPreview(640, 480);

        camHolder.addCallback(camPreview);
        //  camHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mainLayout = (LinearLayout) findViewById(R.id.linearLayout1);
        mainLayout.addView(camView, new LayoutParams(640, 480));

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
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int X = (int) event.getX();
            if (X >= 640)
                mHandler.postDelayed(TakePicture, 300);
            else
                camPreview.CameraStartAutoFocus();
        }
        return true;
    }

    ;

    private Runnable TakePicture = new Runnable() {
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        String MyDirectory_path = extStorageDirectory;
        String PictureFileName;

        public void run() {
            File file = new File(MyDirectory_path);
            if (!file.exists())
                file.mkdirs();
            Toast.makeText(MyCamera.this,MyDirectory_path,Toast.LENGTH_SHORT).show();
            PictureFileName = MyDirectory_path + "/MyPicture.jpg";
            camPreview.CameraTakePicture(PictureFileName);
        }
    };

    private void initChart() {
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

        ArrayList<Entry> valsComp1 = new ArrayList<Entry>();
        Entry c1e1 = new Entry(100.000f, 0); // 0 == quarter 1
        valsComp1.add(c1e1);
        Entry c1e2 = new Entry(50.000f, 1); // 1 == quarter 2 ...
        valsComp1.add(c1e2);

        LineDataSet setComp1 = new LineDataSet(valsComp1, "Company 1");

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(setComp1);

        ArrayList<String> xVals = new ArrayList<String>();
        xVals.add("1.Q");
        xVals.add("2.Q");
        xVals.add("3.Q");
        xVals.add("4.Q");

        LineData data = new LineData(xVals, dataSets);
        chart.setData(data);
        chart.invalidate();
    }

    public void setParameters(List<Float> list){
        ArrayList<Entry> valsComp1 = new ArrayList<Entry>();
        for(int i=0;i<list.size();i++){
            valsComp1.add(new Entry(list.get(i),i));
        }
        LineDataSet setComp1 = new LineDataSet(valsComp1, "Company 1");
       // dataSets.add(setComp1);
    }

}
