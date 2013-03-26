package org.c_base.c_beam.domain;

import java.sql.Date;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Mission {
	private int id = 0;
	private String short_description = "";
	private String description = ""; 
	private String status = "unknown";
	private Date created_on = null;
	private int ap = 0;
	private ArrayList<String> assigned_to;
	//private User assigned_to = null;
	
	public Mission(JSONObject item) {
		try {
			
			id = item.getInt("id");
			short_description = item.getString("short_description");
			status = item.getString("status");
			description = item.getString("description");
			ap = item.getInt("ap");
			JSONArray tmp = item.getJSONArray("assigned_to");
			assigned_to = new ArrayList<String>();
			for (int i=0; i<tmp.length(); i++) {
				assigned_to.add(tmp.getString(i));
			}
			//created_on = Date.valueOf(item.getString("created_on"));
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

	public int getAp() {
		// TODO Auto-generated method stub
		return this.ap;
	}

	public Date getCreated_on() {
		return created_on;
	}

	public void setCreated_on(Date created_on) {
		this.created_on = created_on;
	}

	public ArrayList<String> getAssigned_to() {
		return assigned_to;
	}

	public void setAssigned_to(ArrayList<String> assigned_to) {
		this.assigned_to = assigned_to;
	}

	public void setAp(int ap) {
		this.ap = ap;
	}
	
}
