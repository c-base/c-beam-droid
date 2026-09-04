package org.c_base.c_beam.task;

import android.os.AsyncTask;
import android.util.Log;

import org.c_base.c_beam.domain.C_beam;

public  class AsyncC_beamTask extends AsyncTask<String, Void, String> {

    public static final String TAG = "c-beam";
    private static final int EXPECTED_ARGS = 5;
    private Exception exception;

    protected String doInBackground(String... params) {
        if (params.length < EXPECTED_ARGS) {
            Log.e(TAG, "expected " + EXPECTED_ARGS + " arguments, got " + params.length);
            return null;
        }
        try {
            Log.d(TAG, "calling " + params[0]);
            String result = C_beam.getInstance().call(params[0], params[1], params[2], params[3], params[4]);
            Log.d(TAG, "result for " + params[0] + ": " + result);
            return result;
        } catch (Exception e) {
            this.exception = e;
            Log.e(TAG, "call " + params[0] + " failed", e);
            return null;
        }
    }

    protected void onPostExecute(String result) {
    }
}
