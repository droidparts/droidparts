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
package org.droidparts.contract;

public interface SQL {

	String EQUAL = " = ?";
	String NOT_EQUAL = " != ?";
	String LESS = " < ?";
	String LESS_OR_EQUAL = " <= ?";
	String GREATER = " > ?";
	String GREATER_OR_EQUAL = " >= ?";

	String NULL = " ISNULL";
	String NOT_NULL = " NOTNULL";

	String BETWEEN = " BETWEEN ? AND ?";
	String NOT_BETWEEN = " NOT" + BETWEEN;

	String IN = " IN ";
	String NOT_IN = " NOT" + IN;

	String LIKE = " LIKE ?";
	String NOT_LIKE = " NOT" + LIKE;

	String AND = " AND ";
	String OR = " OR ";
	String ASC = " ASC";
	String DESC = " DESC";

	public interface DDL {
		String CREATE_TABLE = "CREATE TABLE ";
		String ALTER_TABLE = "ALTER TABLE ";

		String ADD_COLUMN = " ADD COLUMN ";

		String CREATE_INDEX = "CREATE INDEX ";
		String CREATE_UNIQUE_INDEX = "CREATE UNIQUE INDEX ";
		String ON = " ON ";

		String OPENING_BRACE = " (";
		String SEPARATOR = ", ";
		String CLOSING_BRACE = ");";

		String INTEGER = " INTEGER";
		String REAL = " REAL";
		String TEXT = " TEXT";
		String BLOB = " BLOB";

		String PK = DB.Column.ID + " INTEGER PRIMARY KEY";
		String NOT_NULL = " NOT NULL";
		String DEFAULT = " DEFAULT ";
		String UNIQUE = " UNIQUE";
	}

}
