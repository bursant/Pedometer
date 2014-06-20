package pedometer.droid.algorithm.step;

/**
 * Created by bursant on 17.05.14.
 * Calculates and returns exponential moving average.
 */
public class ExponentialMovingAverage {

    private double alpha;

    private Double oldValue;

    public ExponentialMovingAverage(double alpha) {
        this.alpha = alpha;
    }

    public double average(double value) {
        if (oldValue == null) {
            oldValue = value;
            return value;
        }
        double newValue = oldValue + alpha * (value - oldValue);
        oldValue = newValue;
        return newValue;
    }
}
