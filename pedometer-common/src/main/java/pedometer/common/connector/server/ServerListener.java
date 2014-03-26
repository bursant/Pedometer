package pedometer.common.connector.server;

import pedometer.common.connector.client.ConnectorClient;

public interface ServerListener {

    void notifyNewClient(ConnectorClient client);
}
