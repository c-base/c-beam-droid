package org.c_base.c_beam.domain;

import java.util.ArrayList;

import org.alexd.jsonrpc.JSONRPCClient;
import org.alexd.jsonrpc.JSONRPCException;
import org.alexd.jsonrpc.JSONRPCParams;
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
	private static final String TAG = "c-beam";
	protected JSONRPCClient c_beamClient;
	protected JSONRPCClient portalClient;
	protected Runnable runnable;

	protected ArrayList<User> onlineList = new ArrayList<User>();
	protected ArrayList<User> offlineList = new ArrayList<User>();
	protected ArrayList<User> etaList = new ArrayList<User>();
	protected ArrayList<Mission> missions = new ArrayList<Mission>();
	protected ArrayList<User> users = new ArrayList<User>();
	protected ArrayList<Event> events = new ArrayList<Event>();
	protected Activity activity;

	protected ArrayList<Artefact> artefactList = new ArrayList<Artefact>();
	protected ArrayList<Article> articleList = new ArrayList<Article>();
	protected ArrayList<User> stats = new ArrayList<User>();

	protected int sleeptime = 5000;
	protected boolean userSuccess = false;

	Thread thread;
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
		String c_beamUrl = "http://10.0.1.27:4254/rpc/";
	
		if (activity != null) {
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
			c_beamUrl = sharedPref.getString(Settings.C_BEAM_URL, "http://10.0.1.27:4254/rpc/");
		}
		Log.i(TAG, "using c-beam url "+ c_beamUrl);
		c_beamClient = JSONRPCClient.create(c_beamUrl, JSONRPCParams.Versions.VERSION_2);
		c_beamClient.setConnectionTimeout(5000);
		c_beamClient.setSoTimeout(5000);
		portalClient = JSONRPCClient.create("https://c-portal.c-base.org/rpc/", JSONRPCParams.Versions.VERSION_2);
		portalClient.setConnectionTimeout(5000);
		portalClient.setSoTimeout(5000);
		
		// Create new JSON-RPC 2.0 client session
		//		try {
		//			portalSession = new JSONRPC2Session(new URL("https://c-portal.c-base.org/rpc/"));
		//			portalSession.getOptions().trustAllCerts(true);
		//		} catch (MalformedURLException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
	}

	public static C_beam getInstance() {
		return instance;
	}

	public static void setInstance(C_beam instance) {
		C_beam.instance = instance;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
		initC_beamClient();
	}

	public void startThread() {
		runnable = new Runnable() {
			@Override
			public void run() {
				boolean stop = false;
				while(!stop) {
					updateLists();
					try {
						Thread.sleep(sleeptime);
					} catch (InterruptedException e) {
						stop = true;
					}
				}
			}
		};
		thread = new Thread(runnable);
		thread.start();
	}
	
	public boolean isInCrewNetwork() {
		if (activity == null)
			return false;
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
		String username = sharedPref.getString(Settings.USERNAME, "bernd");
		
		if (sharedPref.getBoolean(Settings.DEBUG_ENABLED, false) || debug)
			return true;
		
		WifiManager wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
		String ip = Formatter.formatIpAddress(wifiManager.getDhcpInfo().ipAddress);
		if (wifiManager.isWifiEnabled() && ip.startsWith("42.42.") ) {
			return true;
		} else if (wifiManager.isWifiEnabled() && ip.startsWith("10.0.")) {
			return true;
		} else {
			return false;
		}
	}

	public void updateLists() {
		try {
			JSONObject result = c_beamClient.callJSONObject("app_data");
			updateUserLists(result.getJSONArray("user"));
			updateEvents(result.getJSONArray("events"));
			updateArtefacts(result.getJSONArray("artefacts"));
			updateMissions(result.getJSONArray("missions"));
			updateArticles(result.getJSONArray("articles"));
			updateActivitylog(result.getJSONArray("activitylog"));
			updateStats(result.getJSONArray("stats"));
		} catch (Exception e) {
			Log.i(TAG, "updateLists failed");
			e.printStackTrace();	
		}
//		updateArticles();


		//		String method = "list_articles";
		//		int requestID = 0;

		//		try {
		//			Log.i(TAG,portalClient.callJSONArray("list_articles").toString());
		//
		//		} catch (JSONRPCException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}

		//		JSONRPC2Request request = new JSONRPC2Request(method, requestID);
		//		JSONRPC2Response response = null;
		//
		//		try {
		//			response = portalSession.send(request);
		//			if (response.indicatesSuccess())
		//				System.out.println(response.getResult());
		//			else
		//				System.out.println(response.getError().getMessage());
		//		} catch (JSONRPC2SessionException e) {
		//			System.err.println(e.getMessage());
		//			e.printStackTrace();
		//		}
	}

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

	private synchronized ArrayList<User> updateUsers() {
		ArrayList<User> list = new ArrayList<User>();

		try {
			if (isInCrewNetwork()) {
				JSONArray result = c_beamClient.callJSONArray("user_list");
				for (int i=0; i<result.length(); i++) {
					JSONObject item = result.getJSONObject(i);
					list.add(new User(item));
				}
			}
			users = list;
			sleeptime = 6000;
		} catch (JSONRPCException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}

	public User getUser(int id) {
		for(User user: users) {
			if (user.getId() == id) {
				return user;
			}
		}
		// TODO this can cause a NetworkOnMainThreadException, can it be replaced by the code above?
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
	
//	public synchronized ArrayList<Event> updateEvents() {
//		Log.i(TAG, "updateEvents()");
//		try {
//			events = new ArrayList<Event>();
//			JSONArray result = c_beamClient.callJSONArray("event_list");
//			for (int i=0; i<result.length(); i++) {
//				JSONObject item = result.getJSONObject(i);
//				events.add(new Event(item));
//			}
//			return events;
//		} catch (JSONRPCException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return events;
//	}

	public synchronized ArrayList<Mission> getMissions() {
		return missions;
	}
	
//	public synchronized ArrayList<Mission> updateMissions() {
//		missions = new ArrayList<Mission>();
//
//		try {
//			if (isInCrewNetwork()) {
//				JSONArray result = c_beamClient.callJSONArray("mission_list");
//				for (int i=0; i<result.length(); i++) {
//					JSONObject item = result.getJSONObject(i);
//					missions.add(new Mission(item));
//				}
//			}
//		} catch (JSONRPCException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return missions;
//	}

	public synchronized ArrayList<ActivityLog> getActivityLog() {
		return activitylog;
	}

//	public synchronized ArrayList<ActivityLog> updateActivityLog() {
//		activitylog = new ArrayList<ActivityLog>();
//
//		try {
//			if (isInCrewNetwork()) {
//				JSONArray result = c_beamClient.callJSONArray("activitylog");
//				for (int i=0; i<result.length(); i++) {
//					JSONObject item = result.getJSONObject(i);
//					activitylog.add(new ActivityLog(item));
//				}
//			}
//		} catch (JSONRPCException e) {
//			e.printStackTrace();
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		return activitylog;
//	}

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

//	private synchronized ArrayList<User> updateStats() {
//		ArrayList<User> list = new ArrayList<User>();
//
//		try {
//			if (isInCrewNetwork()) {
//				JSONArray result = c_beamClient.callJSONArray("stats_list");
//				for (int i=0; i<result.length(); i++) {
//					JSONObject item = result.getJSONObject(i);
//					list.add(new User(item));
//				}
//			}
//			stats = list;
//		} catch (JSONRPCException e) {
//			e.printStackTrace();
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		return list;
//	}
	
	public ArrayList<User> getStats() {
		return stats;
	}

	public synchronized void register(String regId, String user) {
		try {
			if (isInCrewNetwork())
				c_beamClient.call("gcm_register", user, regId);
			Log.i(TAG, "register: "+user+":"+regId);
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized void register_update(String regId, String user) {
		try {
			if (isInCrewNetwork()){
				String result = c_beamClient.callString("gcm_update", user, regId);
				Log.i(TAG, "registerUpdate("+user+", " +regId+")");
			}
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
				JSONObject res = c_beamClient.callJSONObject("sounds");
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

//	public synchronized ArrayList<Article> updateArticles() {
//		articleList = new ArrayList<Article>();
//		try {
//			if (isInCrewNetwork()) {
//				JSONArray result = c_beamClient.callJSONObject("list_articles").getJSONArray("result");
//				for (int i=0; i<result.length(); i++) {
//					JSONObject item = result.getJSONObject(i);
//					//Log.i("articles", item.toString());
//					articleList.add(new Article(item));
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return articleList;
//	}

	public ArrayList<Article> getArticles() {
		return articleList;
	}

//	public synchronized ArrayList<Artefact> updateArtefacts() {
//		artefactList = new ArrayList<Artefact>();
//		try {
//			if (isInCrewNetwork()) {
//				JSONArray result = c_beamClient.callJSONArray("artefact_list");
//				for (int i=0; i<result.length(); i++) {
//					JSONObject item = result.getJSONObject(i);
//					artefactList.add(new Artefact(item));
//				}
//			}
//		} catch (JSONRPCException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return artefactList;
//	}

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

	public void hwstorage(boolean b) {
		try {
			if (isInCrewNetwork())
				Log.i("c-beam", "hw-storage");
			c_beamClient.callJSONObject("hwstorage", true);
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stopThread() {
		if (thread != null) {
			thread.interrupt();
		}
	}

	public void set_stripe_pattern(int pattern) {
		try {
			if (isInCrewNetwork())
				Log.i("c-beam", "set_stripe_pattern");
			c_beamClient.callJSONObject("set_stripe_pattern", pattern);
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void set_stripe_speed(int speed) {
		try {
			if (isInCrewNetwork())
				Log.i("c-beam", "set_stripe_speed");
			c_beamClient.callJSONObject("set_stripe_speed", speed);
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void set_stripe_offset(int offset) {
		try {
			if (isInCrewNetwork())
				Log.i("c-beam", "set_stripe_pattern");
			c_beamClient.callJSONObject("set_stripe_pattern", offset);
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void notbeleuchtung() {
		try {
			if (isInCrewNetwork())
				Log.i("c-beam", "notbeleuchtung");
			c_beamClient.callJSONObject("notbeleuchtung");
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void set_stripe_default() {
		try {
			if (isInCrewNetwork())
				Log.i("c-beam", "set_pattern_default");
			c_beamClient.callJSONObject("set_pattern_default");
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public boolean isStats_enabled() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
		String user = sharedPref.getString(Settings.USERNAME, "bernd");
		User u = null;
		try {
			if (isInCrewNetwork()) {
				JSONObject item = c_beamClient.callJSONObject("get_user_by_name", user);
				u = new User(item);
				return u.isStats_enabled();
			}
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public void setStatsEnabled(boolean stats_enabled) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
		String user = sharedPref.getString(Settings.USERNAME, "bernd");
		User u = null;
		try {
			if (isInCrewNetwork()) {
				String result = c_beamClient.callString("set_stats_enabled", user, stats_enabled);
			}
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void logactivity(String activity, String ap_string) {
		int ap = Integer.parseInt(ap_string);
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.activity);
		String user = sharedPref.getString(Settings.USERNAME, "bernd");
		User u = null;
		try {
			if (isInCrewNetwork()) {
				String result = c_beamClient.callString("logactivity", user, activity, ap);
			}
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public boolean isLoggedIn(String user) {
		User u = null;
		try {
			if (isInCrewNetwork()) {
				JSONObject item = c_beamClient.callJSONObject("get_user_by_name", user);
				u = new User(item);
				return u.getStatus().equals("online");
			}
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}


	public void setPushMissions(Boolean newValue) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
		String user = sharedPref.getString(Settings.USERNAME, "bernd");
		User u = null;
		try {
			if (isInCrewNetwork()) {
				String result = c_beamClient.callString("set_push_missions", user, newValue);
			}
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setPushBoarding(Boolean newValue) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
		String user = sharedPref.getString(Settings.USERNAME, "bernd");
		User u = null;
		try {
			if (isInCrewNetwork()) {
				String result = c_beamClient.callString("set_push_boarding", user, newValue);
			}
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void setPushETA(Boolean newValue) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
		String user = sharedPref.getString(Settings.USERNAME, "bernd");
		User u = null;
		try {
			if (isInCrewNetwork()) {
				String result = c_beamClient.callString("set_push_eta", user, newValue);
			}
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
