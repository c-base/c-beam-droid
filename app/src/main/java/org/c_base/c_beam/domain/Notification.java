package org.c_base.c_beam.domain;

import androidx.annotation.NonNull;

public class Notification {
	private String notification;
	private long id;

	public Notification(String notification) {
		super();
		this.notification = notification;
	}

	public Notification() {
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

    @NonNull
	@Override
	public String toString() {
		return notification;
	}
}
