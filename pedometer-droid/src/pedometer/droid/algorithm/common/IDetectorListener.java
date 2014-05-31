package pedometer.droid.algorithm.common;

/**
 * Created by paoolo on 31.05.14.
 */
public interface IDetectorListener {

    public void notifyCountChange(IDetector detector, Integer count);
}
