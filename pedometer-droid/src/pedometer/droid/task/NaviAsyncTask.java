package pedometer.droid.task;

import android.os.AsyncTask;
import pedometer.droid.DroidMain;

public abstract class NaviAsyncTask<Params> extends AsyncTask<Params, Void, Void> {

    private final DroidMain main;

    public NaviAsyncTask(DroidMain main) {
        this.main = main;
    }

    public DroidMain getMain() {
        return main;
    }

    @Override
    protected void onPreExecute() {
        main.showDialog(DroidMain.PLEASE_WAIT_DIALOG);
    }

    @Override
    protected void onPostExecute(Void result) {
        main.removeDialog(DroidMain.PLEASE_WAIT_DIALOG);
    }
}
