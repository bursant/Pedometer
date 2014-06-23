package pedometer.droid;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;
import pedometer.app.R;
import pedometer.droid.algorithm.common.DetectorManager;
import pedometer.droid.algorithm.common.IDetector;
import pedometer.droid.algorithm.common.IDetectorListener;
import pedometer.droid.algorithm.fall.FallDetector;
import pedometer.droid.algorithm.step.ExponentialMovingAverage;
import pedometer.droid.algorithm.step.StepDetector;
import roboguice.service.RoboService;

public class DroidService extends RoboService implements IDetectorListener, SensorEventListener {

    public static final String STEPS = "steps";
    public static final String FALLS = "falls";

    public static final String NOTIFICATION = "pedometer.droid.receiver";

    private final DetectorManager detectorManager = new DetectorManager();

    private FallDetector fallDetector;

    private StepDetector stepDetector;

    private boolean started;

    private int steps, falls;

    private final IBinder droidBinder = new DroidBinder();

    public boolean isStarted() {
        return started;
    }

    public DetectorManager getDetectorManager() {
        return detectorManager;
    }

    public StepDetector getStepDetector() {
        return stepDetector;
    }

    public FallDetector getFallDetector() {
        return fallDetector;
    }

    public class DroidBinder extends Binder {

        public DroidService getService() {
            return DroidService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        started = true;

        ExponentialMovingAverage avg = new ExponentialMovingAverage(DroidPreference.getAlpha(getApplicationContext()));

        fallDetector = new FallDetector();
        stepDetector = new StepDetector(avg, DroidPreference.getStepDetectionDelta(getApplicationContext()));

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        sensorManager.registerListener(detectorManager, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

        detectorManager.registerDetector(fallDetector);
        detectorManager.registerDetector(stepDetector);

        detectorManager.registerListener(this);

        Notification note = new Notification(R.drawable.icon, "Pedometer", System.currentTimeMillis());
        Intent intent = new Intent(this, DroidService.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        note.setLatestEventInfo(this, "Pedometer", "Working state...", pendingIntent);

        note.flags |= Notification.FLAG_NO_CLEAR;

        startForeground(1337, note);

        Toast.makeText(this, "Started pedometer", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        started = false;

        detectorManager.unregisterDetector(fallDetector);
        detectorManager.unregisterDetector(stepDetector);

        detectorManager.unregisterListener(this);

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        sensorManager.unregisterListener(detectorManager);
        sensorManager.unregisterListener(this);

        stopForeground(true);

        Toast.makeText(this, "Stopped pedometer", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return droidBinder;
    }

    private void publishResults(int steps, int falls) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(STEPS, steps);
        intent.putExtra(FALLS, falls);
        sendBroadcast(intent);
    }

    @Override
    public void notifyCountChange(final IDetector detector, final Integer count) {
        if (detector != null) {
            if (detector.equals(fallDetector)) {
                falls = count;
            } else if (detector.equals(stepDetector)) {
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
