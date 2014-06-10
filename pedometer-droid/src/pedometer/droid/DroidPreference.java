package pedometer.droid;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import pedometer.app.R;

public class DroidPreference extends PreferenceActivity {

    public static final String ALPHA = "alpha";
    public static final float ALPHA_VAL = 0.3f;

    public static final String SWAP_SENSOR_ORIENTATION = "swapSensorOrientation";
    public static final boolean SWAP_SENSOR_ORIENTATION_VAL = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
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
