package org.c_base.c_beam.domain;

import android.util.Log;
import net.minidev.json.JSONObject;

public class ActivityLog {
	private User user;
	private String activity;
	private long ap;
	private String timestamp;
	private String str;

	public ActivityLog(JSONObject item) {
		super();
		try {
            if (item.containsKey("user")) {
                user = new User((JSONObject) item.get("user"));
            }
			activity = (String) item.get("activity");
            if (item.containsKey("ap")) {
                ap = (Long) item.get("ap");
            }
			timestamp = (String) item.get("timestamp");
			str = (String) item.get("str");
		} catch (Exception e) {
			Log.e("ActivityLog", "Error parsing ActivityLog from JSON", e);
		}
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
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
