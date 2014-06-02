package pedometer.droid;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import pedometer.common.connector.client.ClientListener;
import pedometer.droid.algorithm.common.DetectorManager;
import pedometer.droid.algorithm.common.IDetector;
import pedometer.droid.algorithm.common.IDetectorListener;
import pedometer.droid.algorithm.fall.FallDetector;
import pedometer.droid.algorithm.step.ExponentialMovingAverage;
import pedometer.droid.algorithm.step.StepDetector;
import pedometer.droid.helper.DroidAccelSensor;
import pedometer.droid.helper.DroidNetwork;
import pedometer.droid.task.ConnectAsyncTask;
import pedometer.droid.task.DisconnectAsyncTask;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

import java.io.IOException;
import java.util.Random;

public class DroidMain extends RoboActivity implements IDetectorListener {

    public static final int PLEASE_WAIT_DIALOG = 1;

    private final DroidNetwork network = DroidHandler.getNetwork();

    private ExponentialMovingAverage avg;

    private FallDetector fallDetector;

    private StepDetector stepDetector;

    private DroidAccelSensor sensorAccel;

    @InjectView(R.id.connectButton)
    private Button connectButton;

    @InjectView(R.id.accSensor)
    private TextView accSensorTextView;

    @InjectView(R.id.chart)
    private LinearLayout chartLayout;

    private GraphicalView mChart;

    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();

    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();

    private XYSeries mCurrentSeries;

    private XYSeriesRenderer mCurrentRenderer;

    private void initChart() {
        mCurrentSeries = new XYSeries("Sample Data");
        mDataset.addSeries(mCurrentSeries);
        mCurrentRenderer = new XYSeriesRenderer();
        mRenderer.addSeriesRenderer(mCurrentRenderer);
    }

    private void addSampleData() {
        mCurrentSeries.add(1, 2);
        mCurrentSeries.add(2, 3);
        mCurrentSeries.add(3, 2);
        mCurrentSeries.add(4, 5);
        mCurrentSeries.add(5, 4);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DroidHandler.setMain(this);
        setContentView(R.layout.main);

        connectButton.setText(network.isConnected() ? "Disconnect" : "Connect");

        connectButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
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
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        avg = new ExponentialMovingAverage(DroidPreference.getAlpha());

        fallDetector = new FallDetector();
        stepDetector = new StepDetector(avg);

        sensorAccel = new DroidAccelSensor(DroidPreference.swapSensorOrientation(), this);

        DetectorManager detectorManager = DroidHandler.getDetectorManager();

        detectorManager.registerDetector(fallDetector);
        detectorManager.registerDetector(stepDetector);

        detectorManager.registerListener(this);

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(sensorAccel, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(detectorManager, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);

        if (mChart == null) {
            initChart();
            addSampleData();
            mChart = ChartFactory.getCubeLineChartView(this, mDataset, mRenderer, 0.3f);
            chartLayout.addView(mChart);
        } else {
            mChart.repaint();
        }
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
            case R.id.preferences:
                Intent myIntent = new Intent(this, DroidPreference.class);
                startActivity(myIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        DetectorManager detectorManager = DroidHandler.getDetectorManager();
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        sensorManager.unregisterListener(sensorAccel);
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
                connectButton.setText("Disconnect");
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
                connectButton.setText("Connect");
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

    public void setAccSensor(final float values[]) {
        setSensor(accSensorTextView, "acc", values);
    }

    private double d = 6;

    private Random rand = new Random(System.currentTimeMillis());

    private void setSensor(final TextView view, final String pre, final float values[]) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setText(pre + ":\n\tx: " + values[0] + ";\n\ty: " + values[1] + ";\n\tz: " + values[2]);
                if (mChart != null) {
                    mCurrentSeries.remove(0);
                    mCurrentSeries.add(d, rand.nextInt(10));
                    d += 1;
                    mChart.repaint();
                }
            }
        });
    }

    @Override
    public void notifyCountChange(final IDetector detector, final Integer count) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (detector != null) {
                    if (detector.equals(fallDetector)) {
                        Toast.makeText(DroidMain.this, "Fall detected, current count: " + count, Toast.LENGTH_LONG).show();
                    } else if (detector.equals(stepDetector)) {
                        Toast.makeText(DroidMain.this, "Step detected, current count: " + count, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
}
