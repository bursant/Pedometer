package pedometer.droid;

import pedometer.droid.algorithm.common.DetectorManager;
import pedometer.droid.algorithm.common.IDetector;
import pedometer.droid.algorithm.common.IDetectorListener;
import pedometer.droid.algorithm.fall.FallDetector;
import pedometer.droid.algorithm.step.ExponentialMovingAverage;
import pedometer.droid.algorithm.step.StepDetector;

import java.util.LinkedList;
import java.util.List;

public class DroidHandler {

    static final DetectorManager detectorManager;

    static {
        List<IDetector> detectors = new LinkedList<IDetector>();
        List<IDetectorListener> listeners = new LinkedList<IDetectorListener>();

        detectorManager = new DetectorManager(detectors, listeners);
    }

    static DroidMain main;

    static ExponentialMovingAverage avg;

    static FallDetector fallDetector;

    static StepDetector stepDetector;

    static long startTimeStamp;

    static boolean started;
}
