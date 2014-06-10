package pedometer.droid.algorithm.common;

/**
 * Created by bursant on 03.06.14.
 * Compute value of motion vector.
 */
public class MotionVector {
    public static double compute(double x, double y, double z) {
        return Math.sqrt(x * x + y * y + z * z);
    }
}
