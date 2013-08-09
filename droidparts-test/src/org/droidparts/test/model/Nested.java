package org.droidparts.test.model;

import org.droidparts.annotation.json.Key;
import org.droidparts.model.Model;

public class Nested extends Model {
	private static final long serialVersionUID = 1L;

	@Key(name = "sub_obj" + Key.SUB + "str")
	public String str;

}
