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

import static org.droidparts.util.Strings.join;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.droidparts.util.Strings;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

public class SelectBuilder extends BaseBuilder {

	public SelectBuilder(SQLiteDatabase db, String tableName) {
		super(db, tableName);
	}

	//

	@Override
	public SelectBuilder where(String selection, Object... selectionArgs) {
		return (SelectBuilder) super.where(selection, selectionArgs);
	}

	@Override
	public SelectBuilder where(Where where) {
		return (SelectBuilder) super.where(where);
	}

	private String[] columns = null;
	private boolean distinct = false;
	private String[] groupBy = null;
	private final LinkedHashMap<String, Boolean> orderBy = new LinkedHashMap<String, Boolean>();
	private int limit = -1;

	public SelectBuilder columns(String... columns) {
		this.columns = columns;
		return this;
	}

	public SelectBuilder distinct(boolean distinct) {
		this.distinct = distinct;
		return this;
	}

	public SelectBuilder groupBy(String... columns) {
		this.groupBy = columns;
		return this;
	}

	public SelectBuilder having() {
		// TODO
		return this;
	}

	public SelectBuilder orderBy(String column, boolean ascending) {
		orderBy.put(column, ascending);
		return this;
	}

	public SelectBuilder limit(int limit) {
		return this;
	}

	public Cursor execute() {
		Pair<String, String[]> selection = getSelection();
		String groupByStr = null;
		if (groupBy != null && groupBy.length > 0) {
			groupByStr = Strings.join(groupBy, ", ", null);
		}
		String havingStr = null;
		// TODO
		String orderByStr = null;
		if (orderBy.size() > 0) {
			ArrayList<String> list = new ArrayList<String>();
			for (String key : orderBy.keySet()) {
				list.add(key + (orderBy.get(key) ? ASC : DESC));
			}
			orderByStr = join(list, ", ", null);
		}
		String limitStr = (limit > 0) ? String.valueOf(limit) : null;
		return db.query(distinct, tableName, columns, selection.first,
				selection.second, groupByStr, havingStr, orderByStr, limitStr);
	}
}
