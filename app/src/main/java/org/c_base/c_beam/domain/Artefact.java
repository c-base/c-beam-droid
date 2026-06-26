package org.c_base.c_beam.domain;

import androidx.annotation.NonNull;
import net.minidev.json.JSONObject;

public class Artefact {
	private String name;
	private String slug;

	public Artefact(JSONObject item) {
		super();
		this.name = (String) item.get("name");
		this.slug = (String) item.get("slug");
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

    @NonNull
	@Override
	public String toString() {
		return name;
	}
}
