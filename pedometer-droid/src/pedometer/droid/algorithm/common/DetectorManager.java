package pedometer.droid.algorithm.common;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by bursant on 17.05.14.
 * Listens to sensor events, detects steps and falls.
 */
public class DetectorManager implements SensorEventListener, IDetectorManager {

    private List<IDetector> detectors;
    private List<IDetectorListener> listeners;
    private Map<IDetector, Boolean> results;
    private Map<IDetector, Integer> counts;

    public DetectorManager(List<IDetector> detectors, List<IDetectorListener> listeners) {
        this.detectors = detectors;
        this.listeners = listeners;

        results = new TreeMap<IDetector, Boolean>();
        counts = new HashMap<IDetector, Integer>();

        for (IDetector detector : detectors) {
            results.put(detector, false);
            counts.put(detector, 0);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Double vector = MotionVector.compute(event.values[0], event.values[1], event.values[2]);
        Log.i("SENSOR", vector.toString());
        for (IDetector detector : detectors) {
            boolean result = detector.detect(event);
            results.put(detector, result);
            if (result) {
                Integer value;
                synchronized (detector) {
                    value = counts.get(detector);
                    if (value != null)
                        value += 1;
                    counts.put(detector, value);
                }
                for (IDetectorListener listener : listeners) {
                    listener.notifyCountChange(detector, value);
                }
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    public boolean registerDetector(IDetector detector) {
        results.put(detector, false);
        counts.put(detector, 0);

        return detectors.add(detector);
    }

    @Override
    public boolean unregisterDetector(IDetector detector) {
        if (results.containsKey(detector))
            results.remove(detector);
        if (counts.containsKey(detector))
            counts.remove(detector);

        return detectors.contains(detector) && detectors.remove(detector);
    }

    @Override
    public boolean registerListener(IDetectorListener listener) {
        return listeners.add(listener);
    }

    @Override
    public boolean unregisterListener(IDetectorListener listener) {
        return listeners.remove(listener);
    }

    @Override
    public boolean getLastResultForDetector(IDetector detector) {
        return results.get(detector);
    }

    @Override
    public int getCountForDetector(IDetector detector) {
        return counts.get(detector);
    }
}