package org.droidparts.test.model;

import org.droidparts.annotation.json.Key;
import org.droidparts.annotation.sql.Column;
import org.droidparts.annotation.sql.Table;
import org.droidparts.model.Entity;
import org.droidparts.test.persist.DB;

@Table(name = DB.Table.ALBUM)
public class Album extends Entity {
	private static final long serialVersionUID = 1L;

	@Key
	@Column(name = DB.Column.YEAR)
	public int year;

	@Key
	@Column(name = DB.Column.NAME, unique = true)
	public String name;

	@Column(name = DB.Column.COMMENT, nullable = true)
	public String comment;

	public Album() {
	}

	public Album(String name, int year) {
		this.name = name;
		this.year = year;
	}

}
