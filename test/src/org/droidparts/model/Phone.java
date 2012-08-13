package org.droidparts.model;

import org.droidparts.annotation.json.Key;

public class Phone extends Entity {
	private static final long serialVersionUID = 1L;

	@Key
	public String name;
	@Key
	public float version;

}
