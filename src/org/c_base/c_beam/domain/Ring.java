package org.c_base.c_beam.domain;

import android.graphics.drawable.Drawable;
import android.widget.ImageButton;

/**
 * Created by smile on 7/6/13.
 */
public class Ring {
    private String name = "";
    private Drawable image = null;

    public Ring(String name, Drawable image) {
        this.name = name;
        this.image = image;
    }

    public Ring() {

    }

    public String getName() {
        return name;
    }

    public Drawable getImage() {
        return image;
    }

}
