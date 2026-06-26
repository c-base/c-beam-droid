package org.c_base.c_beam.domain;

import android.graphics.drawable.Drawable;

public class Ring {
	private String name;
	private Drawable image;

	public Ring(String name, Drawable image) {
		super();
		this.name = name;
		this.image = image;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Drawable getImage() {
		return image;
	}

	public void setImage(Drawable image) {
		this.image = image;
	}

}
