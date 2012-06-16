/**
 * Copyright 2012 Alex Yanchenko
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

import android.provider.BaseColumns;

public interface DB {

	public interface Table {

	}

	public interface Column extends BaseColumns {

		String ID = _ID;

	}

	// sql chunks
	String EQUALS = " = ?";
	String NOT_EQUAL = " != ?";
	String GREATER = " > ?";
	String LESS = " < ?";
	// http://code.google.com/p/android/issues/detail?id=3153
	String LIKE_START = " LIKE '%s%%'";
	String LIKE_MIDDLE = " LIKE '%%%s%%'";
	String AND = " AND ";
	String OR = " OR ";
	String ASC = " ASC";
	String DESC = " DESC";
	String IS_NULL = " IS NULL";
	String IS_NOT_NULL = " IS NOT NULL";

	public interface DDL {
		String CREATE_TABLE = "CREATE TABLE ";

		String CREATE_INDEX = "CREATE INDEX ";
		String CREATE_UNIQUE_INDEX = "CREATE UNIQUE INDEX ";
		String ON = " ON ";

		String OPENING_BRACE = " (";
		String SEPARATOR = ", ";
		String CLOSING_BRACE = ");";

		String INTEGER = "INTEGER";
		String REAL = "REAL";
		String TEXT = "TEXT";
		String BLOB = "BLOB";

		String PK = Column.ID + " INTEGER PRIMARY KEY";
		String NOT_NULL = "NOT NULL";
		String UNIQUE = "UNIQUE";
	}

}
