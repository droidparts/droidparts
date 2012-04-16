package org.droidparts.sample.db;

import org.droidparts.annotation.sql.Column;
import org.droidparts.annotation.sql.Table;
import org.droidparts.model.Entity;

@Table("entries")
public class Entry extends Entity {
	private static final long serialVersionUID = 1L;

	@Column
	public long created;
	@Column
	public String name;

}
