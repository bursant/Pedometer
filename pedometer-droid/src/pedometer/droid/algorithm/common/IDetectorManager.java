package pedometer.droid.algorithm.common;

/**
 * Created by bursant on 17.05.14.
 * Interface for classes that manage detectors.
 */
public interface IDetectorManager {

    /*
     *  Registers detector from detector manager.
     */
    boolean register(IDetector detector);

    /*
     * Unregisters detector from detector manager.
     */
    boolean unregister(IDetector detector);

    /*
     * Returns the result of the last detector algorithm run. If true, than event was detected.
     * If false, event was not detected.
     */
    boolean getLastResult(IDetector detector);

}