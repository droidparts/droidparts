package org.droidparts.test.model;

import org.droidparts.annotation.sql.Column;
import org.droidparts.model.Entity;

public class TwoStrings extends Entity {
	private static final long serialVersionUID = 1L;

	@Column(unique = true)
	public String one;

	@Column(nullable = true)
	public String two;

}
