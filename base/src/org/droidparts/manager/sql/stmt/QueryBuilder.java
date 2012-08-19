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
package org.droidparts.manager.sql.stmt;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

public class QueryBuilder extends BaseSelectionBuilder {

	public QueryBuilder(SQLiteDatabase db, String tableName) {
		super(db, tableName);
	}

	//

	@Override
	public QueryBuilder equals(String column, Object val) {
		return (QueryBuilder) super.equals(column, val);
	}

	@Override
	public QueryBuilder notEqual(String column, Object val) {
		return (QueryBuilder) super.notEqual(column, val);
	}

	@Override
	public QueryBuilder lessThan(String column, Object val) {
		return (QueryBuilder) super.lessThan(column, val);
	}

	@Override
	public QueryBuilder greaterThan(String column, Object val) {
		return (QueryBuilder) super.greaterThan(column, val);
	}

	private String[] columns = null;
	private boolean distinct = false;
	private String[] groupBy = null;
	private int limit = -1;

	public QueryBuilder columns(String... columns) {
		this.columns = columns;
		return this;
	}

	public QueryBuilder distinct(boolean distinct) {
		this.distinct = distinct;
		return this;
	}

	public QueryBuilder groupBy(String... columns) {
		this.groupBy = columns;
		return this;
	}

	public QueryBuilder having() {
		// TODO
		return this;
	}

	public QueryBuilder orderByAsc(String column) {
		// TODO
		return this;
	}

	public QueryBuilder orderByDesc(String column) {
		// TODO
		return this;
	}

	public QueryBuilder limit(int limit) {
		return this;
	}

	public Cursor execute() {
		Pair<String, String[]> selection = buildSelection();
		// TODO
		return db.query(distinct, tableName, columns, selection.first,
				selection.second, null, null, null, null);
	}

}
