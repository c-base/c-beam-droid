package org.c_base.c_beam.domain;

import androidx.annotation.NonNull;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class Mission {
	private long id;
	private String short_description;
	private String description;
	private String status;
	private Date created_on;
	private long ap;
	private ArrayList<String> assigned_to;

	public Mission(JSONObject item) {
		super();
		id = (Long) item.get("id");
		short_description = (String) item.get("short_description");
		description = (String) item.get("description");
		status = (String) item.get("status");
		ap = (Long) item.get("ap");
		JSONArray tmp = (JSONArray) item.get("assigned_to");
		assigned_to = new ArrayList<>();
        if (tmp != null) {
            for (Object o : tmp) {
                assigned_to.add((String) o);
            }
        }
	}

    @NonNull
	@Override
	public String toString() {
		return short_description;
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

	public Date getCreated_on() {
		return created_on;
	}

	public void setCreated_on(Date created_on) {
		this.created_on = created_on;
	}

	public long getAp() {
		return ap;
	}

	public void setAp(int ap) {
		this.ap = ap;
	}

	public ArrayList<String> getAssigned_to() {
		return assigned_to;
	}

	public void setAssigned_to(ArrayList<String> assigned_to) {
		this.assigned_to = assigned_to;
	}
}
