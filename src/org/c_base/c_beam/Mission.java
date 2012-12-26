package org.c_base.c_beam;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Mission {
	private int id = 0;
	private String short_description = "";
	private String description = ""; 
	private String status = "unknown";
	
	public Mission(JSONObject item) {
		try {
			
			id = item.getInt("id");
			short_description = item.getString("short_description");
			status = item.getString("status");
			description = item.getString("description");
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		return short_description + " ("	+ status + ")";
	}

	public String getShort_description() {
		return short_description;
	}
	public void setShort_description(String short_description) {
		this.short_description = short_description;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
}
