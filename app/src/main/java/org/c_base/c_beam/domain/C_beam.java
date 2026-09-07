package org.c_base.c_beam.domain;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import org.c_base.c_beam.CbeamApplication;
import org.c_base.c_beam.Settings;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class C_beam {
    private volatile ArrayList<String> sounds = new ArrayList<String>();

    enum RESULTS {
        SUCCESS("success"),
        FAILURE("failure");
        private final String stringValue;

        RESULTS(final String s) {
            stringValue = s;
        }

        public String toString() {
            return stringValue;
        }
    }

    private static final String TAG = "c-beam";

    private static final String C_BEAM_URL = "https://c-beam.cbrp3.c-base.org/rpc/";
    private static final String ETA_URL = "https://shell.c-base.org/rpc/";

    // Locking model: the only monitors in this class guard the two RPC sessions, so a
    // network round trip never blocks a reader. Everything the UI reads is a volatile
    // reference that the poller swaps wholesale (see updateLists), and the wrappers below
    // either enqueue an RPCCallTask or take rpcLock/etaLock for the duration of one call.
    private final Object rpcLock = new Object();
    private final Object etaLock = new Object();
    private volatile JSONRPC2Session etaClient;
    private volatile JSONRPC2Session c_beamClient;
    // Every collection below is replaced wholesale by the poller thread and read from the
    // UI thread without a lock, so each is volatile and never mutated in place.
    private volatile ArrayList<User> onlineList = new ArrayList<User>();
    private volatile ArrayList<User> offlineList = new ArrayList<User>();
    private volatile ArrayList<User> etaList = new ArrayList<User>();
    private volatile ArrayList<Mission> missions = new ArrayList<Mission>();
    private volatile ArrayList<User> users = new ArrayList<User>();
    private volatile ArrayList<Event> events = new ArrayList<Event>();
    private volatile ArrayList<Artefact> artefactList = new ArrayList<Artefact>();
    private volatile ArrayList<User> stats = new ArrayList<User>();
    private volatile boolean barStatus = false;

    private int sleepTime = 1000;

    private volatile Thread thread;
    private volatile ArrayList<ActivityLog> activitylog;

    private final boolean debug = false;

    /**
     * Notified on the main thread each time the poller has swapped in a fresh
     * {@code app_data} payload. Screens render once per notification instead of
     * on their own timer.
     */
    public interface DataListener {
        void onDataUpdated();
    }

    private volatile DataListener dataListener;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static final C_beam instance = new C_beam();

    private C_beam() {
        initC_beamClient();
    }

    /**
     *
     */
    /**
     * The application context from {@link CbeamApplication}; null only if this singleton
     * is touched before Application.onCreate has run.
     */
    private static Context appContext() {
        return CbeamApplication.getAppContext();
    }

    private void initC_beamClient() {
        String c_beamUrl = C_BEAM_URL;

        Context context = appContext();
        if (context != null) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            if (sharedPref.getBoolean(Settings.DEBUG_ENABLED, false)) {
                c_beamUrl = sharedPref.getString(Settings.C_BEAM_URL, C_BEAM_URL);
            }
        }
        c_beamClient = createClientSession(c_beamUrl);
        //portalClient = JSONRPCClient.create(C_PORTAL_URL, JSONRPCParams.Versions.VERSION_2);
        etaClient = createClientSession(ETA_URL);
    }

    private JSONRPC2Session createClientSession(String url) {
        URL serverURL = null;
        try {
            serverURL = new URL(url);

        } catch (MalformedURLException e) {
            // handle exception...
        }

        // No trustAllCerts here: the RPC hosts serve publicly trusted
        // certificates, so the platform's own validation is what we want.
        return new JSONRPC2Session(serverURL);
    }

    public static C_beam getInstance() {
        return instance;
    }

    /**
     * Rebuilds the RPC sessions, picking up a changed debug URL preference. Screens call
     * this on resume; it replaced setActivity(), which used to keep the Activity alive in
     * this static singleton.
     */
    public void reloadConfiguration() {
        initC_beamClient();
    }

    public void setDataListener(DataListener listener) {
        dataListener = listener;
    }

    /**
     * Clears the listener only if it is still the one given. Activities call this
     * from onStop, which for the previous screen runs *after* the next screen's
     * onStart, so an unconditional clear would drop the new screen's registration.
     */
    public void removeDataListener(DataListener listener) {
        if (dataListener == listener) {
            dataListener = null;
        }
    }

    private void notifyDataUpdated() {
        final DataListener listener = dataListener;
        if (listener == null) {
            return;
        }
        mainHandler.post(() -> {
            // Re-read: the screen may have unregistered while the post was queued.
            if (dataListener == listener) {
                listener.onDataUpdated();
            }
        });
    }

    public void startThread() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean stop = false;
                while (!stop) {
                    updateLists();
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        stop = true;
                    }
                }
            }
        });
        thread.start();
    }

    public boolean isInCrewNetwork() {
        Context context = appContext();
        if (context == null) {
            Log.e(TAG, "no application context yet");
            return true;
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPref.getBoolean(Settings.DEBUG_ENABLED, false) || debug)
            return true;

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wifiManager.getDhcpInfo().ipAddress);
        return wifiManager.isWifiEnabled() && (ip.startsWith("42.42.") || ip.startsWith("10.0."));
    }

    private Object c_beamCall(String method, Map<String, Object> params) {
        JSONRPC2Request request = new JSONRPC2Request(method, params, 0);
        JSONRPC2Response response;

        try {
            synchronized (rpcLock) {
                response = c_beamClient.send(request);
            }
        } catch (JSONRPC2SessionException e) {
            Log.e(TAG, "c_beamCall failed: " + e.getMessage());
            return null;
        }

        if (response != null && response.indicatesSuccess()) {
            return response.getResult();
        }
        return null;
    }

    private JSONObject etaCall(String method, Map<String, Object> params) {

        JSONRPC2Request request = null;
        if (params == null) {
            request = new JSONRPC2Request(method, 0);
        } else {
            request = new JSONRPC2Request(method, params, 0);
        }

        JSONRPC2Response response = null;
        try {
            synchronized (etaLock) {
                response = etaClient.send(request);
            }
        } catch (JSONRPC2SessionException e) {

            System.err.println(e.getMessage());
            // handle exception...
        }

        // Print response result / error
//        if (response.indicatesSuccess())
//            System.out.println(response.getResult());
//        else
//            System.out.println(response.getError().getMessage());
        if (response != null) {
            return (net.minidev.json.JSONObject) response.getResult();
        } else {
            return null;
        }
    }

    private void updateLists() {
        try {
            JSONObject result = (JSONObject) c_beamCall("app_data", null);
            if (result != null) {
                updateUserLists((JSONArray) result.get("user"));
                updateEvents((JSONArray) result.get("events"));
                updateArtefacts((JSONArray) result.get("artefacts"));
                updateMissions((JSONArray) result.get("missions"));
                updateActivitylog((JSONArray) result.get("activitylog"));
                updateStats((JSONArray) result.get("stats"));
                updateSounds((JSONArray) result.get("sounds"));
                updateBarStatus((boolean) result.get("barstatus"));
                sleepTime = 5000;
                Log.i(TAG, "updateLists successful");
                notifyDataUpdated();
            } else {
                Log.e(TAG, "updateLists returned null");
                initC_beamClient();
            }
        } catch (Exception e) {
            Log.e(TAG, "updateLists failed");
            e.printStackTrace();
            initC_beamClient();
        }
    }

    private void updateStats(JSONArray statsResult) {
        ArrayList<User> statsList = new ArrayList<User>();
        for (int i = 0; i < statsResult.size(); i++) {
            statsList.add(new User((JSONObject) statsResult.get(i)));
        }
        this.stats = statsList;
    }

    private void updateActivitylog(JSONArray activitylogResult) {
        ArrayList<ActivityLog> activitylogList = new ArrayList<ActivityLog>();
        for (int i = 0; i < activitylogResult.size(); i++) {
            activitylogList.add(new ActivityLog((JSONObject) activitylogResult.get(i)));
        }
        this.activitylog = activitylogList;
    }


    private void updateMissions(JSONArray missionResult) {
        ArrayList<Mission> missionList = new ArrayList<Mission>();
        for (int i = 0; i < missionResult.size(); i++) {
            missionResult.get(i);
            missionList.add(new Mission((JSONObject) missionResult.get(i)));
        }
        this.missions = missionList;
    }

    private void updateArtefacts(JSONArray artefactsResult) {
        ArrayList<Artefact> artefactList = new ArrayList<Artefact>();
        for (int i = 0; i < artefactsResult.size(); i++) {
            artefactList.add(new Artefact((JSONObject) artefactsResult.get(i)));
        }
        this.artefactList = artefactList;
    }

    private void updateEvents(JSONArray eventsResult) {
        ArrayList<Event> eventList = new ArrayList<Event>();
        for (int i = 0; i < eventsResult.size(); i++) {
            eventList.add(new Event((JSONObject) eventsResult.get(i)));
        }
        if (eventList.size() == 0) {
            //eventList.add(new Event(activity.getString(R.string.no_events)));
            eventList.add(new Event("fu:r heute sind keine events eingetragen"));
        }
        this.events = eventList;
    }

    private void updateSounds(JSONArray soundsResult) {
        ArrayList<String> soundList = new ArrayList<String>();
        for (int i = 0; i < soundsResult.size(); i++) {
            soundList.add((String) soundsResult.get(i));

        }
        if (soundList.size() == 0) {
            soundList.add("hilfe, die sounds sind weg");
        }
        this.sounds = soundList;
    }

    private void updateBarStatus(boolean barStatus) {
        this.barStatus = barStatus;
    }

    public boolean getBarStatus() {
        return this.barStatus;
    }

    private void updateUserLists(JSONArray userResult) {
        ArrayList<User> userList = new ArrayList<User>();
        for (int i = 0; i < userResult.size(); i++) {
            JSONObject item = (JSONObject) userResult.get(i);
            userList.add(new User(item));
        }
        this.users = userList;

        ArrayList<User> online = new ArrayList<User>();
        ArrayList<User> offline = new ArrayList<User>();
        ArrayList<User> eta = new ArrayList<User>();

        for (User user : userList) {
            if (user.getStatus().equals("online")) {
                online.add(user);
            }
            if (user.getStatus().equals("eta")) {
                eta.add(user);
            }
            if (user.getStatus().equals("offline")) {
                offline.add(user);
            }
        }

        if (online.size() == 0 && eta.size() == 0) {
            online.add(new User("Niemand da"));
        }

        // Swap, don't mutate: the UI thread may be iterating the previous lists.
        this.onlineList = online;
        this.offlineList = offline;
        this.etaList = eta;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public User getUser(long id) {
        for (User user : users) {
            if (user.getId() == id) {
                return user;
            }
        }
        // TODO this can cause a NetworkOnMainThreadException, can it be replaced by the code above?
        Log.e(TAG, "user not in user list, doing an extra call");
        User u = null;
        try {
            if (isInCrewNetwork()) {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("id", id);
                JSONObject item = (JSONObject) c_beamCall("get_user_by_id", params);
                u = new User(item);
            }
        } catch (Exception e) {
            // c-beam call failed for some reason, we can continue and return null
        }
        return u;
    }

    public User getCurrentUser() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext());
        String username = sharedPref.getString(Settings.USERNAME, "bernd");
        for (User user : users) {
            if (user.getUsername().contentEquals(username)) {
                return user;
            }
        }
        User u = null;
        try {
            if (isInCrewNetwork()) {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("username", username);
                JSONObject item = (JSONObject) c_beamCall("get_user_by_name", params);
                u = new User(item);
            }
        } catch (Exception e) {
            // c-beam call failed for some reason, we can continue and return null
        }
        return u;
    }

    public ArrayList<Event> getEvents() {
        return events;
    }

    public ArrayList<Mission> getMissions() {
        return missions;
    }

    public ArrayList<ActivityLog> getActivityLog() {
        return activitylog;
    }

    public Mission getMission(long id) {
        for (Mission mission : missions) {
            if (mission.getId() == id) {
                return mission;
            }
        }

        Mission m = null;
        try {
            if (isInCrewNetwork()) {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("id", id);
                JSONObject item = (JSONObject) c_beamCall("mission_detail", params);
                m = new Mission(item);
            }
        } catch (Exception e) {
            // c-beam call failed for some reason, we can continue and return null
        }
        return m;
    }

    public String assignMission(long id) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext());
        String user = sharedPref.getString(Settings.USERNAME, "bernd");
        String result = "";
        if (isInCrewNetwork()) {
            //result = c_beamClient.callString("mission_assign", user, id);
            Map<String, String> params = new HashMap<String, String>();
            params.put("user", user);
            params.put("mission_id", String.valueOf(id));
            callAsync("mission_assign", params);
        }
        return result;
    }

    public String completeMission(long id) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext());
        String user = sharedPref.getString(Settings.USERNAME, "bernd");
        String result = "";
        if (isInCrewNetwork()) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("user", user);
            params.put("mission_id", String.valueOf(id));
            callAsync("mission_complete", params);
        }
        return result;
    }

    public String cancelMission(long id) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext());
        String user = sharedPref.getString(Settings.USERNAME, "bernd");
        String result = "";
        if (isInCrewNetwork()) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("user", user);
            params.put("mission_id", String.valueOf(id));
            callAsync("mission_cancel", params);
        }
        return result;
    }

    public ArrayList<User> getStats() {
        return stats;
    }

    public String register(String regId, String user) {
        String result = "failure";
        if (isInCrewNetwork()) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("user", user);
            params.put("regId", regId);
            result = (String) c_beamCall("gcm_register", params);
        }
        return result;
    }

    public String register_update(String regId, String user) {
        String result = "failure";
        if (isInCrewNetwork()) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("user", user);
            params.put("regId", regId);
            result = (String) c_beamCall("gcm_update", params);
        }
        return result;
    }

    public void toggleLogin(String user) {
        if (isInCrewNetwork()) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("user", user);
            callAsync("tagevent", params);
        }
    }

    public void force_login(String user) {
        if (isInCrewNetwork()) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("user", user);
            callAsync("force_login", params);
            for (int i = 0; i < offlineList.size(); i++) {
                if (offlineList.get(i).getUsername().equals(user)) {
                    offlineList.get(i).setStatus("online");
                    onlineList.add(offlineList.get(i));
                    offlineList.remove(i);
                }
            }
        }
    }

    public void force_logout(String user) {
        if (isInCrewNetwork()) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("user", user);
            callAsync("force_logout", params);
            for (int i = 0; i < onlineList.size(); i++) {
                if (onlineList.get(i).getUsername().equals(user)) {
                    onlineList.get(i).setStatus("offline");
                    offlineList.add(onlineList.get(i));
                    onlineList.remove(i);
                }
            }
        }
    }

    public ArrayList<User> getOnlineList() {
        return onlineList;
    }

    public ArrayList<User> getOfflineList() {
        return offlineList;
    }

    public ArrayList<User> getEtaList() {
        return etaList;
    }

    public void tts(String text) {
        if (isInCrewNetwork()) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("voice", "julia");
            params.put("text", text);
            callAsync("tts", params);
        }
    }

    public void r2d2(String text) {
        if (isInCrewNetwork()) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("voice", "r2d2");
            params.put("text", text);
            callAsync("tts", params);
        }
    }

    public ArrayList<String> getSounds() {
        return sounds;
    }

    public void play(String sound) {
        if (isInCrewNetwork()) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("file", sound);
            callAsync("play", params);
        }
    }

    public void announce(String text) {
        if (isInCrewNetwork()) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("text", text);
            callAsync("announce", params);
        }

    }


    public ArrayList<Artefact> getArtefacts() {
        return artefactList;
    }

    public void bluewall() {
    }

    public void darkwall() {
    }


    public void hwstorage() {
    }

    public void stopThread() {
        if (thread != null) {
            thread.interrupt();
        }
    }

    public String set_stripe_pattern(int pattern) {
        String result = "failure";
        if (isInCrewNetwork()) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("pattern_id", String.valueOf(pattern));
            callAsync("set_stripe_pattern", params);
        }
        return result;
    }

    public String set_stripe_speed(int speed) {
        String result = "failure";
        if (isInCrewNetwork()) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("speed", String.valueOf(speed));
            callAsync("set_stripe_speed", params);
        }
        return result;
    }

    public String set_stripe_offset(int offset) {
        String result = "failure";
        if (isInCrewNetwork()) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("offset", String.valueOf(offset));
            callAsync("set_stripe_offset", params);
        }
        return result;
    }

    public String notbeleuchtung() {
        String result = "failure";
        if (isInCrewNetwork()) {
            callAsync("notbeleuchtung");
        }
        return result;
    }

    public String set_stripe_default() {
        String result = "failure";
        if (isInCrewNetwork()) {
            callAsync("set_pattern_default");
        }
        return result;
    }

    public boolean isStatsEnabled() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext());
        String user = sharedPref.getString(Settings.USERNAME, "bernd");
        User u;
        if (isInCrewNetwork()) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("user", user);
            JSONObject item = (JSONObject) c_beamCall("get_user_by_name", params);
            u = new User(item);
            return u.isStats_enabled();
        }
        return false;
    }

    public String setStatsEnabled(boolean stats_enabled) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext());
        String user = sharedPref.getString(Settings.USERNAME, "bernd");
        String result = "failure";
        if (isInCrewNetwork()) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("user", user);
            params.put("is_enabled", String.valueOf(stats_enabled));
            callAsync("set_stats_enabled", params);
            getCurrentUser().setStats_enabled(stats_enabled);
        }
        return result;
    }

    public String logactivity(String activity, String ap_string) {
        String result = "failure";

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext());
        String user = sharedPref.getString(Settings.USERNAME, "bernd");
        if (isInCrewNetwork()) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("user", user);
            params.put("activity", activity);
            params.put("ap", ap_string);
            callAsync("logactivity", params);
        }
        return result;
    }

    public boolean isLoggedIn(String user) {
        User u;
        if (isInCrewNetwork()) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("user", user);
            JSONObject item = (JSONObject) c_beamCall("get_user_by_name", params);
            u = new User(item);
            return u.getStatus().equals("online");
        }
        return false;
    }

    public String setPushMissions(Boolean newValue) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext());
        String user = sharedPref.getString(Settings.USERNAME, "bernd");
        String result = "failure";
        if (isInCrewNetwork()) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("user", user);
            params.put("is_enabled", String.valueOf(newValue));
            callAsync("set_push_missions", params);
            getCurrentUser().setPush_missions(newValue);
        }
        return result;
    }

    public String setPushBoarding(Boolean newValue) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext());
        String user = sharedPref.getString(Settings.USERNAME, "bernd");
        String result = "failure";
        if (isInCrewNetwork()) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("user", user);
            params.put("is_enabled", String.valueOf(newValue));
            callAsync("set_push_boarding", params);
            getCurrentUser().setPush_boarding(newValue);
        }
        return result;
    }

    public String setPushETA(Boolean newValue) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext());
        String user = sharedPref.getString(Settings.USERNAME, "bernd");
        String result = "failure";
        if (isInCrewNetwork()) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("user", user);
            params.put("is_enabled", String.valueOf(newValue));
            callAsync("set_push_eta", params);
            getCurrentUser().setPush_eta(newValue);
        }
        return result;
    }

    public String setETA(String user, String eta) {
        String result = "failure";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("user", user);
        params.put("eta", eta);
        JSONObject call_result = etaCall("eta", params);
        if (call_result != null) {
            result = (String) call_result.get("result");
        }
        return result;
    }

    public String call(String method, String param1_name, String param1_value) {
        if (isInCrewNetwork()) {
            Map<String, Object> params = new HashMap<>();
            params.put(param1_name, param1_value);
            Object response = c_beamCall(method, params);
            if (response instanceof JSONObject) {
                return (String) ((JSONObject) response).get("result");
            } else if (response instanceof String) {
                return (String) response;
            }
        }
        return "failure";
    }

    public String call(String method, String param1_name, String param1_value, String param2_name, String param2_value) {
        if (isInCrewNetwork()) {
            Map<String, Object> params = new HashMap<>();
            params.put(param1_name, param1_value);
            params.put(param2_name, param2_value);
            Object response = c_beamCall(method, params);
            if (response instanceof JSONObject) {
                return (String) ((JSONObject) response).get("result");
            } else if (response instanceof String) {
                return (String) response;
            }
        }
        return "failure";
    }

    public void callAsync(String method) {
        Map<String, String> params = new HashMap<String, String>();
        callAsync(method, params);
    }

    public void callAsync(String method, Map<String, String> map) {
        new RPCCallTask(method).execute(map);
    }

    @SuppressLint("StaticFieldLeak")
    public class RPCCallTask extends AsyncTask<Map<String, String>, Void, String> {
        private final String method;

        public RPCCallTask(String method) {
            this.method = method;
        }

        @SafeVarargs
        @Override
        protected final String doInBackground(Map<String, String>... params) {
            Map<String, Object> callParams = new HashMap<>();
            if (params.length > 0 && params[0] != null) {
                callParams.putAll(params[0]);
            }
            Object response = c_beamCall(method, callParams);
            if (response instanceof JSONObject) {
                return ((JSONObject) response).toJSONString();
            } else if (response instanceof String) {
                return (String) response;
            }
            return "failure";
        }

        @Override
        protected void onPostExecute(String result) {
            Context context = appContext();
            if (context != null && !result.equals("failure")) {
                Toast.makeText(context, result, Toast.LENGTH_LONG).show();
            }
        }
    }

}
