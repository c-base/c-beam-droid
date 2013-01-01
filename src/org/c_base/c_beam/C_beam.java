package org.c_base.c_beam;

import java.util.ArrayList;

import org.alexd.jsonrpc.JSONRPCClient;
import org.alexd.jsonrpc.JSONRPCException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

public class C_beam {
	protected JSONRPCClient c_beamClient;
	protected JSONRPCClient portalClient;
	protected Runnable runnable;
	
	protected ArrayList<User> onlineList = new ArrayList<User>();
	protected ArrayList<User> offlineList = new ArrayList<User>();
	protected ArrayList<User> etaList = new ArrayList<User>();
	protected ArrayList<User> users = new ArrayList<User>();
	protected JSONArray events = new JSONArray();
	protected Activity parent;
	protected ArrayList<Artefact> artefactList = new ArrayList<Artefact>();
	protected ArrayList<Article> articleList = new ArrayList<Article>();
	
	public C_beam(Activity parent) {
		this.parent = parent;
		c_beamClient = JSONRPCClient.create("http://10.0.1.27:4254/rpc/");
    	c_beamClient.setConnectionTimeout(5000);
    	c_beamClient.setSoTimeout(5000);
    	portalClient = JSONRPCClient.create("https://c-portal.c-base.org/rpc/");
    	portalClient.setConnectionTimeout(5000);
    	portalClient.setSoTimeout(5000);
	}
	
	public void startThread() {
		runnable = new Runnable() {
			@Override
			public void run() {
				while(true) {
					updateLists();
					
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		new Thread(runnable).start();
	}
	public boolean isInCrewNetwork() {
		WifiManager wifiManager = (WifiManager) parent.getSystemService(Context.WIFI_SERVICE);

		if (Formatter.formatIpAddress(wifiManager.getDhcpInfo().ipAddress).startsWith("10.0.") && wifiManager.isWifiEnabled()) {
			return true;
		} else {
			// TODO: Display message 
			Log.i("c-beam", "not in crew network");
			return false;
		}

	}

	public void updateLists() {
		Log.i("c-beam", "updateLists()");
		users = updateUsers();
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
		
		updateEvents();
		updateArtefacts();
		updateArticles();
	}
	
	public synchronized JSONObject who() { 
		JSONObject result = null;
		try {
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
			JSONArray result = c_beamClient.callJSONArray("user_list");
			for (int i=0; i<result.length(); i++) {
				JSONObject item = result.getJSONObject(i);
				list.add(new User(item));				
			}			
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}
	
	public synchronized User getUser(int id) {
		User u = null;
		try {
			JSONObject item = c_beamClient.callJSONObject("get_user_by_id", id);
			u = new User(item);
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return u;
	}

	public JSONArray getEvents(){
		return events;
	}
	public synchronized JSONArray updateEvents() {
		Log.i("c-beam", "updateEvents()");
		try {
			events = c_beamClient.callJSONArray("events");
			return events;
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public synchronized ArrayList<Mission> getMissions() {
		ArrayList<Mission> list = new ArrayList<Mission>();
		
		try {
			JSONArray result = c_beamClient.callJSONArray("mission_list");
			for (int i=0; i<result.length(); i++) {
				JSONObject item = result.getJSONObject(i);
				list.add(new Mission(item));				
			}			
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}
	
	public synchronized Mission getMission(int id) {
		Mission m = null;
		try {
			JSONObject item = c_beamClient.callJSONObject("mission_detail", id);
			Log.i("item", item.toString());
			m = new Mission(item);
			Log.i("dn", m.toString());
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return m;
	}
	
	public synchronized void register(String regId, String user) {
		try {
			c_beamClient.call("gcm_register", user, regId);
			Log.i("c-beam", "register: "+user+":"+regId);
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	public synchronized void register_update(String regId, String user) {
		try {
			c_beamClient.call("gcm_update", user, regId);
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	public synchronized void login(String user) {
		try {
			c_beamClient.call("login", user);
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized void logout(String user) {
		try {
			c_beamClient.call("logout", user);
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized void force_login(String user) {
		try {
			c_beamClient.call("force_login", user);
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public synchronized void force_logout(String user) {
		try {
			c_beamClient.call("force_logout", user);
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
			Log.i("c-beam","tts("+text+")");
			c_beamClient.call("tts", "julia", text);
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public synchronized void r2d2(String text) {
		try {
			Log.i("c-beam","r2d2("+text+")");
			c_beamClient.call("tts", "r2d2", text);
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public synchronized ArrayList<String> getSounds() {
		ArrayList<String> result = new ArrayList<String>();
		try {
			JSONObject res = c_beamClient.callJSONObject("sounds");
			JSONArray items = res.getJSONArray("result");
			for(int i=0; i<items.length();i++)
				result.add(items.getString(i));
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
			Log.i("c-beam","play("+sound+")");
			c_beamClient.call("play", sound);
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized void announce(String text) {
		try {
			Log.i("c-beam","announce("+text+")");
			c_beamClient.call("announce", text);
		} catch (JSONRPCException e) {
			e.printStackTrace();
		}
		
	}

	public synchronized ArrayList<Article> updateArticles() {
		articleList = new ArrayList<Article>();
		try {
			JSONArray result = c_beamClient.callJSONObject("list_articles").getJSONArray("result");
			for (int i=0; i<result.length(); i++) {
				JSONObject item = result.getJSONObject(i);			
				Log.i("articles", item.toString());
				articleList.add(new Article(item));				
			}			
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return articleList;
	}
	
	public ArrayList<Article> getArticles() {
		return articleList;
	}
	
	public synchronized ArrayList<Artefact> updateArtefacts() {
		artefactList = new ArrayList<Artefact>();
		try {
			JSONArray result = c_beamClient.callJSONArray("artefact_list");
			for (int i=0; i<result.length(); i++) {
				JSONObject item = result.getJSONObject(i);
				artefactList.add(new Artefact(item));				
			}			
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return artefactList;
	}

	public ArrayList<Artefact> getArtefacts() {
		return artefactList;
	}

	public void bluewall() {
		try {
			JSONObject result = c_beamClient.callJSONObject("bluewall");
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void darkwall() {
		try {
			JSONObject result = c_beamClient.callJSONObject("darkwall");
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
