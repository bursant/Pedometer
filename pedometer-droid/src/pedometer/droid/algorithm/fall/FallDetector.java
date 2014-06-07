package pedometer.droid.algorithm.fall;

import android.hardware.SensorEvent;
import pedometer.droid.algorithm.common.IDetector;
import pedometer.droid.algorithm.common.MotionVector;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by bursant on 17.05.14.
 * Recognises falls based on sensor data.
 */
public class FallDetector implements IDetector {

    private enum State{
        fall,
        afterFall,
        noFall
    }

    private static final double afterFallThreshold = 0.35;
    private static final long afterFallTime = 2000;

    private static final double fallInitThreshold = 10.0;
    private static final double fallDurationThreshold = 5.0;
    private static final double fallEndThreshold = 1.0;

    private double fallEndTimestamp;

    private State state;
    private Queue<Double> buffer;

    public FallDetector() {
        buffer = new LinkedList<Double>();
        state = State.noFall;
    }


    @Override
    public boolean detect(SensorEvent event) {
        Double vector;
        vector = MotionVector.compute(event.values[0], event.values[1], event.values[2]);
        long timestamp = System.currentTimeMillis();

        switch(state){
            case fall:
                return fallProcedure(vector, timestamp);
            case noFall:
                return noFallProcedure(vector);
            case afterFall:
                return afterFallProcedure(vector, timestamp);
        }

        return false;
    }

    private boolean fallProcedure(double vector, long timestamp){

        if(vector < fallEndThreshold){
            if(getAverage() > fallDurationThreshold) {
                state = State.afterFall;
                fallEndTimestamp = timestamp;
            }
            else{
                state = State.noFall;
            }
            buffer.clear();
        }
        return false;
    }

    private boolean afterFallProcedure(double vector, long timestamp){
        if(timestamp - fallEndTimestamp < afterFallTime){
            buffer.add(vector);
        }
        else{
            double value = getAverage();
            buffer.clear();
            state = State.noFall;
            if(value < afterFallThreshold)
                return true;
        }
        return false;
    }

    private boolean noFallProcedure(double vector){
        if(vector > fallInitThreshold) {
            state = State.fall;
            buffer.add(vector);
        }
        return false;
    }

    private double getAverage(){
        double avg = 0.0;

        for(double item : buffer)
            avg += item;

        avg = avg/buffer.size();
        return avg;
    }

    @Override
    public int compareTo(IDetector iDetector) {
        if (iDetector != null && FallDetector.class.equals(iDetector.getClass()))
            return 0;
        return -1;
    }
}
