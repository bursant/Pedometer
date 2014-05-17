package pedometer.server;

import com.google.protobuf.InvalidProtocolBufferException;
import pedometer.common.connector.client.ClientConnector;
import pedometer.common.connector.client.ClientListener;
import pedometer.common.connector.server.ServerConnector;
import pedometer.common.connector.server.ServerListener;
import pedometer.common.proto.CommonProto;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

class MainClientListener implements ClientListener {

    @Override
    public void notifyReceived(byte[] object) {
        try {
            CommonProto.Message msg = CommonProto.Message.parseFrom(object);
            String out = String.format("acc: x=%04g, y=%04g, z=%04g; gyro: x=%04g, y=%04g, z=%04g",
                    msg.getAccX(), msg.getAccY(), msg.getAccZ(),
                    msg.getGyroX(), msg.getGyroY(), msg.getGyroZ());
            System.err.println(out);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void notifyDisconnected() {
        System.err.println("disconnected");
    }
}

class MainServerListener implements ServerListener {

    @Override
    public void notifyNewClient(ClientConnector client) {
        client.setListener(new MainClientListener());
        client.start();
    }
}

public class App {

    public static void main(String[] args) throws IOException {
        String ip = "0.0.0.0";
        int port = 1234;
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(ip, port));
        ServerConnector serverConnector = new ServerConnector(serverSocket);
        serverConnector.setListener(new MainServerListener());
        serverConnector.start();
        System.err.println("ready");
    }
}
