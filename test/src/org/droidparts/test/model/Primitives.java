package org.droidparts.test.model;

import org.droidparts.annotation.json.Key;
import org.droidparts.model.Model;

public class Primitives extends Model {
	private static final long serialVersionUID = 1L;

	@Key
	public int int1;
	@Key
	public int int2;
	@Key
	public float float1;
	@Key
	public float float2;
	@Key
	public boolean boolean1;
	@Key
	public boolean boolean2;
	@Key
	public boolean boolean3;
	@Key
	public boolean boolean4;
	@Key
	public String string1;

	@Key(name = "string_array")
	public String[] arr;

}
