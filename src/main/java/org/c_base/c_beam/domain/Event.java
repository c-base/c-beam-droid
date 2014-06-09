package org.c_base.c_beam.domain;

import org.json.JSONException;
import org.json.JSONObject;

public class Event {
	protected int id = 0;
	protected String title = "dummy event";
	protected String description = "";
	protected String start = "";
	protected String end = "";

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Event(String title) {
		this.title = title;
	}

	public Event(JSONObject item) {
		super();
		try {
//			id = item.getInt("id");	
			title = item.getString("title");
			description = item.getString("description");
			start = item.getString("start");
			end = item.getString("end");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		if (!start.isEmpty() || !end.isEmpty()) {
			return title + " ("+start+"-"+end+")";
		} else {
			return title;
		}
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = end;
	}
	
	
	
}
