package org.droidparts.test.model;

import org.droidparts.annotation.json.Key;
import org.droidparts.annotation.sql.Column;
import org.droidparts.model.Entity;

public class Album extends Entity {
	private static final long serialVersionUID = 1L;

	@Key
	@Column
	public int year;

	@Key
	@Column(unique = true)
	public String name;

	@Column(nullable = true)
	public String comment;

	public Album() {
	}

	public Album(String name, int year) {
		this.name = name;
		this.year = year;
	}

}
