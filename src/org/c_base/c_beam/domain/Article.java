package org.c_base.c_beam.domain;

import org.json.JSONException;
import org.json.JSONObject;

public class Article {
	private int id;
	private String title;
	private String articleAbstract;
	private String body;

	public Article(JSONObject item) {
		super();
		try {
			//id = item.getString("id");	
			JSONObject fields = item.getJSONObject("fields");
			this.id = item.getInt("pk");
			
			this.title = fields.getString("title");
			this.articleAbstract = fields.getString("abstract");
			this.body = fields.getString("body");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Article(String title) {
		this.title = title;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}
	@Override
	public String toString() {
		return title + ":\n" + articleAbstract;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getArticleAbstract() {
		return articleAbstract;
	}
	public void setArticleAbstract(String articleAbstract) {
		this.articleAbstract = articleAbstract;
	}

}
