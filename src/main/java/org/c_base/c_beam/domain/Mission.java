package org.c_base.c_beam.domain;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.sql.Date;
import java.util.ArrayList;

public class Mission {
	private long id = 0;
	private String short_description = "";
	private String description = ""; 
	private String status = "unknown";
	private Date created_on = null;
	private long ap = 0;
	private ArrayList<String> assigned_to;
	//private User assigned_to = null;
	
	public Mission(JSONObject item) {

		id = (long) item.get("id");
		short_description = (String) item.get("short_description");
		status = (String) item.get("status");
		description = (String) item.get("description");
		ap = (long) item.get("ap");
		JSONArray tmp = (JSONArray) item.get("assigned_to");
		assigned_to = new ArrayList<String>();
		for (int i = 0; i < tmp.size(); i++) {
			assigned_to.add((String) tmp.get(i));
		}
		//created_on = Date.valueOf(item.getString("created_on"));
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

	public long getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getAp() {
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

	public void setAp(long ap) {
		this.ap = ap;
	}
	
}
