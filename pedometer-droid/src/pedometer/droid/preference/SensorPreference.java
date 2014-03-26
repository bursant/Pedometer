package pedometer.droid.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import pedometer.droid.DroidPreference;
import info.suder.navi.main.app.R;

public class SensorPreference extends DialogPreference implements SensorEventListener {

    private final SensorManager sensorManager;

    private boolean swapSensorOrientation = false;

    private boolean Y_VALUE = false;

    private float newValue = 0.0f;

    private TextView sensorValueTextView;

    public SensorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.sensor);

        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SensorPreference);
        if (a != null) {
            String yValue = a.getString(R.styleable.SensorPreference_sensorValue);
            Y_VALUE = (yValue != null && yValue.equals("y") && !yValue.equals("x"));
        }

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    protected void onBindDialogView(View view) {
        sensorValueTextView = (TextView) view.findViewById(R.id.sensorValueText);

        super.onBindDialogView(view);
    }

    @Override
    protected View onCreateDialogView() {
        swapSensorOrientation = DroidPreference.swapSensorOrientation();

        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

        return super.onCreateDialogView();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        sensorManager.unregisterListener(this);

        if (positiveResult) {
            persistFloat(newValue);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        newValue = ((swapSensorOrientation ? !Y_VALUE : Y_VALUE) ? sensorEvent.values[1] : sensorEvent.values[0]);

        if (sensorValueTextView != null) {
            sensorValueTextView.setText("Sensor value is " + newValue + ".");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @SuppressWarnings("unused")
    public static class SavedState extends BaseSavedState {

        private float value;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            value = source.readFloat();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeFloat(value);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {

                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
