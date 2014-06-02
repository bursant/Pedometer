package pedometer.droid.task;

import pedometer.common.connector.client.ClientListener;
import pedometer.droid.DroidHandler;
import pedometer.droid.DroidMain;

import java.io.IOException;

public class ConnectAsyncTask extends NaviAsyncTask<Object> {

    public ConnectAsyncTask(DroidMain main) {
        super(main);
    }

    @Override
    protected Void doInBackground(Object... args) {
        final String hostname = (String) args[0];
        final int port = Integer.parseInt((String) args[1]);
        final ClientListener listener = (ClientListener) args[2];

        try {
            DroidHandler.network.connect(hostname, port, listener);
            getMain().connected(hostname, port);
        } catch (IOException e) {
            getMain().notConnected(hostname, port);
        }
        return null;
    }
}
