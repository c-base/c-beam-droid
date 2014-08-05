package org.c_base.c_beam.domain;

public class Notification {

	public static final String NO_MESSAGES = "keine nachrichten";
	
	private String notification = "";
	private long id = 0;

	public Notification(String notification) {
		super();
		this.notification = notification;
	}
	public Notification() {
		super();
	}
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	public String getNotification() {
		return notification;
	}
	public void setNotification(String notification) {
		this.notification = notification;
	}
	
	@Override
	public String toString() {
		return notification;
	}
}
