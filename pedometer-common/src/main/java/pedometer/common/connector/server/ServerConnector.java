package pedometer.common.connector.server;

import pedometer.common.connector.client.ClientConnector;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class ServerConnector extends Thread {

    private static final Logger logger = Logger.getLogger(String.valueOf(ServerConnector.class));

    private final ServerSocket serverSocket;

    private ServerListener listener;

    public ServerConnector(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void setListener(ServerListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            while (serverSocket.isBound() && !serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                try {
                    ClientConnector client = new ClientConnector(socket);
                    if (listener != null) {
                        listener.notifyNewClient(client);
                    } else {
                        logger.info("No listener, dropping client...");
                    }
                } catch (Exception e) {
                    logger.warning("Error during servicing new connection: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.warning("Error during accepting new connection: " + e.getMessage());
        }
    }
}
