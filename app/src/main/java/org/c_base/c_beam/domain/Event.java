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
		title = text(item, "title");
		description = text(item, "description");
		start = text(item, "start");
		end = text(item, "end");
	}

	/** The server sends null for an event without description; the fields stay "". */
	private static String text(JSONObject item, String key) {
		Object value = item.get(key);
		return value == null ? "" : value.toString();
	}

	/** True for an all-day entry, which the server marks with "0000" for both times. */
	public boolean isAllDay() {
		return "0000".equals(start) && "0000".equals(end);
	}

	/** "1900" as "19:00"; anything else as it is. */
	public static String clock(String hhmm) {
		if (hhmm != null && hhmm.length() == 4) {
			return hhmm.substring(0, 2) + ":" + hhmm.substring(2);
		}
		return hhmm;
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
