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
package org.droidparts.persist.sql.stmt;

import static org.droidparts.inner.PersistUtils.getRowCount;
import static org.droidparts.util.Strings.join;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import org.droidparts.model.Entity;
import org.droidparts.util.L;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

public class Select<EntityType extends Entity> extends Statement<EntityType>
		implements AbstractSelect<EntityType> {

	private String[] columns = null;
	private boolean distinct = false;
	private String[] groupBy = null;
	private String having = null;
	private int offset = 0;
	private int limit = 0;
	private final LinkedHashMap<String, Boolean> orderBy = new LinkedHashMap<String, Boolean>();

	public Select(SQLiteDatabase db, String tableName) {
		super(db, tableName);
	}

	@Override
	public Select<EntityType> whereId(long... oneOrMore) {
		return (Select<EntityType>) super.whereId(oneOrMore);
	}

	@Override
	public Select<EntityType> where(String columnName, Is operator,
			Object... columnValue) {
		return (Select<EntityType>) super.where(columnName, operator,
				columnValue);
	}

	@Override
	public Select<EntityType> where(Where where) {
		return (Select<EntityType>) super.where(where);
	}

	@Override
	public Select<EntityType> where(String selection, Object... selectionArgs) {
		return (Select<EntityType>) super.where(selection, selectionArgs);
	}

	public Select<EntityType> columns(String... columns) {
		this.columns = columns;
		return this;
	}

	public Select<EntityType> distinct() {
		this.distinct = true;
		return this;
	}

	public Select<EntityType> groupBy(String... columns) {
		this.groupBy = columns;
		return this;
	}

	public Select<EntityType> having(String having) {
		this.having = having;
		return this;
	}

	public Select<EntityType> offset(int offset) {
		this.offset = offset;
		return this;
	}

	public Select<EntityType> limit(int limit) {
		this.limit = limit;
		return this;
	}

	public Select<EntityType> orderBy(String column, boolean ascending) {
		orderBy.put(column, ascending);
		return this;
	}

	@Override
	public Cursor execute() {
		buildArgs();
		L.d(describe("SELECT"));
		return db.query(distinct, tableName, columns, selection.first,
				selection.second, groupByStr, having, orderByStr, limitStr);
	}

	@Override
	public int count() {
		buildArgs();
		L.d(describe("COUNT"));
		return getRowCount(db, distinct, tableName, columns, selection.first,
				selection.second, groupByStr, having, orderByStr, limitStr);
	}

	private Pair<String, String[]> selection;
	private String groupByStr;
	private String orderByStr;
	private String limitStr;

	private void buildArgs() {
		selection = getSelection();
		groupByStr = null;
		if (groupBy != null && groupBy.length > 0) {
			groupByStr = join(groupBy, ", ");
		}
		orderByStr = null;
		if (!orderBy.isEmpty()) {
			ArrayList<String> list = new ArrayList<String>();
			for (String key : orderBy.keySet()) {
				list.add(key + (orderBy.get(key) ? ASC : DESC));
			}
			orderByStr = join(list, ", ");
		}
		limitStr = null;
		if (offset > 0) {
			limitStr = offset + DDL.SEPARATOR;
		}
		if (limit > 0) {
			if (limitStr == null) {
				limitStr = String.valueOf(limit);
			} else {
				limitStr += limit;
			}
		} else if (limitStr != null) {
			limitStr += Long.MAX_VALUE;
		}
	}

	private String describe(String prefix) {
		return prefix + super.toString() + ", columns: '"
				+ Arrays.toString(columns) + "', orderBy: '" + orderByStr
				+ "', groupBy: '" + groupByStr + "', having: '" + having
				+ "', distinct: '" + distinct + "', limit: '" + limitStr + "'.";
	}

	@Override
	public String toString() {
		buildArgs();
		return describe("SELECT");
	}
}
