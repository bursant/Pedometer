package pedometer.droid;

import pedometer.droid.helper.DroidNetwork;

public class DroidHandler {

    private static final DroidNetwork network = new DroidNetwork();

    private static final Object mainLock = new Object();

    private static DroidMain main;

    public static void setMain(DroidMain main) {
        synchronized (mainLock) {
            DroidHandler.main = main;
        }
    }

    public static DroidMain getMain() {
        synchronized (mainLock) {
            return main;
        }
    }

    public static DroidNetwork getNetwork() {
        return network;
    }
}
