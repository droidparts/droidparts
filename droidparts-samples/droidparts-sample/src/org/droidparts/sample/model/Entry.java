package org.droidparts.sample.model;

import java.util.Date;

import org.droidparts.annotation.json.Key;
import org.droidparts.annotation.sql.Column;
import org.droidparts.annotation.sql.Table;
import org.droidparts.model.Entity;

@Table(name = "entries")
public class Entry extends Entity {
	private static final long serialVersionUID = 1L;

	@Column
	@Key(name = "date_created")
	public Date created;

	@Column
	@Key
	public String name;

}
