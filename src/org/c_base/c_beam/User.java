package org.c_base.c_beam;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
	private String username = "";
	private String status = "offline";
	private String eta = "";
	private int id = 0;
	private String etd;
	private String nickspell;
	private String etatimestamp;
	private String etdtimestamp;
	private String reminder;
	private String remindertimestamp;
	private String logintime;
	
	public User(JSONObject item) {
		try {
			id = item.getInt("id");	
			username = item.getString("username");
			status = item.getString("status");
			eta = item.getString("eta");
			etatimestamp = item.getString("etatimestamp");
			etd = item.getString("nickspell");
			etdtimestamp = item.getString("etdtimestamp");
			nickspell = item.getString("nickspell");
			reminder = item.getString("reminder");
			remindertimestamp = item.getString("remindertimestamp");
			logintime = item.getString("logintime");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public String getUsername() {
		return username;
	}
	@Override
	public String toString() {
		//return "User [username=" + username + ", status=" + status + ", id="
		//		+ id + "]";
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getEta() {
		return eta;
	}
	public void setEta(String eta) {
		this.eta = eta;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getEtd() {
		return etd;
	}
	public void setEtd(String etd) {
		this.etd = etd;
	}
	public String getNickspell() {
		return nickspell;
	}
	public void setNickspell(String nickspell) {
		this.nickspell = nickspell;
	}
	public String getEtatimestamp() {
		return etatimestamp;
	}
	public void setEtatimestamp(String etatimestamp) {
		this.etatimestamp = etatimestamp;
	}
	public String getEtdtimestamp() {
		return etdtimestamp;
	}
	public void setEtdtimestamp(String etdtimestamp) {
		this.etdtimestamp = etdtimestamp;
	}
	public String getReminder() {
		return reminder;
	}
	public void setReminder(String reminder) {
		this.reminder = reminder;
	}
	public String getRemindertimestamp() {
		return remindertimestamp;
	}
	public void setRemindertimestamp(String remindertimestamp) {
		this.remindertimestamp = remindertimestamp;
	}
	public String getLogintime() {
		return logintime;
	}
	public void setLogintime(String logintime) {
		this.logintime = logintime;
	}
	
}
