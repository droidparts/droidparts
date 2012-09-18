package org.droidparts.test.model;

import java.util.ArrayList;
import java.util.HashSet;

import org.droidparts.annotation.json.Key;
import org.droidparts.annotation.sql.Column;
import org.droidparts.model.Entity;

public class Primitives extends Entity {
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
	@Column(nullable = true)
	public String[] strArr;
	@Column(nullable = true)
	public int[] intArr;
	@Column(nullable = true)
	public ArrayList<String> strList = new ArrayList<String>();
	@Column(nullable = true)
	public HashSet<Double> doubleSet = new HashSet<Double>();

	@Column(nullable = true)
	public En en;
	@Column(nullable = true)
	public En[] enArr;

	public static enum En {
		HI, THERE,
	}

}
