package org.droidparts.test.model;

import org.droidparts.annotation.json.Key;
import org.droidparts.model.Entity;

public class Phone extends Entity {
	private static final long serialVersionUID = 1L;

	@Key
	public String name;
	@Key
	public float version;

}
