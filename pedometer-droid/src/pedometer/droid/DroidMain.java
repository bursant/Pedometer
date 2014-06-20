package pedometer.droid;

import android.content.*;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.text.format.Time;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import pedometer.app.R;
import pedometer.droid.algorithm.common.DetectorManager;
import pedometer.droid.algorithm.common.MotionVector;
import pedometer.droid.algorithm.fall.FallDetector;
import pedometer.droid.algorithm.step.StepDetector;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

public class DroidMain extends RoboActivity implements SensorEventListener {

    @InjectView(R.id.start)
    private Button startButton;

    @InjectView(R.id.clear)
    private Button clearButton;

    @InjectView(R.id.share)
    private Button shareButton;

    @InjectView(R.id.stepDetector)
    private TextView stepDetectorTextView;

    @InjectView(R.id.fallDetector)
    private TextView fallDetectorTextView;

    @InjectView(R.id.chart)
    private LinearLayout chartLayout;

    private long startTimeStamp;

    private GraphicalView mChart;

    private final XYMultipleSeriesDataset mDataSet = new XYMultipleSeriesDataset();

    private final XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();

    private XYSeries mCurrentSeries;

    private XYSeriesRenderer mCurrentRenderer;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int steps = bundle.getInt(DroidService.STEPS);
                int falls = bundle.getInt(DroidService.FALLS);

                DroidMain.this.stepDetectorTextView.setText(steps + " steps.");
                DroidMain.this.fallDetectorTextView.setText(falls + " falls.");
            }
        }
    };

    private View.OnClickListener startButtonListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            if (droidBinder != null && droidBinder.getService().isStarted()) {
                stopService();
                DroidMain.this.startButton.setText("Start");
                Toast.makeText(DroidMain.this, "Stopped", Toast.LENGTH_SHORT).show();
            } else {
                startService();
                DroidMain.this.startButton.setText("Stop");
                Toast.makeText(DroidMain.this, "Started", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private View.OnClickListener clearButtonListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            if (droidBinder != null) {
                DetectorManager detectorManager = droidBinder.getService().getDetectorManager();
                detectorManager.resetCounters();
                DroidMain.this.stepDetectorTextView.setText("0 steps.");
                DroidMain.this.stepDetectorTextView.setText("0 falls.");
            }
        }
    };

    private View.OnClickListener shareButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Time now = new Time();
            now.setToNow();

            String[] loinc = new String[2];
            loinc[0] = "55423-8";
            loinc[1] = "54854-5";

            String[] values = new String[2];
            if (droidBinder != null) {
                DetectorManager detectorManager = droidBinder.getService().getDetectorManager();
                StepDetector stepDetector = droidBinder.getService().getStepDetector();
                FallDetector fallDetector = droidBinder.getService().getFallDetector();
                values[0] = String.valueOf(detectorManager.getCountForDetector(stepDetector));
                values[1] = String.valueOf(detectorManager.getCountForDetector(fallDetector));
            }

            Bundle bundle = new Bundle();
            bundle.putString("Action", "meddev.MEASUREMENT");
            bundle.putString("Data", "device://Pedometer");
            bundle.putString("Name", "Pedometer");
            bundle.putString("Date", now.toString());
            bundle.putString("Action", "meddev.MEASUREMENT");
            bundle.putStringArray("LOINC_LIST", loinc);
            bundle.putStringArray("LOINC_VALUES", values);

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("meddev.MEASUREMENT");
            broadcastIntent.setData(Uri.parse("device://Pedometer"));
            broadcastIntent.putExtras(bundle);
            sendBroadcast(broadcastIntent);

            Toast.makeText(getApplicationContext(), "Shared: " + values[0] + " steps , " + values[1] + " falls.", Toast.LENGTH_SHORT).show();
        }
    };

    private DroidService.DroidBinder droidBinder;

    private final ServiceConnection droidServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            droidBinder = (DroidService.DroidBinder) iBinder;

            if (droidBinder.getService().isStarted()) {
                startMeasuring();
                startButton.setText("Stop");
            }

            Toast.makeText(getApplicationContext(), "Service connected.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            droidBinder = null;

            stopMeasuring();

            Toast.makeText(getApplicationContext(), "Service disconnected.", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent i = new Intent(this, DroidService.class);
        if (bindService(i, droidServiceConnection, 0)) {
            Toast.makeText(getApplicationContext(), "Pedometer bound.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Cannot bound to Pedometer.", Toast.LENGTH_SHORT).show();
        }

        startButton.setOnClickListener(startButtonListener);
        clearButton.setOnClickListener(clearButtonListener);
        shareButton.setOnClickListener(shareButtonListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mCurrentSeries == null) {
            mCurrentSeries = new XYSeries("Vector");

            mDataSet.addSeries(mCurrentSeries);
        }

        if (mCurrentRenderer == null) {
            mCurrentRenderer = new XYSeriesRenderer();

            mRenderer.addSeriesRenderer(mCurrentRenderer);
            mRenderer.setLabelsTextSize(25);
            mRenderer.setLegendTextSize(25);
        }

        if (mChart == null) {
            mChart = ChartFactory.getCubeLineChartView(this, mDataSet, mRenderer, 0.3f);
            chartLayout.addView(mChart);
        }

        registerReceiver(receiver, new IntentFilter(DroidService.NOTIFICATION));
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(receiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbindService(droidServiceConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.preferences: {
                Intent myIntent = new Intent(this, DroidPreference.class);
                startActivity(myIntent);
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private void startMeasuring() {
        startTimeStamp = System.currentTimeMillis();
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void stopMeasuring() {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.unregisterListener(this);
    }

    private void startService() {
        Intent pedoService = new Intent(this, DroidService.class);
        startService(pedoService);
    }

    private void stopService() {
        Intent pedoService = new Intent(this, DroidService.class);
        stopService(pedoService);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Double vector = MotionVector.compute(event.values[0], event.values[1], event.values[2]);

        long timeStamp = System.currentTimeMillis();

        mRenderer.setXAxisMin((timeStamp - startTimeStamp) / 1000.0 - 10.0);
        mRenderer.setXAxisMax((timeStamp - startTimeStamp) / 1000.0);

        if (mCurrentRenderer != null)
            mCurrentSeries.add((timeStamp - startTimeStamp) / 1000.0, vector);

        if (mChart != null)
            mChart.repaint();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
