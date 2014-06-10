package pedometer.droid;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import pedometer.app.R;

public class DroidPreference extends PreferenceActivity {

    public static final String HOST = "host";
    public static final String HOST_VAL = "192.168.0.100";

    public static final String PORT = "port";
    public static final String PORT_VAL = "1234";

    public static final String MAX_GLOBAL_X = "maxGlobalX";
    public static final float MAX_GLOBAL_X_VAL = -5.0f;

    public static final String MIN_GLOBAL_X = "minGlobalX";
    public static final float MIN_GLOBAL_X_VAL = 5.0f;

    public static final String MAX_GLOBAL_Y = "maxGlobalY";
    public static final float MAX_GLOBAL_Y_VAL = 7.5f;

    public static final String MIN_GLOBAL_Y = "minGlobalY";
    public static final float MIN_GLOBAL_Y_VAL = -7.5f;

    public static final String ALPHA = "alpha";
    public static final float ALPHA_VAL = 0.1f;

    public static final String SWAP_SENSOR_ORIENTATION = "swapSensorOrientation";
    public static final boolean SWAP_SENSOR_ORIENTATION_VAL = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
    }

    public static float getMaxGlobalX() {
        return PreferenceManager.getDefaultSharedPreferences(
                DroidHandler.main).getFloat(MAX_GLOBAL_X, MAX_GLOBAL_X_VAL);
    }

    public static float getMinGlobalX() {
        return PreferenceManager.getDefaultSharedPreferences(
                DroidHandler.main).getFloat(MIN_GLOBAL_X, MIN_GLOBAL_X_VAL);
    }

    public static float getMaxGlobalY() {
        return PreferenceManager.getDefaultSharedPreferences(
                DroidHandler.main).getFloat(MAX_GLOBAL_Y, MAX_GLOBAL_Y_VAL);
    }

    public static float getMinGlobalY() {
        return PreferenceManager.getDefaultSharedPreferences(
                DroidHandler.main).getFloat(MIN_GLOBAL_Y, MIN_GLOBAL_Y_VAL);
    }

    public static float getAlpha() {
        return Float.parseFloat(PreferenceManager.getDefaultSharedPreferences(
                DroidHandler.main).getString(ALPHA, String.valueOf(ALPHA_VAL)));
    }

    public static boolean swapSensorOrientation() {
        return PreferenceManager.getDefaultSharedPreferences(
                DroidHandler.main).getBoolean(SWAP_SENSOR_ORIENTATION, SWAP_SENSOR_ORIENTATION_VAL);
    }
}
