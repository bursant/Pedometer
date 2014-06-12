package pedometer.droid;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.widget.TextView;
import android.widget.Toast;
import pedometer.app.R;
import roboguice.inject.InjectView;

public class DroidPreference extends PreferenceActivity {

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

    public static float getAlpha() {
        return Float.parseFloat(PreferenceManager.getDefaultSharedPreferences(
                DroidHandler.main).getString(ALPHA, String.valueOf(ALPHA_VAL)));
    }

    public static long getStepDetectionDelta() {
        return Long.parseLong(PreferenceManager.getDefaultSharedPreferences(
                DroidHandler.main).getString(STEP_DETECTION_DELTA, String.valueOf(STEP_DETECTION_DELTA_VAL)));
    }

    public static boolean swapSensorOrientation() {
        return PreferenceManager.getDefaultSharedPreferences(
                DroidHandler.main).getBoolean(SWAP_SENSOR_ORIENTATION, SWAP_SENSOR_ORIENTATION_VAL);
    }
}
