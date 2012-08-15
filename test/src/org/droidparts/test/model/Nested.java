package org.droidparts.test.model;

import static org.droidparts.serializer.json.JSONSerializer.__;

import org.droidparts.annotation.json.Key;
import org.droidparts.model.Model;

public class Nested extends Model {
	private static final long serialVersionUID = 1L;

	@Key(name = "sub_obj" + __ + "str")
	public String str;

}
