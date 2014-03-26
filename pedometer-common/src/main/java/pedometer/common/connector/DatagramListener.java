package pedometer.common.connector;

public interface DatagramListener {

    void notifyReceived(byte[] object);

    void notifyDisconnected();
}
