package org.droidparts.test.model;

import static org.droidparts.serializer.json.JSONSerializer.SUB;

import org.droidparts.annotation.json.Key;
import org.droidparts.model.Model;

public class Nested extends Model {
	private static final long serialVersionUID = 1L;

	@Key(name = "sub_obj" + SUB + "str")
	public String str;

}
