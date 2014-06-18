package pedometer.droid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DroidAutoStart extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            if (DroidPreference.isAutoStart(context)) {
                Intent myIntent = new Intent(context, DroidService.class);
                context.startService(myIntent);
            }
        }
    }
}