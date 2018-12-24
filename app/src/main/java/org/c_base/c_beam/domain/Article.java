package org.c_base.c_beam.domain;

import net.minidev.json.JSONObject;

public class Article {
	private int id;
	private String title;
	private String articleAbstract;
	private String body;

	public Article(JSONObject item) {
		super();
		//id = item.getString("id");
		JSONObject fields = (JSONObject) item.get("fields");
		this.id = (int) item.get("pk");

		this.title = (String) fields.get("title");
		this.articleAbstract = (String) fields.get("abstract");
		this.body = (String) fields.get("body");
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
