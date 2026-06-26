package org.c_base.c_beam.domain;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.format.Formatter;
import android.util.Log;

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
	private final ArrayList<String> sounds = new ArrayList<>();

	private static final String TAG = "c-beam";

	private static final String C_BEAM_URL = "https://c-beam.cbrp3.c-base.org/rpc/";
	private static final String ETA_URL = "https://shell.c-base.org/rpc/";

	private JSONRPC2Session etaClient;
	private JSONRPC2Session c_beamClient;
	private final ArrayList<User> onlineList = new ArrayList<>();
	private final ArrayList<User> offlineList = new ArrayList<>();
	private final ArrayList<User> etaList = new ArrayList<>();
	private final ArrayList<Mission> missions = new ArrayList<>();
	private final ArrayList<User> users = new ArrayList<>();
	private final ArrayList<Event> events = new ArrayList<>();
	private final ArrayList<Artefact> artefactList = new ArrayList<>();
	private final ArrayList<Article> articleList = new ArrayList<>();
	private final ArrayList<User> stats = new ArrayList<>();

	private Activity activity;

	private final int sleepTime = 1000;

	private Thread thread;
	private ArrayList<ActivityLog> activitylog;

	private static final C_beam instance = new C_beam();

	private C_beam() {
	}

	public void initC_beamClient() {
		String c_beamUrl = C_BEAM_URL;

		if (activity != null) {
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
			c_beamUrl = sharedPref.getString(Settings.C_BEAM_URL, C_BEAM_URL);
		}

		c_beamClient = createClientSession(c_beamUrl);
		etaClient = createClientSession(ETA_URL);
	}

	private JSONRPC2Session createClientSession(String urlString) {
		URL serverURL = null;
		try {
			serverURL = new URL(urlString);
		} catch (MalformedURLException e) {
			Log.e(TAG, "Malformed URL: " + urlString, e);
		}
		return new JSONRPC2Session(serverURL);
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
		if (thread != null && thread.isAlive()) {
			return;
		}
		thread = new Thread(() -> {
            boolean stop = false;
            while (!stop) {
                if (isInCrewNetwork()) {
                    updateLists();
                }
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    stop = true;
                }
            }
        });
		thread.start();
	}

	public boolean isInCrewNetwork() {
		if (activity == null) return false;
		WifiManager wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		String ip = Formatter.formatIpAddress(wifiManager.getDhcpInfo().ipAddress);
		return ip.startsWith("10.0.1.") || ip.startsWith("42.42.");
	}

	private synchronized Object c_beamCall(String method, Map<String, Object> params) {
		JSONRPC2Request request = new JSONRPC2Request(method, params, 1);
		JSONRPC2Response response;

		try {
			response = c_beamClient.send(request);
		} catch (JSONRPC2SessionException e) {
			Log.e(TAG, "c_beamCall failed", e);
			return null;
		}

		if (response.indicatesSuccess())
			return response.getResult();
		else
			return null;
	}

	private JSONObject etaCall(String method, Map<String, Object> params) {
		JSONRPC2Request request = new JSONRPC2Request(method, params, 1);
		JSONRPC2Response response;

		try {
			response = etaClient.send(request);
		} catch (JSONRPC2SessionException e) {
			Log.e(TAG, "etaCall failed", e);
			return null;
		}

		return (JSONObject) response.getResult();
	}

	public void updateLists() {
		try {
			JSONObject result = (JSONObject) c_beamCall("get_all_unified", new HashMap<>());
			if (result != null) {
				if (result.containsKey("user")) updateUserLists((JSONArray) result.get("user"));
				if (result.containsKey("events")) updateEvents((JSONArray) result.get("events"));
				if (result.containsKey("artefacts")) updateArtefacts((JSONArray) result.get("artefacts"));
				if (result.containsKey("missions")) updateMissions((JSONArray) result.get("missions"));
				if (result.containsKey("articles")) updateArticles((JSONArray) result.get("articles"));
				if (result.containsKey("activitylog")) updateActivitylog((JSONArray) result.get("activitylog"));
				if (result.containsKey("stats")) updateStats((JSONArray) result.get("stats"));
				if (result.containsKey("sounds")) updateSounds((JSONArray) result.get("sounds"));
			}
		} catch (Exception e) {
			Log.e(TAG, "updateLists failed", e);
		}
	}

	private void updateStats(JSONArray statsResult) {
		stats.clear();
		for (Object o : statsResult) {
			stats.add(new User((JSONObject) o));
		}
	}

	private void updateActivitylog(JSONArray activitylogResult) {
		ArrayList<ActivityLog> activitylogList = new ArrayList<>();
		for (Object o : activitylogResult) {
			activitylogList.add(new ActivityLog((JSONObject) o));
		}
		this.activitylog = activitylogList;
	}

	private void updateArticles(JSONArray articleResult) {
		articleList.clear();
		for (Object o : articleResult) {
			articleList.add(new Article((JSONObject) o));
		}
	}

	private void updateMissions(JSONArray missionResult) {
		missions.clear();
		for (Object o : missionResult) {
			missions.add(new Mission((JSONObject) o));
		}
	}

	private void updateArtefacts(JSONArray artefactResult) {
		artefactList.clear();
		for (Object o : artefactResult) {
			artefactList.add(new Artefact((JSONObject) o));
		}
	}

	private void updateEvents(JSONArray eventResult) {
		events.clear();
		for (Object o : eventResult) {
			events.add(new Event((JSONObject) o));
		}
		if (events.isEmpty()) {
			events.add(new Event("keine events heute"));
		}
	}

	private void updateSounds(JSONArray soundResult) {
		sounds.clear();
		for (Object o : soundResult) {
			sounds.add((String) o);
		}
	}

	private void updateUserLists(JSONArray userResult) {
		users.clear();
		onlineList.clear();
		offlineList.clear();
		etaList.clear();
		for (Object o : userResult) {
			User user = new User((JSONObject) o);
			users.add(user);
			if (user.getStatus().equals("online")) {
				onlineList.add(user);
			} else if (user.getStatus().equals("eta")) {
				etaList.add(user);
			} else if (user.getStatus().equals("offline")) {
				offlineList.add(user);
			}
		}
		if (onlineList.isEmpty() && etaList.isEmpty()) {
			onlineList.add(new User("Niemand an Bord"));
		}
	}

	public ArrayList<User> getUsers() {
		return users;
	}

	public User getUser(int id) {
		for (User u : users) {
			if (u.getId() == id) {
				return u;
			}
		}

		Map<String, Object> params = new HashMap<>();
		params.put("id", id);
		JSONObject item = (JSONObject) c_beamCall("get_user_by_id", params);
		if (item != null) {
            return new User(item);
        }
		return null;
	}

	public User getCurrentUser() {
		if (activity == null) return null;
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
		String username = sharedPref.getString(Settings.USERNAME, "bernd");
		for (User u : users) {
			if (u.getUsername().equals(username)) {
				return u;
			}
		}
		return null;
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

	public Mission getMission(int id) {
		for (Mission m : missions) {
			if (m.getId() == id) {
				return m;
			}
		}
		return null;
	}

	public synchronized String assignMission(int id) {
		String result = "failure";
		if (isInCrewNetwork()) {
			Map<String, Object> params = new HashMap<>();
			params.put("mission_id", id);
            JSONObject res = (JSONObject) c_beamCall("assign_mission", params);
            if (res != null) {
                result = (String) res.get("result");
            }
		}
		return result;
	}

	public synchronized String completeMission(int id) {
		String result = "failure";
		if (isInCrewNetwork()) {
			Map<String, Object> params = new HashMap<>();
			params.put("mission_id", id);
            JSONObject res = (JSONObject) c_beamCall("complete_mission", params);
            if (res != null) {
                result = (String) res.get("result");
            }
		}
		return result;
	}

	public synchronized String cancelMission(int id) {
		String result = "failure";
		if (isInCrewNetwork()) {
			Map<String, Object> params = new HashMap<>();
			params.put("mission_id", id);
            JSONObject res = (JSONObject) c_beamCall("cancel_mission", params);
            if (res != null) {
                result = (String) res.get("result");
            }
		}
		return result;
	}

	public ArrayList<User> getStats() {
		return stats;
	}

	public synchronized void toggleLogin(String user) {
		if (isInCrewNetwork()) {
			Map<String, String> params = new HashMap<>();
			params.put("user", user);
			callAsync("login", params);
		}
	}

	public void force_login(String user) {
		if (isInCrewNetwork()) {
			Map<String, String> params = new HashMap<>();
			params.put("user", user);
			callAsync("force_login", params);
		}
	}

	public void force_logout(String user) {
		if (isInCrewNetwork()) {
			Map<String, String> params = new HashMap<>();
			params.put("user", user);
			callAsync("force_logout", params);
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
			Map<String, String> params = new HashMap<>();
			params.put("text", text);
			callAsync("tts", params);
		}
	}

	public void r2d2(String text) {
		if (isInCrewNetwork()) {
			Map<String, String> params = new HashMap<>();
			params.put("text", text);
			callAsync("r2d2", params);
		}
	}

	public ArrayList<String> getSounds() {
		return sounds;
	}

	public void play(String sound) {
		if (isInCrewNetwork()) {
			Map<String, String> params = new HashMap<>();
			params.put("sound", sound);
			callAsync("play", params);
		}
	}

	public void announce(String text) {
		if (isInCrewNetwork()) {
			Map<String, String> params = new HashMap<>();
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

	public void bluewall() {
		callAsync("bluewall");
	}

	public void darkwall() {
		callAsync("darkwall");
	}

	public void hwstorage() {
		callAsync("hwstorage");
	}

	public void stopThread() {
		if (thread != null) {
			thread.interrupt();
		}
	}

	public synchronized String set_stripe_pattern(int pattern) {
		String result = "failure";
		if (isInCrewNetwork()) {
			Map<String, String> params = new HashMap<>();
			params.put("pattern", "" + pattern);
			callAsync("set_stripe_pattern", params);
		}
		return result;
	}

	public synchronized String set_stripe_speed(int speed) {
		String result = "failure";
		if (isInCrewNetwork()) {
			Map<String, String> params = new HashMap<>();
			params.put("speed", "" + speed);
			callAsync("set_stripe_speed", params);
		}
		return result;
	}

	public synchronized String set_stripe_offset(int offset) {
		String result = "failure";
		if (isInCrewNetwork()) {
			Map<String, String> params = new HashMap<>();
			params.put("offset", "" + offset);
			callAsync("set_stripe_offset", params);
		}
		return result;
	}

	public String notbeleuchtung() {
		callAsync("notbeleuchtung");
		return "success";
	}

	public String set_stripe_default() {
		callAsync("set_stripe_default");
		return "success";
	}

	public synchronized boolean isStatsEnabled() {
		User u = getCurrentUser();
		if (u != null) {
			return u.isStats_enabled();
		}
		return false;
	}

	public synchronized String setStatsEnabled(boolean stats_enabled) {
		String result = "failure";
		if (isInCrewNetwork()) {
			Map<String, String> params = new HashMap<>();
			params.put("stats_enabled", stats_enabled ? "True" : "False");
			callAsync("set_stats_enabled", params);
		}
		return result;
	}

	public synchronized String logactivity(String activity, String ap_string) {
		String result = "failure";

		if (isInCrewNetwork()) {
			Map<String, String> params = new HashMap<>();
			params.put("activity", activity);
			params.put("ap", ap_string);
			callAsync("logactivity", params);
		}
		return result;
	}

	public boolean isLoggedIn(String user) {
		for (User u : onlineList) {
			if (u.getUsername().equals(user)) {
				return u.getStatus().equals("online");
			}
		}
		return false;
	}

	public synchronized String setPushMissions(Boolean enabled) {
		String result = "failure";
		if (isInCrewNetwork()) {
			Map<String, String> params = new HashMap<>();
			params.put("enabled", enabled ? "True" : "False");
			callAsync("set_push_missions", params);
		}
		return result;
	}

	public synchronized String setPushBoarding(Boolean enabled) {
		String result = "failure";
		if (isInCrewNetwork()) {
			Map<String, String> params = new HashMap<>();
			params.put("enabled", enabled ? "True" : "False");
			callAsync("set_push_boarding", params);
		}
		return result;
	}

	public synchronized String setPushETA(Boolean enabled) {
		String result = "failure";
		if (isInCrewNetwork()) {
			Map<String, String> params = new HashMap<>();
			params.put("enabled", enabled ? "True" : "False");
			callAsync("set_push_eta", params);
		}
		return result;
	}

	public synchronized String setETA(String user, String eta) {
		Map<String, Object> params = new HashMap<>();
		params.put("user", user);
		params.put("eta", eta);
		JSONObject result = etaCall("eta", params);
		if (result != null) {
			return (String) result.get("result");
		}
		return "failure";
	}

	public synchronized String call(String method, String param1_name, String param1_value) {
		Map<String, Object> params = new HashMap<>();
		params.put(param1_name, param1_value);
		JSONObject res = (JSONObject) c_beamCall(method, params);
		if (res != null) {
            return (String) res.get("result");
        }
		return "failure";
	}

    public synchronized String call(String method, String param1_name, String param1_value, String param2_name, String param2_value) {
        Map<String, Object> params = new HashMap<>();
        params.put(param1_name, param1_value);
        params.put(param2_name, param2_value);
        JSONObject res = (JSONObject) c_beamCall(method, params);
        if (res != null) {
            return (String) res.get("result");
        }
        return "failure";
    }

	public void callAsync(String method) {
		callAsync(method, new HashMap<>());
	}

	public void callAsync(String method, Map<String, String> params) {
		new RPCCallTask(method).execute(params);
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

            c_beamCall(method, callParams);

			return "";
		}

	}

}
