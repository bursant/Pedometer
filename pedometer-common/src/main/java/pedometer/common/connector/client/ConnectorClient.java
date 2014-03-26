package pedometer.common.connector.client;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

public class ConnectorClient extends Thread {

    private static final Logger logger = Logger.getLogger(String.valueOf(ConnectorClient.class));

    private final Socket socket;

    private final DataOutputStream objectOutputStream;

    private final DataInputStream objectInputStream;

    private ClientListener listener;

    public ConnectorClient(Socket socket) throws IOException {
        this.socket = socket;

        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();

        objectInputStream = new DataInputStream(inputStream);
        objectOutputStream = new DataOutputStream(outputStream);
    }

    public void setListener(ClientListener listener) {
        this.listener = listener;
    }

    public void send(byte[] bytes) {
        try {
            synchronized (objectOutputStream) {
                objectOutputStream.writeInt(bytes.length);
                objectOutputStream.write(bytes);
            }
        } catch (IOException e) {
            logger.warning("Error during sending data: " + e.getMessage());

            if (listener != null) {
                listener.notifyDisconnected();
            }
        }
    }

    private byte[] receive() throws IOException {
        int length = objectInputStream.readInt();
        byte[] bytes = new byte[length];

        int offset = objectInputStream.read(bytes, 0, length);
        while (offset < length) {
            offset += objectInputStream.read(bytes, offset, length - offset);
        }

        return bytes;
    }

    @Override
    public void run() {
        try {
            while (socket.isConnected() && !socket.isClosed()) {
                byte[] bytes = receive();
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
    public String toString() {
        return socket.getRemoteSocketAddress() + "/" + socket.getLocalSocketAddress();
    }
}
