package pedometer.droid.algorithm.common;

/**
 * Created by paoolo on 31.05.14.
 * Interface for classes that wait for changes.
 */
public interface IDetectorListener {

    public void notifyCountChange(IDetector detector, Integer count);
}
