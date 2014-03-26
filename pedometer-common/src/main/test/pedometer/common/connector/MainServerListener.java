package pedometer.common.connector;

import java.util.Arrays;

/**
 * Created by paoolo on 26.03.14.
 */
class MainServerListener implements Listener {

    private final Connector server;

    public MainServerListener(Connector server) {
        this.server = server;
    }

    @Override
    public void notifyReceived(byte[] object) {
        System.err.println("received " + Arrays.toString(object));
        server.send(object);
    }

    @Override
    public void notifyDisconnected() {
        System.err.println("disconnected");
    }
}
