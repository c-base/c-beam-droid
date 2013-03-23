package org.c_base.c_beam.domain;

import org.json.JSONException;
import org.json.JSONObject;

public class ActivityLog {
	int id;
	User user;
	Mission mission;
	String activity;
	int ap;
	String timestamp;
	String str;
	
	public ActivityLog(JSONObject item) {
		super();
		try {
			//id = item.getInt("id");	
			user = new User(item.getJSONObject("user"));
			//mission = new Mission(item.getJSONObject("mission"));
			activity = item.getString("activity");
			ap = item.getInt("ap");
			timestamp = item.getString("timestamp");
			str = item.getString("str");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Mission getMission() {
		return mission;
	}
	public void setMission(Mission mission) {
		this.mission = mission;
	}
	public String getActivity() {
		return activity;
	}
	public void setActivity(String activity) {
		this.activity = activity;
	}
	public int getAp() {
		return ap;
	}
	public void setAp(int ap) {
		this.ap = ap;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getStr() {
		return str;
	}

	public void setStr(String str) {
		this.str = str;
	}
	

}
