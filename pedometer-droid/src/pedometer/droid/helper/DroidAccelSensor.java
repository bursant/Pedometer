package pedometer.droid.helper;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import pedometer.droid.DroidPreference;

public class DroidAccelSensor implements SensorEventListener {

    private final boolean swapSensorOrientation;

    private final SpeedAccelCalculator speedAccelCalculator = new SpeedAccelCalculator();

    private float[] oldValues;

    public DroidAccelSensor(boolean swapSensorOrientation) {
        this.swapSensorOrientation = swapSensorOrientation;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float[] values = lowPass(sensorEvent.values, oldValues);
        oldValues = values;
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
