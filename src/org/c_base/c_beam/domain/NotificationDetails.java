package org.c_base.c_beam.domain;

import java.util.Date;

public class NotificationDetails {
	private String title = "";
	private String text = "";
	private Date timestamp = new Date();
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
}
