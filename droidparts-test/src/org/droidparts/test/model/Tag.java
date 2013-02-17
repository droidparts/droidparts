package org.droidparts.test.model;

import org.droidparts.annotation.json.Key;
import org.droidparts.annotation.sql.Column;
import org.droidparts.annotation.sql.Table;
import org.droidparts.model.Entity;
import org.droidparts.test.persist.DB;

@Table(name = DB.Table.TAGS)
public class Tag extends Entity {
	private static final long serialVersionUID = 1L;

	@Key
	@Column(unique = true)
	public String name;

	public Tag() {
	}

	public Tag(String name) {
		this.name = name;
	}

}
