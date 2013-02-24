package org.c_base.c_beam.domain;

import org.json.JSONException;
import org.json.JSONObject;

public class Artefact {
	int id;
	String name;
	String slug;
	
	public Artefact(String string) {
		// TODO Auto-generated constructor stub
	}
	
	public Artefact(JSONObject item) {
		super();
		try {
			//id = item.getInt("id");	
			name = item.getString("name");
			slug = item.getString("slug");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
