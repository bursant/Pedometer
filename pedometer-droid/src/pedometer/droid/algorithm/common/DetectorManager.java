package pedometer.droid.algorithm.common;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by bursant on 17.05.14.
 * Listens to sensor events, detects steps and falls.
 */
public class DetectorManager implements SensorEventListener, IDetectorManager {

    private List<IDetector> detectors;
    private Map<IDetector, Boolean> results;

    public DetectorManager(List<IDetector> detectors){
        this.detectors = detectors;

        results = new TreeMap<IDetector, Boolean>();
        for(IDetector detector : detectors) {
            results.put(detector, false);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        for(IDetector detector : detectors) {
            boolean result = detector.detect(event);
            results.put(detector, result);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { }

    @Override
    public boolean register(IDetector detector) {
        return detectors.add(detector );
    }

    @Override
    public boolean unregister(IDetector detector) {
        return detectors.remove(detector);
    }

    @Override
    public boolean getLastResult(IDetector detector) {
        return results.get(detector);
    }
}