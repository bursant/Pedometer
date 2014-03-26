package pedometer.droid;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.*;
import android.widget.Button;
import android.widget.Toast;
import pedometer.app.R;
import pedometer.common.connector.Listener;
import pedometer.droid.helper.DroidAccelSensor;
import pedometer.droid.helper.DroidGyroSensor;
import pedometer.droid.helper.DroidNetwork;
import pedometer.droid.task.ConnectAsyncTask;
import pedometer.droid.task.DisconnectAsyncTask;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

import java.io.IOException;

public class DroidMain extends RoboActivity {

    public static final int PLEASE_WAIT_DIALOG = 1;

    private final DroidNetwork network = DroidHandler.getNetwork();

    private DroidAccelSensor sensorAccel;

    private DroidGyroSensor sensorGyro;

    @InjectView(R.id.connectButton)
    private Button connectButton;

    @InjectView(R.id.driveButton)
    private Button driveButton;

    @InjectView(R.id.leftSide)
    private View leftSide;

    @InjectView(R.id.rightSide)
    private View rightSide;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DroidHandler.setMain(this);
        setContentView(R.layout.main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        connectButton.setText(network.isConnected() ? "Disconnect" : "Connect");
        driveButton.setEnabled(network.isConnected());

        connectButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (!network.isConnected()) {
                    String hostname = PreferenceManager.getDefaultSharedPreferences(DroidMain.this)
                            .getString(DroidPreference.HOST, DroidPreference.HOST_VAL);
                    String port = PreferenceManager.getDefaultSharedPreferences(DroidMain.this)
                            .getString(DroidPreference.PORT, DroidPreference.PORT_VAL);

                    new ConnectAsyncTask(DroidMain.this).execute(hostname, port, new Listener() {
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
        driveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // nothing
            }
        });

        leftSide.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        // touch move code
                        break;

                    case MotionEvent.ACTION_UP:
                        break;
                }
                return false;
            }
        });
        rightSide.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        // touch move code
                        break;

                    case MotionEvent.ACTION_UP:
                        break;
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        sensorAccel = new DroidAccelSensor(DroidPreference.swapSensorOrientation());
        sensorGyro = new DroidGyroSensor(DroidPreference.swapSensorOrientation());

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(sensorAccel, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

        Sensor gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(sensorGyro, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
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

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.unregisterListener(sensorAccel);
        sensorManager.unregisterListener(sensorGyro);

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
                driveButton.setEnabled(true);
                driveButton.setText("Drive");

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
                driveButton.setEnabled(false);
                driveButton.setText("Drive");

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
}
