package org.c_base.c_beam.domain;

import androidx.annotation.NonNull;
import net.minidev.json.JSONObject;

import java.util.Objects;

public class User {
	private String username;
	private String status = "offline";
	private String eta = "";
	private long id;
	private String etd;
	private String nickspell;
	private String etatimestamp;
	private String etdtimestamp;
	private String reminder;
	private String remindertimestamp;
	private String logintime;
	private long ap;
	private long autologout;
	private double autologout_in;
	private boolean stats_enabled;
	private boolean push_missions;
	private boolean push_boarding;
	private boolean push_eta;

	public User(JSONObject item) {
		super();
		this.username = (String) item.get("username");
		this.status = (String) item.get("status");
		this.eta = (String) item.get("eta");
		this.id = (Long) item.get("id");
		this.etd = (String) item.get("etd");
		this.nickspell = (String) item.get("nickspell");
		this.etatimestamp = (String) item.get("etatimestamp");
		this.etdtimestamp = (String) item.get("etdtimestamp");
		this.reminder = (String) item.get("reminder");
		this.remindertimestamp = (String) item.get("remindertimestamp");
		this.logintime = (String) item.get("logintime");
		this.ap = (Long) item.get("ap");
		setAutologout((Long) item.get("autologout"));
        if (item.containsKey("autologout_in")) {
            setAutologout_in((Double) item.get("autologout_in"));
        }
		this.stats_enabled = (Boolean) item.get("stats_enabled");
		this.push_missions = (Boolean) item.get("push_missions");
		this.push_boarding = (Boolean) item.get("push_boarding");
		this.push_eta = (Boolean) item.get("push_eta");
	}

	public User(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

    @NonNull
	@Override
	public String toString() {
        if (Objects.equals(status, "eta")) {
            return username + " (" + eta + ")";
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

	public long getId() {
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

	public long getAutologout() {
		return autologout;
	}

	public void setAutologout(long autologout) {
		this.autologout = autologout;
	}

	public double getAutologout_in() {
		return autologout_in;
	}

	public void setAutologout_in(double autologout_in) {
		this.autologout_in = autologout_in;
	}

	public void setAp(int ap) {
		this.ap = ap;
	}

	public long getAp() {
		return ap;
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
