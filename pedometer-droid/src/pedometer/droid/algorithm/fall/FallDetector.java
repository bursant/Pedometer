package pedometer.droid.algorithm.fall;

import android.hardware.SensorEvent;
import pedometer.droid.algorithm.common.IDetector;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by bursant on 17.05.14.
 * Recognises falls based on sensor data.
 */
public class FallDetector implements IDetector {

    private double freeFallMinThreshold = 8.0;
    private double freeFallMaxThreshold = 10.0;
    private double hitMinThreshold = 30.0;
    private int bufferSize = 5;

    private Queue<Double> buffer;

    public FallDetector() {
        buffer = new LinkedList<Double>();
        initBuffer(bufferSize);
    }


    @Override
    public boolean detect(SensorEvent event) {
        double vector = Math.sqrt(
                (event.values[0] * event.values[0]) +
                        (event.values[1] * event.values[1]) +
                        (event.values[2] * event.values[2])
        );

        buffer.remove();
        buffer.add(vector);

        int counter = 0;

        for (Double value : buffer) {
            if (value < freeFallMaxThreshold && value > freeFallMinThreshold)
                counter++;
            if (value < freeFallMinThreshold)
                counter = 0;
            if (value > hitMinThreshold && counter >= 3) {
                initBuffer(bufferSize);
                return true;
            }
        }

        return false;
    }

    private void initBuffer(int bufferLength) {
        buffer.clear();
        for (int i = 0; i < bufferLength; i++)
            buffer.add(0.0);
    }

    @Override
    public int compareTo(IDetector iDetector) {
        if (iDetector != null && FallDetector.class.equals(iDetector.getClass()))
            return 0;
        return -1;
    }
}
