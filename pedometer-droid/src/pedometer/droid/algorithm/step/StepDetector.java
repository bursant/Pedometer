package pedometer.droid.algorithm.step;

import android.hardware.SensorEvent;
import pedometer.droid.algorithm.common.IDetector;

/**
 * Created by bursant on 17.05.14.
 * Recognises steps based on sensor data.
 */
public class StepDetector implements IDetector {

    private ExponentialMovingAverage avg;
    private long LastStepDetection;
    private long StepDetectionDelta;
    private double DifferenceDelta;

    public StepDetector(ExponentialMovingAverage avg) {
        this.avg = avg;
        LastStepDetection = 0;
        StepDetectionDelta = 500;
        DifferenceDelta = 0.8;
    }

    @Override
    public boolean detect(SensorEvent event) {
        double vector = Math.sqrt(
                (event.values[0] * event.values[0]) +
                        (event.values[1] * event.values[1]) +
                        (event.values[2] * event.values[2])
        );

        double average = avg.average(vector);
        long time = System.currentTimeMillis();

        if (vector - average > DifferenceDelta && time - LastStepDetection > StepDetectionDelta) {
            LastStepDetection = time;
            //TODO(bursant): You may notify something here that step was detected.
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
