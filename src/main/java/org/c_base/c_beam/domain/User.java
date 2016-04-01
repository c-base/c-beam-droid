package org.c_base.c_beam.domain;

//import net.minidev.json.JSONException;

import net.minidev.json.JSONObject;

public class User {
    private String username = "";
    private String status = "offline";
    private String eta = "";
    private long id = 0;
    private String etd;
    private String nickspell;
    private String etatimestamp;
    private String etdtimestamp;
    private String reminder;
    private String remindertimestamp;
    private String logintime;
    private long ap = 0;
    private long autologout;
    private double autologout_in;
    private boolean stats_enabled = false;
    private boolean push_missions = false;
    private boolean push_boarding = false;
    private boolean push_eta = false;

    public User(JSONObject item) {
        super();
        id = (long) item.get("id");
        username = (String) item.get("username");
        status = (String) item.get("status");
        eta = (String) item.get("eta");
        etatimestamp = (String) item.get("etatimestamp");
        etd = (String) item.get("nickspell");
        etdtimestamp = (String) item.get("etdtimestamp");
        nickspell = (String) item.get("nickspell");
        reminder = (String) item.get("reminder");
        remindertimestamp = (String) item.get("remindertimestamp");
        logintime = (String) item.get("logintime");
        ap = (long) item.get("ap");
        setAutologout((long) item.get("autologout"));
        try {
            setAutologout_in((double) item.get("autologout_in"));
        } catch (Exception e) {
            System.out.println("errrrrrr");
        }
        stats_enabled = (boolean) item.get("stats_enabled");
        push_missions = (boolean) item.get("push_missions");
        push_boarding = (boolean) item.get("push_boarding");
        push_eta = (boolean) item.get("push_eta");
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
