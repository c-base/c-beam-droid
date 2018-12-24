package org.c_base.c_beam.domain;

import net.minidev.json.JSONObject;

public class Artefact {
	int id;
	String name;
	String slug;
	
	public Artefact(String string) {
		// TODO Auto-generated constructor stub
	}
	
	public Artefact(JSONObject item) {
		super();
		//id = item.getInt("id");
		name = (String) item.get("name");
		slug = (String) item.get("slug");
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	@Override
	public String toString() {
		return name;
	}

}
