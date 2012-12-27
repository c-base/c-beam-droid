package org.c_base.c_beam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.alexd.jsonrpc.JSONRPCClient;
import org.alexd.jsonrpc.JSONRPCException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class C_beam {
	JSONRPCClient client;
	public C_beam() {
		client = JSONRPCClient.create("http://10.0.1.27:4254/rpc/");
    	client.setConnectionTimeout(5000);
    	client.setSoTimeout(5000);
	}
	
	public JSONObject who() { 
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
	
	public User getUser(int id) {
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

	public JSONArray getEvents() {
		try {
			return client.callJSONArray("events");
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public ArrayList<Mission> getMissions() {
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
	
	public Mission getMission(int id) {
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
	
	public void register(String regId, String user) {
		try {
			client.call("gcm_register", user, regId);
			Log.i("c-beam", "register: "+user+":"+regId);
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	public void register_update(String regId, String user) {
		try {
			client.call("gcm_update", user, regId);
		} catch (JSONRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
}
