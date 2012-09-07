package org.droidparts.gram.model;

import org.droidparts.annotation.sql.Column;
import org.droidparts.annotation.sql.Table;
import org.droidparts.gram.contract.DB;
import org.droidparts.model.Entity;

@Table(DB.Table.FILTERS)
public class Filter extends Entity {
	private static final long serialVersionUID = 1L;

	@Column(name = DB.Column.NAME, unique = true)
	public String name;

	public Filter() {
	}

	public Filter(String name) {
		this.name = name;
	}

}
