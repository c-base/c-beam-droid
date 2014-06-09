package org.c_base.c_beam.domain;

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
	private int ap = 0;
	private int autologout;
	private int autologout_in;
	private boolean stats_enabled = false;
	private boolean push_missions = false;
	private boolean push_boarding = false;
	private boolean push_eta = false;

	public User(JSONObject item) {
		super();
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
			ap = item.getInt("ap");
			setAutologout(item.getInt("autologout"));
			setAutologout_in(item.getInt("autologout_in"));
			stats_enabled = item.getBoolean("stats_enabled");
			push_missions = item.getBoolean("push_missions");
			push_boarding = item.getBoolean("push_boarding");
			push_eta = item.getBoolean("push_eta");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public User(String username) {
		this.username = username;
		// TODO Auto-generated constructor stub
	}
	public String getUsername() {
		return username;
	}
	@Override
	public String toString() {
		//return "User [username=" + username + ", status=" + status + ", id="
		//		+ id + "]";
		if (status.equals("eta")) {
			return "ETA: " + username + " (" + eta + ")";
		} else {
			return username;
		}
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
	public int getAutologout() {
		return autologout;
	}
	public void setAutologout(int autologout) {
		this.autologout = autologout;
	}
	public int getAutologout_in() {
		return autologout_in;
	}
	public void setAutologout_in(int autologout_in) {
		this.autologout_in = autologout_in;
	}
	public void setAp(int ap) {
		this.ap = ap;
	}
	public int getAp() {
		return this.ap;
	}

	public boolean isStats_enabled() {
		return stats_enabled;
	}
	public void setStats_enabled(boolean stats_enabled) {
		this.stats_enabled = stats_enabled;
	}
	public boolean isPush_missions() {
		return push_missions;
	}
	public void setPush_missions(boolean push_missions) {
		this.push_missions = push_missions;
	}
	public boolean isPush_boarding() {
		return push_boarding;
	}
	public void setPush_boarding(boolean push_boarding) {
		this.push_boarding = push_boarding;
	}
	public boolean isPush_eta() {
		return push_eta;
	}
	public void setPush_eta(boolean push_eta) {
		this.push_eta = push_eta;
	}

}
