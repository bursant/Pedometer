package pedometer.droid.algorithm.common;

import android.hardware.SensorEvent;

/**
 * Created by bursant on 17.05.14.
 * Interface for classes that implements recognition algorithms.
 */
public interface IDetector {

    /*
     * Runs event detection algorithm.
     */
    boolean detect(SensorEvent event);

}
