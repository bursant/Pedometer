package pedometer.droid;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import pedometer.app.R;

public class DroidPreference extends PreferenceActivity {

    public static final String AUTO_START = "autoStart";
    public static final boolean AUTO_START_VAL = false;

    public static final String ALPHA = "alpha";
    public static final float ALPHA_VAL = 0.03f;

    public static final String STEP_DETECTION_DELTA = "delta";
    public static final long STEP_DETECTION_DELTA_VAL = 700;

    public static final String SWAP_SENSOR_ORIENTATION = "swapSensorOrientation";
    public static final boolean SWAP_SENSOR_ORIENTATION_VAL = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
    }

    public static boolean isAutoStart(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(
                ctx).getBoolean(AUTO_START, AUTO_START_VAL);
    }

    public static float getAlpha(Context ctx) {
        return Float.parseFloat(PreferenceManager.getDefaultSharedPreferences(
                ctx).getString(ALPHA, String.valueOf(ALPHA_VAL)));
    }

    public static long getStepDetectionDelta(Context ctx) {
        return Long.parseLong(PreferenceManager.getDefaultSharedPreferences(
                ctx).getString(STEP_DETECTION_DELTA, String.valueOf(STEP_DETECTION_DELTA_VAL)));
    }

    public static boolean swapSensorOrientation(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(
                ctx).getBoolean(SWAP_SENSOR_ORIENTATION, SWAP_SENSOR_ORIENTATION_VAL);
    }
}
