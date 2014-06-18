package pedometer.droid;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;
import pedometer.droid.algorithm.common.IDetector;
import pedometer.droid.algorithm.common.IDetectorListener;
import roboguice.service.RoboIntentService;

public class DroidService extends RoboIntentService implements IDetectorListener, SensorEventListener {

    public static final String STEPS = "steps";
    public static final String FALLS = "falls";

    public static final String NOTIFICATION = "pedometer.droid.receiver";

    private int steps, falls;

    public DroidService() {
        super("PedoMeter");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        start();
        register();
        Toast.makeText(this, "Started pedometer", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregister();
        stop();
        Toast.makeText(this, "Stopped pedometer", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        publishResults(-1, -1);
    }

    private void publishResults(int steps, int falls) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(STEPS, steps);
        intent.putExtra(FALLS, falls);
        sendBroadcast(intent);
    }

    private void start() {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(DroidHandler.detectorManager, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void register() {
        DroidHandler.detectorManager.unregisterDetector(DroidHandler.fallDetector);
        DroidHandler.detectorManager.unregisterDetector(DroidHandler.stepDetector);

        DroidHandler.detectorManager.registerDetector(DroidHandler.fallDetector);
        DroidHandler.detectorManager.registerDetector(DroidHandler.stepDetector);

        DroidHandler.detectorManager.registerListener(this);
    }

    private void unregister() {
        DroidHandler.detectorManager.unregisterListener(this);
    }

    private void stop() {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.unregisterListener(DroidHandler.detectorManager);
        sensorManager.unregisterListener(this);
    }

    @Override
    public void notifyCountChange(final IDetector detector, final Integer count) {
        if (detector != null) {
            if (detector.equals(DroidHandler.fallDetector)) {
                falls = count;
            } else if (detector.equals(DroidHandler.stepDetector)) {
                steps = count;
            }
        }
        publishResults(steps, falls);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
