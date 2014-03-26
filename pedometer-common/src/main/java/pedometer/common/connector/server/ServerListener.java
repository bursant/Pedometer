package pedometer.common.connector.server;

import pedometer.common.connector.client.ClientConnector;

public interface ServerListener {

    void notifyNewClient(ClientConnector client);
}
