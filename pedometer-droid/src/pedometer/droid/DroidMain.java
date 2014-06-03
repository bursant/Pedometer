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
import pedometer.common.connector.client.ClientListener;
import pedometer.droid.algorithm.common.IDetector;
import pedometer.droid.algorithm.common.IDetectorListener;
import pedometer.droid.algorithm.common.MotionVector;
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

        try {
            DroidHandler.network.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Menu menu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.connect: {
                if (!DroidHandler.network.isConnected()) {
                    connect();
                } else {
                    disconnect();
                }
                return true;
            }
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

    private void connect() {
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
    }

    private void disconnect() {
        new DisconnectAsyncTask(DroidMain.this).execute();
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

    public void connected(final String hostname, final int port) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (DroidMain.this.menu != null) {
                    MenuItem item = DroidMain.this.menu.findItem(R.id.connect);
                    if (item != null)
                        item.setTitle("Disconnect");
                }
                Toast.makeText(DroidMain.this, "Connected to " + hostname + "/" + port, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void notConnected(final String hostname, final int port) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DroidMain.this, "Cannot connect to " + hostname + "/" + port, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void disconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (DroidMain.this.menu != null) {
                    MenuItem item = DroidMain.this.menu.findItem(R.id.connect);
                    if (item != null)
                        item.setTitle("Connect");
                }
                Toast.makeText(DroidMain.this, "Disconnected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void notDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DroidMain.this, "Error during disconnecting", Toast.LENGTH_SHORT).show();
            }
        });
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
