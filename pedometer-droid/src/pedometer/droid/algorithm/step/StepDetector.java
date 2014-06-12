package pedometer.droid.algorithm.step;

import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.util.Log;
import pedometer.droid.algorithm.common.IDetector;
import pedometer.droid.algorithm.common.MotionVector;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by bursant on 17.05.14.
 * Recognises steps based on sensor data.
 */
public class StepDetector implements IDetector {

    private ExponentialMovingAverage avg;
    private long LastStepDetection;
    private long StepDetectionDelta;
    private double DifferenceDelta;
    private double minPeak;
    private int count;

    public StepDetector(ExponentialMovingAverage avg, long StepDetectionDelta) {
        this.avg = avg;
        LastStepDetection = 0;
        this.StepDetectionDelta = StepDetectionDelta;
        DifferenceDelta = 1.0;
        minPeak = 3.0;
        count = 1;
    }

    @Override
    public boolean detect(SensorEvent event) {
        Double vector = MotionVector.compute(event.values[0], event.values[1], event.values[2]);

        double average = avg.average(vector);
        long time = System.currentTimeMillis();

        if (vector - average > DifferenceDelta && time - LastStepDetection > StepDetectionDelta && minPeak < vector) {
            LastStepDetection = time;
            Log.i("Pedometer", "Step detected " + count);
            count++;
            return true;
        }

        return false;
    }

    @Override
    public int compareTo(IDetector iDetector) {
        if (iDetector != null && StepDetector.class.equals(iDetector.getClass()))
            return 0;
        return -1;
    }
}
