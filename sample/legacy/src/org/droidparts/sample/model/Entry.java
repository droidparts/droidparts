package org.droidparts.sample.model;

import org.droidparts.annotation.json.Key;
import org.droidparts.annotation.json.Object;
import org.droidparts.annotation.sql.Column;
import org.droidparts.annotation.sql.Table;
import org.droidparts.model.Entity;

@Table(name = "entries")
@Object(name = "entries")
public class Entry extends Entity {
	private static final long serialVersionUID = 1L;

	@Column
	@Key(name = "date_created")
	public long created;

	@Column
	@Key
	public String name;

}
