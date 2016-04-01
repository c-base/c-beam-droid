package org.c_base.c_beam.domain;

import net.minidev.json.JSONObject;

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
		//			id = item.getInt("id");
		title = (String) item.get("title");
		description = (String) item.get("description");
		start = (String) item.get("start");
		end = (String) item.get("end");
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
