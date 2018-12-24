package org.c_base.c_beam.domain;

import net.minidev.json.JSONObject;

public class ActivityLog {
	int id;
	User user;
	Mission mission;
	String activity;
	long ap;
	String timestamp;
	String str;
	
	public ActivityLog(JSONObject item) {
		super();
		//id = item.getInt("id");
		user = new User((JSONObject) item.get("user"));
		//mission = new Mission(item.getJSONObject("mission"));
		activity = (String) item.get("activity");
		ap = (long) item.get("ap");
		timestamp = (String) item.get("timestamp");
		str = (String) item.get("str");
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

	public long getAp() {
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
