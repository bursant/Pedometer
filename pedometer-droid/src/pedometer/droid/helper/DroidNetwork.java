package pedometer.droid.helper;

import pedometer.common.connector.client.ClientListener;
import pedometer.common.connector.client.ConnectorClient;
import pedometer.common.proto.CommonProto;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class DroidNetwork {

    private Socket socket;

    private ConnectorClient connectorClient;

    public boolean isConnected() {
        return (socket != null);
    }

    public void connect(String hostname, int port, ClientListener listener) throws IOException {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(hostname, port));

            connectorClient = new ConnectorClient(socket);
            connectorClient.setListener(listener);
            connectorClient.start();
        } catch (IOException e) {
            socket = null;
            throw e;
        }
    }

    public void disconnect() throws IOException {
        try {
            if (connectorClient != null) {
                connectorClient.interrupt();
            }
            if (socket != null) {
                socket.close();
            }
        } finally {
            connectorClient = null;
            socket = null;
        }
    }

    public void send() {
        if (connectorClient != null) {
            connectorClient.send(getMessage());
        }
    }

    private static byte[] getMessage() {
        return CommonProto.ControlMessage.newBuilder().build().toByteArray();
    }
}
