package pedometer.droid;

import pedometer.droid.algorithm.common.DetectorManager;
import pedometer.droid.algorithm.common.IDetector;
import pedometer.droid.algorithm.common.IDetectorListener;
import pedometer.droid.helper.DroidNetwork;

import java.util.LinkedList;
import java.util.List;

public class DroidHandler {

    private static final DroidNetwork network = new DroidNetwork();

    private static final DetectorManager detectorManager;

    static {
        List<IDetector> detectors = new LinkedList<IDetector>();
        List<IDetectorListener> listeners = new LinkedList<IDetectorListener>();

        detectorManager = new DetectorManager(detectors, listeners);
    }

    private static final Object mainLock = new Object();

    private static DroidMain main;

    public static void setMain(DroidMain main) {
        synchronized (mainLock) {
            DroidHandler.main = main;
        }
    }

    public static DroidMain getMain() {
        synchronized (mainLock) {
            return main;
        }
    }

    public static DroidNetwork getNetwork() {
        return network;
    }

    public static DetectorManager getDetectorManager() {
        return detectorManager;
    }
}
