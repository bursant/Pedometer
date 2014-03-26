package pedometer.droid.task;

import pedometer.droid.DroidHandler;
import pedometer.droid.DroidMain;

import java.io.IOException;

public class DisconnectAsyncTask extends NaviAsyncTask<Void> {

    public DisconnectAsyncTask(DroidMain main) {
        super(main);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            DroidHandler.getNetwork().disconnect();
            getMain().disconnected();
        } catch (IOException e) {
            getMain().notDisconnected();
        }
        return null;
    }
}
