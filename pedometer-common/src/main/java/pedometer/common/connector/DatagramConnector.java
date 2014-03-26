package pedometer.common.connector;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Logger;

public class DatagramConnector extends Thread {

    private static final Logger logger = Logger.getLogger(String.valueOf(DatagramConnector.class));

    private final static int RECEIVING_BUFFER_SIZE = 4096;

    private final DatagramSocket socket;

    private final InetAddress address;

    private final int port;

    private DatagramListener listener;

    public DatagramConnector(DatagramSocket socket, InetAddress address, int port) throws IOException {
        this.socket = socket;
        this.address = address;
        this.port = port;
    }

    public void setListener(DatagramListener listener) {
        this.listener = listener;
    }

    public void send(byte[] bytes) {
        try {
            byte[] data = new byte[bytes.length + 4];

            data[0] = (byte) (bytes.length >>> 24);
            data[1] = (byte) (bytes.length >>> 16);
            data[2] = (byte) (bytes.length >>> 8);
            data[3] = (byte) (bytes.length);

            System.arraycopy(bytes, 0, data, 4, bytes.length);

            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, port);
            socket.send(packet);
        } catch (IOException e) {
            logger.warning("Error during sending data: " + e.getMessage());

            if (listener != null) {
                listener.notifyDisconnected();
            }
        }
    }

    private byte[] receive(DatagramPacket packet) throws IOException {
        socket.receive(packet);
        byte[] data = packet.getData();

        int length = data[0] << 24 | (data[1] & 0xFF) << 16 | (data[2] & 0xFF) << 8 | (data[3] & 0xFF);
        byte[] bytes = new byte[length];

        System.arraycopy(data, 4, bytes, 0, length);

        return bytes;
    }

    @Override
    public void run() {
        DatagramPacket packet = new DatagramPacket(new byte[RECEIVING_BUFFER_SIZE], RECEIVING_BUFFER_SIZE);

        try {
            while ((socket.isConnected() || socket.isBound()) && !socket.isClosed()) {
                byte[] bytes = receive(packet);
                if (listener != null) {
                    listener.notifyReceived(bytes);
                } else {
                    logger.info("No listener, dropping data...");
                }
            }
        } catch (Exception e) {
            logger.warning("Error during receiving data: " + e.getMessage());
        }
        if (listener != null) {
            listener.notifyDisconnected();
        }
    }

    @Override
    public void interrupt() {
        try {
            socket.close();
        } finally {
            super.interrupt();
        }
    }

    @Override
    public String toString() {
        return socket.getRemoteSocketAddress() + "/" + socket.getLocalSocketAddress();
    }
}
