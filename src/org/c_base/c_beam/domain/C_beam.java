package org.c_base.c_beam.domain;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

//import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
//import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
//import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
//import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;
import org.alexd.jsonrpc.JSONRPCClient;
import org.alexd.jsonrpc.JSONRPCException;
import org.alexd.jsonrpc.JSONRPCParams;
import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.text.format.Formatter;
import android.util.Log;

public class C_beam {
    enum RESULTS {
        SUCCESS("success"),
        FAILURE("failure");
        private final String stringValue;
        private RESULTS(final String s) { stringValue = s; }
        public String toString() { return stringValue; }
    }

    private static final String TAG = "c-beam";

    private static final String C_BEAM_URL = "http://10.0.1.27:4254/rpc/";
    private static final String C_PORTAL_URL = "https://c-portal.c-base.org/rpc/";
    private static final String ETA_URL = "http://shell.c-base.org:4255/rpc/";

    private JSONRPCClient c_beamClient;
    private JSONRPCClient portalClient;
    private JSONRPCClient etaClient;

    private ArrayList<User> onlineList = new ArrayList<User>();
    private ArrayList<User> offlineList = new ArrayList<User>();
    private ArrayList<User> etaList = new ArrayList<User>();
    private ArrayList<Mission> missions = new ArrayList<Mission>();
    private ArrayList<User> users = new ArrayList<User>();
    private ArrayList<Event> events = new ArrayList<Event>();
    private ArrayList<Artefact> artefactList = new ArrayList<Artefact>();
    private ArrayList<Article> articleList = new ArrayList<Article>();
    private ArrayList<User> stats = new ArrayList<User>();

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
            c_beamUrl = sharedPref.getString(Settings.C_BEAM_URL, C_BEAM_URL);
        }
        Log.i(TAG, "using c-beam url "+ c_beamUrl);
        c_beamClient = JSONRPCClient.create(c_beamUrl, JSONRPCParams.Versions.VERSION_2);
        c_beamClient.setConnectionTimeout(10000);
        c_beamClient.setSoTimeout(10000);
        portalClient = JSONRPCClient.create(C_PORTAL_URL, JSONRPCParams.Versions.VERSION_2);
        portalClient.setConnectionTimeout(10000);
        portalClient.setSoTimeout(10000);

        etaClient = JSONRPCClient.create(ETA_URL, JSONRPCParams.Versions.VERSION_2);
        etaClient.setConnectionTimeout(10000);
        etaClient.setSoTimeout(10000);


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
                while(!stop) {
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
            Log.i(TAG, "no activity set");
            return true;
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        if (sharedPref.getBoolean(Settings.DEBUG_ENABLED, false) || debug)
            return true;

        WifiManager wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wifiManager.getDhcpInfo().ipAddress);
        return wifiManager.isWifiEnabled() && (ip.startsWith("42.42.") || ip.startsWith("10.0."));
    }

    private void updateLists() {
        try {
            JSONObject result = c_beamClient.callJSONObject("app_data");
            updateUserLists(result.getJSONArray("user"));
            updateEvents(result.getJSONArray("events"));
            updateArtefacts(result.getJSONArray("artefacts"));
            updateMissions(result.getJSONArray("missions"));
            updateArticles(result.getJSONArray("articles"));
            updateActivitylog(result.getJSONArray("activitylog"));
            updateStats(result.getJSONArray("stats"));
            sleepTime = 5000;
        } catch (Exception e) {
            Log.i(TAG, "updateLists failed");
            initC_beamClient();
            //e.printStackTrace();
        }
    }

//    public String testJsonRPC2() {
//
//        // Create new JSON-RPC 2.0 client session
//        //		try {
//        //			portalSession = new JSONRPC2Session(new URL("https://c-portal.c-base.org/rpc/"));
//        //			portalSession.getOptions().trustAllCerts(true);
//        //		} catch (MalformedURLException e) {
//        //			e.printStackTrace();
//        //		}
//
//        //		String method = "list_articles";
//        //		int requestID = 0;
//
//        //		try {
//        //			Log.i(TAG,portalClient.callJSONArray("list_articles").toString());
//        //
//        //		} catch (JSONRPCException e) {
//        //			// TODO Auto-generated catch block
//        //			e.printStackTrace();
//        //		}
//
//        URL serverURL = null;
//
//        try {
//            serverURL = new URL("http://10.0.1.27:4254/rpc/");
//
//        } catch (MalformedURLException e) {
//            // handle exception...
//        }
//
//// Create new JSON-RPC 2.0 client session
//        JSONRPC2Session mySession = new JSONRPC2Session(serverURL);
//
//        JSONRPC2Request request = new JSONRPC2Request("who", 0);
//        JSONRPC2Response response = null;
//        try {
//            response = mySession.send(request);
//
//        } catch (JSONRPC2SessionException e) {
//
//            System.err.println(e.getMessage());
//            // handle exception...
//        }
//
//        // Print response result / error
//        if (response.indicatesSuccess())
//            System.out.println(response.getResult());
//        else
//            System.out.println(response.getError().getMessage());
//        return "success";
//    }

    private void updateStats(JSONArray statsResult) throws JSONException {
        ArrayList<User> statsList = new ArrayList<User>();
        for (int i=0; i<statsResult.length(); i++) {
            JSONObject item = statsResult.getJSONObject(i);
            statsList.add(new User(item));
        }
        this.stats = statsList;
    }

    private void updateActivitylog(JSONArray activitylogResult)
            throws JSONException {
        ArrayList<ActivityLog> activitylogList = new ArrayList<ActivityLog>();
        for (int i=0; i<activitylogResult.length(); i++) {
            JSONObject item = activitylogResult.getJSONObject(i);
            activitylogList.add(new ActivityLog(item));
        }
        this.activitylog = activitylogList;
    }

    private void updateArticles(JSONArray articleResult) throws JSONException {
        ArrayList<Article> articleList = new ArrayList<Article>();
        for (int i=0; i<articleResult.length(); i++) {
            JSONObject item = articleResult.getJSONObject(i);
            articleList.add(new Article(item));
        }
        this.articleList = articleList;
    }

    private void updateMissions(JSONArray missionResult) throws JSONException {
        ArrayList<Mission> missionList = new ArrayList<Mission>();
        for (int i=0; i<missionResult.length(); i++) {
            JSONObject item = missionResult.getJSONObject(i);
            missionList.add(new Mission(item));
        }
        this.missions = missionList;
    }

    private void updateArtefacts(JSONArray artefactsResult)
            throws JSONException {
        ArrayList<Artefact> artefactList = new ArrayList<Artefact>();
        for (int i=0; i<artefactsResult.length(); i++) {
            JSONObject item = artefactsResult.getJSONObject(i);
            artefactList.add(new Artefact(item));
        }
        this.artefactList = artefactList;
    }

    private void updateEvents(JSONArray eventsResult)
            throws JSONException {
        ArrayList<Event> eventList = new ArrayList<Event>();
        for (int i=0; i<eventsResult.length(); i++) {
            JSONObject item = eventsResult.getJSONObject(i);
            eventList.add(new Event(item));
        }
        if (eventList.size() == 0) {
//			eventList.add(new Event(Resources.getSystem().getString(org.c_base.c_beam.R.string.no_events)));
            eventList.add(new Event("fu:r heute sind keine events eingetragen"));
        }
        this.events = eventList;
    }

    private void updateUserLists(JSONArray userResult) throws JSONException {
        ArrayList<User> userList = new ArrayList<User>();
        for (int i=0; i<userResult.length(); i++) {
            JSONObject item = userResult.getJSONObject(i);
            userList.add(new User(item));
        }
        this.users = userList;

        onlineList.clear();
        offlineList.clear();
        etaList.clear();

        for (User user: users) {
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

    public synchronized JSONObject who() {
        JSONObject result = null;
        try {
            if (isInCrewNetwork())
                result = c_beamClient.callJSONObject("who");
        } catch (JSONRPCException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public synchronized User getUser(int id) {
        for(User user: users) {
            if (user.getId() == id) {
                return user;
            }
        }
        // TODO this can cause a NetworkOnMainThreadException, can it be replaced by the code above?
        Log.e(TAG, "user not in user list, doing an extra call");
        User u = null;
        try {
            if (isInCrewNetwork()) {
                JSONObject item = c_beamClient.callJSONObject("get_user_by_id", id);
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
        for(User user: users) {
            if (user.getUsername().contentEquals(username)) {
                return user;
            }
        }
        User u = null;
        try {
            if (isInCrewNetwork()) {
                JSONObject item = c_beamClient.callJSONObject("get_user_by_name", username);
                u = new User(item);
            }
        } catch (Exception e) {
            // c-beam call failed for some reason, we can continue and return null
        }
        return u;
    }

    public ArrayList<Event> getEvents(){
        return events;
    }

    public synchronized ArrayList<Mission> getMissions() {
        return missions;
    }

    public synchronized ArrayList<ActivityLog> getActivityLog() {
        return activitylog;
    }

    public synchronized Mission getMission(int id) {
        for (Mission mission: missions) {
            if (mission.getId() == id) {
                return mission;
            }
        }

        Mission m = null;
        try {
            if (isInCrewNetwork()) {
                JSONObject item = c_beamClient.callJSONObject("mission_detail", id);
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
        try {
            if (isInCrewNetwork()) {
                result = c_beamClient.callString("mission_assign", user, id);
            }
        } catch (JSONRPCException e) {
            e.printStackTrace();
        }
        return result;
    }

    public synchronized String completeMission(int id) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        String user = sharedPref.getString(Settings.USERNAME, "bernd");
        String result = "";
        try {
            if (isInCrewNetwork()) {
                result = c_beamClient.callString("mission_complete", user, id);
            }
        } catch (JSONRPCException e) {
            e.printStackTrace();
        }
        return result;
    }

    public synchronized String cancelMission(int id) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        String user = sharedPref.getString(Settings.USERNAME, "bernd");
        String result = "";
        try {
            if (isInCrewNetwork()) {
                result = c_beamClient.callString("mission_cancel", user, id);
            }
        } catch (JSONRPCException e) {
            e.printStackTrace();
        }
        return result;
    }

    public ArrayList<User> getStats() {
        return stats;
    }

    public synchronized String register(String regId, String user) {
        String result = "failure";
        try {
            if (isInCrewNetwork()) {
                Log.i(TAG, "register: "+user+":"+regId);
                result = c_beamClient.callJSONObject("gcm_register", user, regId).getString("result");
            }
        } catch (JSONRPCException e) {
            result = "JSONRPCException";
        } catch (JSONException e) {
            result = "JSONException";
        }
        return result;
    }

    public synchronized String register_update(String regId, String user) {
        String result = "failure";
        try {
            if (isInCrewNetwork()) {
                Log.i(TAG, "registerUpdate("+user+", " +regId+")");
                result = c_beamClient.callJSONObject("gcm_update", user, regId).getString("result");
            }
        } catch (JSONRPCException e) {
            result = "JSONRPCException";
        } catch (JSONException e) {
            result = "JSONException";
        }
        return result;
    }

    public synchronized void login(String user) {
        try {
            if (isInCrewNetwork()) {
                c_beamClient.call("login", user);
                for(int i=0;i<offlineList.size();i++) {
                    if (offlineList.get(i).getUsername().equals(user)) {
                        offlineList.get(i).setStatus("online");
                        onlineList.add(offlineList.get(i));
                        offlineList.remove(i);
                    }
                }
            }
        } catch (JSONRPCException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public synchronized void logout(String user) {
        try {
            if (isInCrewNetwork()) {
                c_beamClient.call("logout", user);
                for(int i=0;i<onlineList.size();i++) {
                    if (onlineList.get(i).getUsername().equals(user)) {
                        onlineList.get(i).setStatus("offline");
                        offlineList.add(onlineList.get(i));
                        onlineList.remove(i);
                    }
                }
            }
        } catch (JSONRPCException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public synchronized void toggleLogin(String user) {
        try {
            if (isInCrewNetwork()) {
                c_beamClient.call("tagevent", user);
            }
        } catch (JSONRPCException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public synchronized void force_login(String user) {
        try {
            if (isInCrewNetwork()) {
                c_beamClient.call("force_login", user);
                for(int i=0;i<offlineList.size();i++) {
                    if (offlineList.get(i).getUsername().equals(user)) {
                        offlineList.get(i).setStatus("online");
                        onlineList.add(offlineList.get(i));
                        offlineList.remove(i);
                    }
                }
            }
        } catch (JSONRPCException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public synchronized void force_logout(String user) {
        try {
            if (isInCrewNetwork()) {
                c_beamClient.call("force_logout", user);
                for(int i=0;i<onlineList.size();i++) {
                    if (onlineList.get(i).getUsername().equals(user)) {
                        onlineList.get(i).setStatus("offline");
                        offlineList.add(onlineList.get(i));
                        onlineList.remove(i);
                    }
                }
            }
        } catch (JSONRPCException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
        try {
            Log.d(TAG,"tts("+text+")");
            if (isInCrewNetwork())
                c_beamClient.call("tts", "julia", text);
        } catch (JSONRPCException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public synchronized void r2d2(String text) {
        try {
            Log.d(TAG,"r2d2("+text+")");
            if (isInCrewNetwork())
                c_beamClient.call("tts", "r2d2", text);
        } catch (JSONRPCException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public synchronized ArrayList<String> getSounds() {
        ArrayList<String> result = new ArrayList<String>();
        try {
            if (isInCrewNetwork()) {
                System.out.println("calling sounds");
                JSONObject res = c_beamClient.callJSONObject("sounds");
                System.out.println("calling sounds done");
                JSONArray items = res.getJSONArray("result");
                for(int i=0; i<items.length();i++)
                    result.add(items.getString(i));
            }
        } catch (JSONRPCException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    public synchronized void play(String sound) {
        try {
            Log.i(TAG,"play("+sound+")");
            if (isInCrewNetwork())
                c_beamClient.call("play", sound);
        } catch (JSONRPCException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public synchronized void announce(String text) {
        try {
            Log.i(TAG,"announce("+text+")");
            if (isInCrewNetwork())
                c_beamClient.call("announce", text);
        } catch (JSONRPCException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<Article> getArticles() {
        return articleList;
    }

    public ArrayList<Artefact> getArtefacts() {
        return artefactList;
    }

    public synchronized void bluewall() {
        try {
            if (isInCrewNetwork())
                c_beamClient.callJSONObject("bluewall");
        } catch (JSONRPCException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public synchronized void darkwall() {
        try {
            if (isInCrewNetwork())
                c_beamClient.callJSONObject("darkwall");
        } catch (JSONRPCException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public synchronized void hwstorage(boolean b) {
        try {
            if (isInCrewNetwork())
                Log.i("c-beam", "hw-storage");
            c_beamClient.callJSONObject("hwstorage", b);
        } catch (JSONRPCException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public synchronized void stopThread() {
        if (thread != null) {
            thread.interrupt();
        }
    }

    public synchronized String set_stripe_pattern(int pattern) {
        String result = "failure";
        try {
            if (isInCrewNetwork()) {
                result = c_beamClient.callJSONObject("set_stripe_pattern", pattern).getString("result");
            }
        } catch (JSONRPCException e) {
            result = "JSONRPCException";
        } catch (JSONException e) {
            result = "JSONException";
        }
        return result;
    }

    public synchronized String set_stripe_speed(int speed) {
        String result = "failure";
        try {
            if (isInCrewNetwork()) {
                result = c_beamClient.callJSONObject("set_stripe_speed", speed).getString("result");
            }
        } catch (JSONRPCException e) {
            result = "JSONRPCException";
        } catch (JSONException e) {
            result = "JSONException";
        }
        return result;
    }

    public synchronized String set_stripe_offset(int offset) {
        String result = "failure";
        try {
            if (isInCrewNetwork()) {
                result = c_beamClient.callJSONObject("set_stripe_offset", offset).getString("result");
            }
        } catch (JSONRPCException e) {
            result = "JSONRPCException";
        } catch (JSONException e) {
            result = "JSONException";
        }
        return result;
    }

    public synchronized String notbeleuchtung() {
        String result = "failure";
        try {
            if (isInCrewNetwork()) {
                result = c_beamClient.callJSONObject("notbeleuchtung").getString("result");
            }
        } catch (JSONRPCException e) {
            result = "JSONRPCException";
        } catch (JSONException e) {
            result = "JSONException";
        }
        return result;
    }

    public synchronized String set_stripe_default() {
        String result = "failure";
        try {
            if (isInCrewNetwork()) {
                result = c_beamClient.callJSONObject("set_pattern_default").getString("result");
            }
        } catch (JSONRPCException e) {
            result = "JSONRPCException";
        } catch (JSONException e) {
            result = "JSONException";
        }
        return result;
    }

    public synchronized boolean isStatsEnabled() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        String user = sharedPref.getString(Settings.USERNAME, "bernd");
        User u;
        try {
            if (isInCrewNetwork()) {
                JSONObject item = c_beamClient.callJSONObject("get_user_by_name", user);
                u = new User(item);
                return u.isStats_enabled();
            }
        } catch (JSONRPCException e) {
            e.printStackTrace();
        }
        return false;
    }

    public synchronized String setStatsEnabled(boolean stats_enabled) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        String user = sharedPref.getString(Settings.USERNAME, "bernd");
        String result = "failure";
        try {
            if (isInCrewNetwork()) {
                result = c_beamClient.callJSONObject("set_stats_enabled", user, stats_enabled).getString("result");
            }
        } catch (JSONRPCException e) {
            result = "JSONRPCException";
        } catch (JSONException e) {
            result = "JSONException";
        }
        return result;
    }

    public synchronized String logactivity(String activity, String ap_string) {
        String result = "failure";
        int ap = Integer.parseInt(ap_string);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.activity);
        String user = sharedPref.getString(Settings.USERNAME, "bernd");
        try {
            if (isInCrewNetwork()) {
                result = c_beamClient.callJSONObject("logactivity", user, activity, ap).getString("result");
            }
        } catch (JSONRPCException e) {
            result = "JSONRPCException";
        } catch (JSONException e) {
            result = "JSONException";
        }
        return result;
    }

    public synchronized boolean isLoggedIn(String user) {
        User u;
        try {
            if (isInCrewNetwork()) {
                JSONObject item = c_beamClient.callJSONObject("get_user_by_name", user);
                u = new User(item);
                return u.getStatus().equals("online");
            }
        } catch (JSONRPCException e) {
            e.printStackTrace();
        }
        return false;
    }

    public synchronized String setPushMissions(Boolean newValue) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        String user = sharedPref.getString(Settings.USERNAME, "bernd");
        String result = "failure";
        try {
            if (isInCrewNetwork()) {
                //String result = c_beamClient.callString("set_push_missions", user, newValue);
                result = c_beamClient.callJSONObject("set_push_missions", user, newValue).getString("result");
            }
        } catch (JSONRPCException e) {
            result = "JSONRPCException";
        } catch (JSONException e) {
            result = "JSONException";
        }
        return result;
    }

    public synchronized String setPushBoarding(Boolean newValue) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        String user = sharedPref.getString(Settings.USERNAME, "bernd");
        String result = "failure";
        try {
            if (isInCrewNetwork()) {
                result = c_beamClient.callJSONObject("set_push_boarding", user, newValue).getString("result");
            }
        } catch (JSONRPCException e) {
            result = "JSONRPCException";
        } catch (JSONException e) {
            result = "JSONException";
        }
        return result;
    }

    public synchronized String setPushETA(Boolean newValue) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        String user = sharedPref.getString(Settings.USERNAME, "bernd");
        String result = "failure";
        try {
            if (isInCrewNetwork()) {
                result = c_beamClient.callJSONObject("set_push_eta", user, newValue).getString("result");
            }
        } catch (JSONRPCException e) {
            result = "JSONRPCException";
        } catch (JSONException e) {
            result = "JSONException";
        }
        return result;
    }

    public synchronized String setETA(String user, String eta) {
        String result = "failure";
        try {
            result = etaClient.callJSONObject("eta", user, eta).getString("result");
        } catch (JSONRPCException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public synchronized String call(String method, String param1) {
        String result = "failure";
        try {
            if (isInCrewNetwork()) {
                Log.i(TAG, "calling " + method);
                result = etaClient.callJSONObject(method, param1).getString("result");
            } else {
                result = "not in crew network";
            }
        } catch (JSONRPCException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public synchronized String call(String method, String param1, String param2) {
        String result = "failure";
        try {
            if (isInCrewNetwork()) {
                Log.i(TAG, "calling " + method);
                result = c_beamClient.callString(method, param1, param2);
            } else {
                result = "not in crew network";
            }
        } catch (JSONRPCException e) {
            e.printStackTrace();
//        } catch (JSONException e) {
//            e.printStackTrace();
        }
        return result;
    }

}
