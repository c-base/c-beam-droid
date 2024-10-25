package org.c_base.c_beam.domain;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
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

import org.c_base.c_beam.Settings;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class C_beam {
    private ArrayList<String> sounds = new ArrayList<String>();

    enum RESULTS {
        SUCCESS("success"),
        FAILURE("failure");
        private final String stringValue;

        private RESULTS(final String s) {
            stringValue = s;
        }

        public String toString() {
            return stringValue;
        }
    }

    private static final String TAG = "c-beam";

    private static final String C_BEAM_URL = "https://c-beam.cbrp3.c-base.org/rpc/";
    private static final String C_PORTAL_URL = "https://c-portal.c-base.org/rpc/";
    private static final String ETA_URL = "https://shell.c-base.org/rpc/";

    private JSONRPC2Session etaClient;
    private JSONRPC2Session c_beamClient;
    private ArrayList<User> onlineList = new ArrayList<User>();
    private ArrayList<User> offlineList = new ArrayList<User>();
    private ArrayList<User> etaList = new ArrayList<User>();
    private ArrayList<Mission> missions = new ArrayList<Mission>();
    private ArrayList<User> users = new ArrayList<User>();
    private ArrayList<Event> events = new ArrayList<Event>();
    private ArrayList<Artefact> artefactList = new ArrayList<Artefact>();
    private ArrayList<Article> articleList = new ArrayList<Article>();
    private ArrayList<User> stats = new ArrayList<User>();
    private boolean barStatus = false;

    private Activity activity;

    private int sleepTime = 1000;

    private Thread thread;
    private ArrayList<ActivityLog> activitylog;

    private boolean debug = false;

    private static C_beam instance = new C_beam();

    private C_beam() {
        initC_beamClient();
    }

    /**
     *
     */
    private void initC_beamClient() {
        String c_beamUrl = C_BEAM_URL;

        if (activity != null) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
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

        JSONRPC2Session session = new JSONRPC2Session(serverURL);
        session.getOptions().trustAllCerts(true);
        return session;
    }

    public static C_beam getInstance() {
        return instance;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
        initC_beamClient();
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
        if (activity == null) {
            Log.e(TAG, "no activity set");
            return true;
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        if (sharedPref.getBoolean(Settings.DEBUG_ENABLED, false) || debug)
            return true;

        WifiManager wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wifiManager.getDhcpInfo().ipAddress);
        return wifiManager.isWifiEnabled() && (ip.startsWith("42.42.") || ip.startsWith("10.0."));
    }

    private Object c_beamCall(String method, Map<String, Object> params) {

        JSONRPC2Request request = null;
        if (params == null) {
            request = new JSONRPC2Request(method, 0);
        } else {
            request = new JSONRPC2Request(method, params, 0);
        }

        JSONRPC2Response response = null;
        try {
            response = c_beamClient.send(request);
        } catch (JSONRPC2SessionException e) {
            System.err.println(e.getMessage());
            // handle exception...
        }

        // Print response result / error
        if (response != null && response.indicatesSuccess()) {
            System.out.println(response.getResult());
            return response.getResult();
        }
        if (response != null) {
            System.out.println(response.getError().getMessage());
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
            response = etaClient.send(request);

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
                updateArticles((JSONArray) result.get("articles"));
                updateActivitylog((JSONArray) result.get("activitylog"));
                updateStats((JSONArray) result.get("stats"));
                updateSounds((JSONArray) result.get("sounds"));
                updateBarStatus((boolean) result.get("barstatus"));
                sleepTime = 5000;
                Log.i(TAG, "updateLists successful");
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

    private void updateArticles(JSONArray articleResult) {
        ArrayList<Article> articleList = new ArrayList<Article>();
        for (int i = 0; i < articleResult.size(); i++) {
            articleList.add(new Article((JSONObject) articleResult.get(i)));
        }
        this.articleList = articleList;
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

        onlineList.clear();
        offlineList.clear();
        etaList.clear();

        for (User user : users) {
            if (user.getStatus().equals("online")) {
                onlineList.add(user);
            }
            if (user.getStatus().equals("eta")) {
                etaList.add(user);
            }
            if (user.getStatus().equals("offline")) {
                offlineList.add(user);
            }
        }

        if (onlineList.size() == 0 && etaList.size() == 0) {
            onlineList.add(new User("Niemand da"));
        }
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public synchronized User getUser(int id) {
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
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
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

    public synchronized ArrayList<Mission> getMissions() {
        return missions;
    }

    public synchronized ArrayList<ActivityLog> getActivityLog() {
        return activitylog;
    }

    public synchronized Mission getMission(int id) {
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

    public synchronized String assignMission(int id) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
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

    public synchronized String completeMission(int id) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
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

    public synchronized String cancelMission(int id) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
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

    public synchronized String register(String regId, String user) {
        String result = "failure";
        if (isInCrewNetwork()) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("user", user);
            params.put("regId", regId);
            result = (String) c_beamCall("gcm_register", params);
        }
        return result;
    }

    public synchronized String register_update(String regId, String user) {
        String result = "failure";
        if (isInCrewNetwork()) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("user", user);
            params.put("regId", regId);
            result = (String) c_beamCall("gcm_update", params);
        }
        return result;
    }

    public synchronized void toggleLogin(String user) {
        if (isInCrewNetwork()) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("user", user);
            callAsync("tagevent", params);
        }
    }

    public synchronized void force_login(String user) {
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

    public synchronized void force_logout(String user) {
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

    public synchronized void tts(String text) {
        if (isInCrewNetwork()) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("voice", "julia");
            params.put("text", text);
            callAsync("tts", params);
        }
    }

    public synchronized void r2d2(String text) {
        if (isInCrewNetwork()) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("voice", "r2d2");
            params.put("text", text);
            callAsync("tts", params);
        }
    }

    public synchronized ArrayList<String> getSounds() {
        return sounds;
    }

    public synchronized void play(String sound) {
        if (isInCrewNetwork()) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("file", sound);
            callAsync("play", params);
        }
    }

    public synchronized void announce(String text) {
        if (isInCrewNetwork()) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("text", text);
            callAsync("announce", params);
        }

    }

    public ArrayList<Article> getArticles() {
        return articleList;
    }

    public ArrayList<Artefact> getArtefacts() {
        return artefactList;
    }

    public synchronized void bluewall() {
    }

    public synchronized void darkwall() {
    }


    public synchronized void hwstorage() {
    }

    public synchronized void stopThread() {
        if (thread != null) {
            thread.interrupt();
        }
    }

    public synchronized String set_stripe_pattern(int pattern) {
        String result = "failure";
        if (isInCrewNetwork()) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("pattern_id", String.valueOf(pattern));
            callAsync("set_stripe_pattern", params);
        }
        return result;
    }

    public synchronized String set_stripe_speed(int speed) {
        String result = "failure";
        if (isInCrewNetwork()) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("speed", String.valueOf(speed));
            callAsync("set_stripe_speed", params);
        }
        return result;
    }

    public synchronized String set_stripe_offset(int offset) {
        String result = "failure";
        if (isInCrewNetwork()) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("offset", String.valueOf(offset));
            callAsync("set_stripe_offset", params);
        }
        return result;
    }

    public synchronized String notbeleuchtung() {
        String result = "failure";
        if (isInCrewNetwork()) {
            callAsync("notbeleuchtung");
        }
        return result;
    }

    public synchronized String set_stripe_default() {
        String result = "failure";
        if (isInCrewNetwork()) {
            callAsync("set_pattern_default");
        }
        return result;
    }

    public synchronized boolean isStatsEnabled() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
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

    public synchronized String setStatsEnabled(boolean stats_enabled) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
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

    public synchronized String logactivity(String activity, String ap_string) {
        String result = "failure";

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.activity);
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

    public synchronized boolean isLoggedIn(String user) {
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

    public synchronized String setPushMissions(Boolean newValue) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
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

    public synchronized String setPushBoarding(Boolean newValue) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
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

    public synchronized String setPushETA(Boolean newValue) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
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

    public synchronized String setETA(String user, String eta) {
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

    public synchronized String call(String method, String param1_name, String param1_value) {
        String result = "failure";
        if (isInCrewNetwork()) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(param1_name, param1_value);
            result = (String) ((JSONObject) c_beamCall(method, params)).get("result");
        } else {
            result = "not in crew network";
        }
        return result;
    }

    public synchronized String call(String method, String param1_name, String param1_value, String param2_name, String param2_value) {
        String result = "failure";
        if (isInCrewNetwork()) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(param1_name, param1_value);
            params.put(param2_name, param2_value);
            result = (String) c_beamCall(method, params);
        } else {
            result = "not in crew network";
            Log.i("c-beam", "Ignoring call to " + method + ": Not in crew network");
        }
        return result;
    }

    public void callAsync(String method) {
        Map<String, String> params = new HashMap<String, String>();
        callAsync(method, params);
    }

    public void callAsync(String method, Map<String, String> map) {
        RPCCallTask rpcCallTask = new RPCCallTask();
        String[] params = new String[3];
        params[0] = method;
        int counter = 1;
        for (Map.Entry<String, String> param : map.entrySet()) {
            params[counter++] = param.getKey();
            params[counter++] = param.getValue();
        }
        rpcCallTask.execute(params);
    }

    public class RPCCallTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String result = "failed";
            Map<String, Object> params_map = new HashMap<String, Object>();
            String method = params[0];
            if (params.length == 3) {
                params_map.put(params[1], params[2]);
            }
            if (params.length == 5) {
                params_map.put(params[3], params[4]);
            }
            if (params.length == 7) {
                params_map.put(params[5], params[6]);
            }
            if (params.length == 9) {
                params_map.put(params[7], params[8]);
            }
            Object response = c_beamCall(method, params_map);
            if (response instanceof JSONObject) {
                result = ((JSONObject) response).toJSONString();
            } else if (response instanceof String) {
                result = (String) response;
            }

            return result; //c_beam.setETA(sharedPref.getString(Settings.USERNAME, "bernd"), params.length == 1 ? params[0] : getETA());
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(activity.getApplicationContext(), result, Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

}
