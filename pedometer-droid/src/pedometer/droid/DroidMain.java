package pedometer.droid;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import pedometer.common.connector.client.ClientListener;
import pedometer.droid.algorithm.common.DetectorManager;
import pedometer.droid.algorithm.common.IDetector;
import pedometer.droid.algorithm.common.IDetectorListener;
import pedometer.droid.algorithm.fall.FallDetector;
import pedometer.droid.algorithm.step.ExponentialMovingAverage;
import pedometer.droid.algorithm.step.StepDetector;
import pedometer.droid.task.ConnectAsyncTask;
import pedometer.droid.task.DisconnectAsyncTask;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

import java.io.IOException;

public class DroidMain extends RoboActivity implements SensorEventListener, IDetectorListener {

    public static final int PLEASE_WAIT_DIALOG = 1;

    private final DroidNetwork network = DroidHandler.getNetwork();

    private ExponentialMovingAverage avg;

    private FallDetector fallDetector;

    private StepDetector stepDetector;

    @InjectView(R.id.stepDetector)
    private TextView stepDetectorTextView;

    @InjectView(R.id.fallDetector)
    private TextView fallDetectorTextView;

    @InjectView(R.id.chart)
    private LinearLayout chartLayout;

    private GraphicalView mChart;

    private XYMultipleSeriesDataset mDataSet = new XYMultipleSeriesDataset();

    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();

    private XYSeries mCurrentSeries;

    private XYSeriesRenderer mCurrentRenderer;

    private long startTimeStamp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DroidHandler.setMain(this);
        setContentView(R.layout.main);

        avg = new ExponentialMovingAverage(DroidPreference.getAlpha());

        fallDetector = new FallDetector();
        stepDetector = new StepDetector(avg);

        DetectorManager detectorManager = DroidHandler.getDetectorManager();

        detectorManager.registerDetector(fallDetector);
        detectorManager.registerDetector(stepDetector);

        detectorManager.registerListener(this);

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(detectorManager, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

        if (mChart == null) {
            mCurrentSeries = new XYSeries("Vector");
            mDataSet.addSeries(mCurrentSeries);
            mCurrentRenderer = new XYSeriesRenderer();
            mRenderer.addSeriesRenderer(mCurrentRenderer);

            mChart = ChartFactory.getCubeLineChartView(this, mDataSet, mRenderer, 0.3f);
            chartLayout.addView(mChart);
        }

        startTimeStamp = System.currentTimeMillis();
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
            case R.id.connect:
                if (!network.isConnected()) {
                    String hostname = PreferenceManager.getDefaultSharedPreferences(DroidMain.this)
                            .getString(DroidPreference.HOST, DroidPreference.HOST_VAL);
                    String port = PreferenceManager.getDefaultSharedPreferences(DroidMain.this)
                            .getString(DroidPreference.PORT, DroidPreference.PORT_VAL);

                    new ConnectAsyncTask(DroidMain.this).execute(hostname, port, new ClientListener() {
                        @Override
                        public void notifyReceived(byte[] object) {
                        }

                        @Override
                        public void notifyDisconnected() {
                            disconnected();
                        }
                    });
                } else {
                    new DisconnectAsyncTask(DroidMain.this).execute();
                }
                return true;
            case R.id.preferences:
                Intent myIntent = new Intent(this, DroidPreference.class);
                startActivity(myIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        DetectorManager detectorManager = DroidHandler.getDetectorManager();
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        sensorManager.unregisterListener(detectorManager);

        detectorManager.unregisterDetector(fallDetector);
        detectorManager.unregisterDetector(stepDetector);

        detectorManager.unregisterListener(this);

        try {
            network.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        switch (dialogId) {
            case PLEASE_WAIT_DIALOG:
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle("Network operation");
                dialog.setMessage("Please wait....");
                dialog.setCancelable(true);
                return dialog;

            default:
                return super.onCreateDialog(dialogId);
        }
    }

    public void connected(final String hostname, final int port) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DroidMain.this, "Connected to " + hostname + "/" + port, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void notConnected(final String hostname, final int port) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DroidMain.this, "Cannot connect to " + hostname + "/" + port, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void disconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DroidMain.this, "Disconnected", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void notDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DroidMain.this, "Error during disconnecting", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        double vector = Math.sqrt((event.values[0] * event.values[0]) +
                (event.values[1] * event.values[1]) +
                (event.values[2] * event.values[2]));

        long timeStamp = System.currentTimeMillis();
        mRenderer.setXAxisMin((timeStamp - startTimeStamp) / 1000.0 - 10.0);
        mRenderer.setXAxisMax((timeStamp - startTimeStamp) / 1000.0);
        mCurrentSeries.add((timeStamp - startTimeStamp) / 1000.0, vector);
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
                    if (detector.equals(fallDetector)) {
                        fallDetectorTextView.setText(count + " falls.");
                        /* Toast.makeText(DroidMain.this, "Fall detected, current count: " + count, Toast.LENGTH_LONG).show(); */
                    } else if (detector.equals(stepDetector)) {
                        stepDetectorTextView.setText(count + " steps.");
                        /* Toast.makeText(DroidMain.this, "Step detected, current count: " + count, Toast.LENGTH_LONG).show(); */
                    }
                }
            }
        });
    }
}
