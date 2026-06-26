package org.c_base.c_beam.domain;

import androidx.annotation.NonNull;
import net.minidev.json.JSONObject;

public class Article {
	private int id;
	private String title;
	private String body;
	private String articleAbstract;

	public Article(JSONObject item) {
		super();
		this.id = (Integer) item.get("pk");
		JSONObject fields = (JSONObject) item.get("fields");
        if (fields != null) {
            this.title = (String) fields.get("title");
        }
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

	public void setTitle(String title) {
		this.title = title;
	}

    @NonNull
	@Override
	public String toString() {
		return title;
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
