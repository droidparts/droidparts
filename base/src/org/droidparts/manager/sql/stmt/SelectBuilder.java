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
import java.util.Arrays;
import java.util.LinkedHashMap;

import org.droidparts.util.L;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

public class SelectBuilder extends StatementBuilder {

	public SelectBuilder(SQLiteDatabase db, String tableName) {
		super(db, tableName);
	}

	//

	@Override
	public SelectBuilder where(String column, Where operator, Object val) {
		return (SelectBuilder) super.where(column, operator, val);
	}

	private String[] columns = null;
	private boolean distinct = false;
	private String[] groupBy = null;
	private String having = null;
	private final LinkedHashMap<String, Boolean> orderBy = new LinkedHashMap<String, Boolean>();
	private int limit = -1;

	public SelectBuilder columns(String... columns) {
		this.columns = columns;
		return this;
	}

	public SelectBuilder distinct() {
		this.distinct = true;
		return this;
	}

	public SelectBuilder groupBy(String... columns) {
		this.groupBy = columns;
		return this;
	}

	public SelectBuilder having(String having) {
		this.having = having;
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
		Pair<String, String[]> selection = buildSelection();
		String groupByStr = null;
		if (groupBy != null && groupBy.length > 0) {
			groupByStr = join(groupBy, ", ", null);
		}
		String orderByStr = null;
		if (orderBy.size() > 0) {
			ArrayList<String> list = new ArrayList<String>();
			for (String key : orderBy.keySet()) {
				list.add(key + (orderBy.get(key) ? ASC : DESC));
			}
			orderByStr = join(list, ", ", null);
		}
		String limitStr = (limit > 0) ? String.valueOf(limit) : null;
		L.d("Distinct: '" + distinct + "', tableName: '" + tableName
				+ "', columns: '" + Arrays.toString(columns)
				+ "', selection: '" + selection.first + "', selectionArgs: '"
				+ Arrays.toString(selection.second) + "', groupBy: '"
				+ groupByStr + "', having: '" + having + "', orderBy: '"
				+ orderByStr + "', limit: '" + limitStr + "'.");
		return db.query(distinct, tableName, columns, selection.first,
				selection.second, groupByStr, having, orderByStr, limitStr);
	}
}
