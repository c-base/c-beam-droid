package org.c_base.c_beam.domain;

public class Event {
	protected int id = 0;
	protected String title = "dummy event";

	public Event(String title) {
		this.title = title;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return title;
	}
	
	
	
	
}
