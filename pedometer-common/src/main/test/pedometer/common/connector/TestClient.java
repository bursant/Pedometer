package pedometer.common.connector;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class TestClient {

    public static void main(String[] args) throws IOException {
        InetAddress address = InetAddress.getByName("127.0.0.1");
        int port = 1234;
        DatagramSocket socket = new DatagramSocket();

        socket.connect(new InetSocketAddress(address, port));
        Connector client = new Connector(socket, address, port);

        client.setListener(new MainClientListener());
        client.start();
        client.send(new byte[]{0x0, 0x1, 0x2});
        client.interrupt();
    }
}
