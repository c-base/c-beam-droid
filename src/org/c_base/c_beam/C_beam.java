package org.c_base.c_beam;

import java.util.ArrayList;

import org.alexd.jsonrpc.JSONRPCClient;
import org.alexd.jsonrpc.JSONRPCException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.util.Log;

public class C_beam {
	protected JSONRPCClient client;
	protected Runnable runnable;
	
	protected ArrayList<User> onlineList = new ArrayList<User>();
	protected ArrayList<User> offlineList = new ArrayList<User>();
	protected ArrayList<User> etaList = new ArrayList<User>();
	protected ArrayList<User> users = new ArrayList<User>();
	protected JSONArray events = new JSONArray();
	
	public C_beam() {
		client = JSONRPCClient.create("http://10.0.1.27:4254/rpc/");
    	client.setConnectionTimeout(5000);
    	client.setSoTimeout(5000);
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
	}
	
	public synchronized JSONObject who() { 
		JSONObject result = null;
		try {
			result = client.callJSONObject("who");
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
			JSONArray result = client.callJSONArray("user_list");
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
			JSONObject item = client.callJSONObject("get_user_by_id", id);
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
			events = client.callJSONArray("events");
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
			JSONArray result = client.callJSONArray("mission_list");
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
			JSONObject item = client.callJSONObject("mission_detail", id);
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
			client.call("gcm_register", user, regId);
			Log.i("c-beam", "register: "+user+":"+regId);
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	public synchronized void register_update(String regId, String user) {
		try {
			client.call("gcm_update", user, regId);
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	public synchronized void login(String user) {
		try {
			client.call("login", user);
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized void logout(String user) {
		try {
			client.call("logout", user);
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized void force_login(String user) {
		try {
			client.call("force_login", user);
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public synchronized void force_logout(String user) {
		try {
			client.call("force_logout", user);
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
			client.call("tts", "julia", text);
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public synchronized void r2d2(String text) {
		try {
			Log.i("c-beam","r2d2("+text+")");
			client.call("tts", "r2d2", text);
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public ArrayList<String> getSounds() {
		ArrayList<String> result = new ArrayList<String>();
		try {
			JSONObject res = client.callJSONObject("sounds");
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

	public void play(String sound) {
		try {
			Log.i("c-beam","play("+sound+")");
			client.call("play", sound);
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void announce(String text) {
		try {
			Log.i("c-beam","announce("+text+")");
			client.call("announce", text);
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
}
