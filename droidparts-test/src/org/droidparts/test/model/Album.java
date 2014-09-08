/**
 * Copyright 2014 Alex Yanchenko
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

import org.droidparts.annotation.serialize.JSON;
import org.droidparts.annotation.serialize.XML;
import org.droidparts.annotation.sql.Column;
import org.droidparts.annotation.sql.Table;
import org.droidparts.model.Entity;
import org.droidparts.test.persist.DB;

@Table(name = DB.Table.ALBUMS)
public class Album extends Entity {
	private static final long serialVersionUID = 1L;

	@JSON
	@XML
	@Column(name = DB.Column.YEAR)
	public int year;

	@JSON
	@XML
	@Column(name = DB.Column.NAME, unique = true)
	public String name;

	@Column(name = DB.Column.COMMENT, nullable = true)
	public String comment;

	@Column(nullable = true)
	public Nested nullNested;

	@Column
	public Nested nested = new Nested();

	public Album() {
	}

	public Album(String name, int year) {
		this.name = name;
		this.year = year;
	}

}
