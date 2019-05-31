package org.c_base.c_beam.task;

import android.os.AsyncTask;
import android.util.Log;

import org.c_base.c_beam.domain.C_beam;

public  class AsyncC_beamTask extends AsyncTask<String, Void, String> {

    public static final String TAG = "c-beam";
    private Exception exception;

    protected String doInBackground(String... params) {
        String result = "undefined";
        try {
            System.out.println(params[0]);
            Log.d(TAG, "calling " + params[0]);
            result = C_beam.getInstance().call(params[0], params[1], params[2], params[3], params[4]);
            Log.d(TAG, "result for " + params[0] + ": " + result);
        } catch (Exception e) {
            this.exception = e;
            Log.e(TAG, e.toString());
            return null;
        } finally {
            return result;
        }

    }

    protected void onPostExecute(String result) {
    }
}
