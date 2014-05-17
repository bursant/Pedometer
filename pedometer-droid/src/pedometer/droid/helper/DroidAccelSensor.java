package pedometer.droid.helper;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import pedometer.droid.DroidHandler;
import pedometer.droid.DroidMain;
import pedometer.droid.DroidPreference;

public class DroidAccelSensor implements SensorEventListener {

    private final boolean swapSensorOrientation;

    private final SpeedAccelCalculator speedAccelCalculator = new SpeedAccelCalculator();

    private final DroidMain droidMain;

    private float[] oldValues;

    public DroidAccelSensor(boolean swapSensorOrientation, DroidMain droidMain) {
        this.swapSensorOrientation = swapSensorOrientation;
        this.droidMain = droidMain;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float[] values = lowPass(sensorEvent.values, oldValues);
        oldValues = values;

        // TODO(bursant): fill with SpeedGyroCalculator

        droidMain.setAccSensor(values);
        DroidHandler.getNetwork().sendAcc(values);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    private static float[] lowPass(float[] input, float[] output) {
        if (output == null) {
            return input;
        }

        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + DroidPreference.getAlpha() * (input[i] - output[i]);
        }
        return output;
    }
}
