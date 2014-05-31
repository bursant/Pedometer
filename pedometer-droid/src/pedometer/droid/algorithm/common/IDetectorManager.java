package pedometer.droid.algorithm.common;

/**
 * Created by bursant on 17.05.14.
 * Interface for classes that manage detectors.
 */
public interface IDetectorManager {

    /**
     * Register detector from detector manager.
     */
    boolean registerDetector(IDetector detector);

    /**
     * Unregister detector from detector manager.
     */
    boolean unregisterDetector(IDetector detector);

    /**
     * Return the result of the last detector algorithm run. If true, than event was detected.
     * If false, event was not detected.
     */
    boolean getLastResultForDetector(IDetector detector);

    boolean registerListener(IDetectorListener listener);

    boolean unregisterListener(IDetectorListener listener);

    int getCountForDetector(IDetector detector);
}
