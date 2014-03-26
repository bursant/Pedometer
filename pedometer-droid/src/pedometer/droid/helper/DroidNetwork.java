package pedometer.droid.helper;

import pedometer.common.connector.client.ClientConnector;
import pedometer.common.connector.client.ClientListener;
import pedometer.common.proto.CommonProto;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class DroidNetwork {

    private Socket socket;

    private ClientConnector clientConnector;

    public boolean isConnected() {
        return (socket != null);
    }

    public void connect(String hostname, int port, ClientListener listener) throws IOException {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(hostname, port));

            clientConnector = new ClientConnector(socket);
            clientConnector.setListener(listener);
            clientConnector.start();
        } catch (IOException e) {
            socket = null;
            throw e;
        }
    }

    public void disconnect() throws IOException {
        try {
            if (clientConnector != null) {
                clientConnector.interrupt();
            }
            if (socket != null) {
                socket.close();
            }
        } finally {
            clientConnector = null;
            socket = null;
        }
    }

    public void sendAcc(float values[]) {
        if (clientConnector != null) {
            clientConnector.send(CommonProto.Message.newBuilder()
                    .setAccX(values[0]).setAccY(values[1]).setAccZ(values[2])
                    .build().toByteArray());
        }
    }

    public void sendGyro(float values[]) {
        if (clientConnector != null) {
            clientConnector.send(CommonProto.Message.newBuilder()
                    .setGyroX(values[0]).setGyroY(values[1]).setGyroZ(values[2])
                    .build().toByteArray());
        }
    }
}
