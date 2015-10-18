/**
 * Copyright 2015 Alex Yanchenko
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.droidparts.test.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import org.droidparts.annotation.serialize.JSON;
import org.droidparts.annotation.sql.Column;
import org.droidparts.model.Entity;

public class Primitives extends Entity {
	private static final long serialVersionUID = 1L;

	@JSON
	public int int1;
	@JSON
	public int int2;
	@JSON
	public float float1;
	@JSON
	public float float2;
	@JSON
	public boolean boolean1;
	@JSON
	public boolean boolean2;
	@JSON
	public boolean boolean3;
	@JSON
	public boolean boolean4;
	@JSON
	public String string1;

	@Column(nullable = true)
	public En en;
	@Column(nullable = true)
	public En[] enArr;

	@Column(nullable = true)
	public Date date;
	@Column
	public final ArrayList<Date> dates = new ArrayList<Date>();

	@JSON(key = "string_array")
	@Column(nullable = true)
	public String[] strArr;
	@JSON(key = "long_array")
	public ArrayList<Long> longList;

	@Column(nullable = true)
	public int[] intArr;
	@Column
	public ArrayList<String> strList = new ArrayList<String>();
	@Column
	public HashSet<Double> doubleSet = new HashSet<Double>();

	public static enum En {

		HI(14), THERE(1024);

		public final int id;

		private En(int id) {
			this.id = id;
		}

	}

}
