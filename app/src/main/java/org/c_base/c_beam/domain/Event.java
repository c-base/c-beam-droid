package org.c_base.c_beam.domain;

import androidx.annotation.NonNull;
import net.minidev.json.JSONObject;

public class Event {
	protected String title;
	protected String description;
	protected String start;
	protected String end;
	protected int id;

	public Event(JSONObject item) {
		super();
		this.title = (String) item.get("title");
		this.description = (String) item.get("description");
		this.start = (String) item.get("start");
		this.end = (String) item.get("end");
	}

	public Event(String title) {
		this.title = title;
	}

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

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

    @NonNull
	@Override
	public String toString() {
		return title;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = end;
	}
}
