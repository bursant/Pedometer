package pedometer.droid;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
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
import pedometer.droid.algorithm.common.IDetector;
import pedometer.droid.algorithm.common.IDetectorListener;
import pedometer.droid.algorithm.common.MotionVector;
import pedometer.droid.algorithm.fall.FallDetector;
import pedometer.droid.algorithm.step.ExponentialMovingAverage;
import pedometer.droid.algorithm.step.StepDetector;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

public class DroidMain extends RoboActivity implements SensorEventListener, IDetectorListener {

    public static final int PLEASE_WAIT_DIALOG = 1;

    @InjectView(R.id.start)
    private Button startButton;

    @InjectView(R.id.stepDetector)
    private TextView stepDetectorTextView;

    @InjectView(R.id.fallDetector)
    private TextView fallDetectorTextView;

    @InjectView(R.id.chart)
    private LinearLayout chartLayout;

    private GraphicalView mChart;

    private final XYMultipleSeriesDataset mDataSet = new XYMultipleSeriesDataset();

    private final XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();

    private XYSeries mCurrentSeries;

    private XYSeriesRenderer mCurrentRenderer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DroidHandler.main = this;
        setContentView(R.layout.main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (DroidHandler.avg == null)
            DroidHandler.avg = new ExponentialMovingAverage(DroidPreference.getAlpha());

        if (DroidHandler.fallDetector == null)
            DroidHandler.fallDetector = new FallDetector();
        if (DroidHandler.stepDetector == null)
            DroidHandler.stepDetector = new StepDetector(DroidHandler.avg);

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

        if (DroidHandler.started) {
            start();
            startButton.setText("Stop");
        }


        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!DroidHandler.started) {
                    register();
                    start();
                    DroidHandler.startTimeStamp = System.currentTimeMillis();
                    DroidHandler.started = true;
                    DroidMain.this.startButton.setText("Stop");
                    Toast.makeText(DroidMain.this, "Started", Toast.LENGTH_SHORT).show();
                } else {
                    stop();
                    unregister();
                    DroidHandler.started = false;
                    DroidMain.this.startButton.setText("Start");
                    Toast.makeText(DroidMain.this, "Stopped", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        stop();
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

    private void register() {
        DroidHandler.detectorManager.registerDetector(DroidHandler.fallDetector);
        DroidHandler.detectorManager.registerDetector(DroidHandler.stepDetector);
        DroidHandler.detectorManager.registerListener(this);
    }

    private void start() {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(DroidHandler.detectorManager, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void stop() {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.unregisterListener(DroidHandler.detectorManager);
        sensorManager.unregisterListener(this);
    }

    private void unregister() {
        DroidHandler.detectorManager.unregisterDetector(DroidHandler.fallDetector);
        DroidHandler.detectorManager.unregisterDetector(DroidHandler.stepDetector);
        DroidHandler.detectorManager.unregisterListener(this);
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        switch (dialogId) {
            case PLEASE_WAIT_DIALOG:
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle("Connecting to server");
                dialog.setMessage("Please wait....");
                dialog.setCancelable(true);
                return dialog;

            default:
                return super.onCreateDialog(dialogId);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Double vector = MotionVector.compute(event.values[0], event.values[1], event.values[2]);

        long timeStamp = System.currentTimeMillis();

        mRenderer.setXAxisMin((timeStamp - DroidHandler.startTimeStamp) / 1000.0 - 10.0);
        mRenderer.setXAxisMax((timeStamp - DroidHandler.startTimeStamp) / 1000.0);

        if (mCurrentRenderer != null)
            mCurrentSeries.add((timeStamp - DroidHandler.startTimeStamp) / 1000.0, vector);
        if (mChart != null)
            mChart.repaint();

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    public void notifyCountChange(final IDetector detector, final Integer count) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (detector != null) {
                    if (detector.equals(DroidHandler.fallDetector)) {
                        DroidMain.this.fallDetectorTextView.setText(count + " falls.");
                    } else if (detector.equals(DroidHandler.stepDetector)) {
                        DroidMain.this.stepDetectorTextView.setText(count + " steps.");
                    }
                }
            }
        });
    }
}
