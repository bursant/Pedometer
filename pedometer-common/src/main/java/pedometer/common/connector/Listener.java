package pedometer.common.connector;

public interface Listener {

    void notifyReceived(byte[] object);

    void notifyDisconnected();
}
