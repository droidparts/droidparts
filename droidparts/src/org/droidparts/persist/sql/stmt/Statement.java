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

import static org.droidparts.inner.PersistUtils.toWhereArgs;

import java.util.Arrays;

import org.droidparts.contract.DB;
import org.droidparts.contract.SQL;
import org.droidparts.model.Entity;

import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

public abstract class Statement<EntityType extends Entity> implements SQL {

	protected final SQLiteDatabase db;
	protected final String tableName;

	private Where where;

	private String selection;
	private String[] selectionArgs;

	public Statement(SQLiteDatabase db, String tableName) {
		this.db = db;
		this.tableName = tableName;
	}

	protected Statement<EntityType> whereId(long... oneOrMore) {
		if (oneOrMore.length == 1) {
			return where(DB.Column.ID, Is.EQUAL, oneOrMore[0]);
		} else {
			return where(DB.Column.ID, Is.IN, oneOrMore);
		}
	}

	protected Statement<EntityType> where(String columnName, Is operator,
			Object... columnValue) {
		return where(new Where(columnName, operator, columnValue));
	}

	protected Statement<EntityType> where(Where where) {
		selection = null;
		if (this.where == null) {
			this.where = where;
		} else {
			this.where.and(where);
		}
		return this;
	}

	protected Statement<EntityType> where(String selection,
			Object... selectionArgs) {
		where = null;
		this.selection = selection;
		this.selectionArgs = toWhereArgs(selectionArgs);
		return this;
	}

	protected Pair<String, String[]> getSelection() {
		if (selection == null && where != null) {
			Pair<String, Object[]> p = where.build();
			selection = p.first;
			selectionArgs = toWhereArgs(p.second);
		}
		return Pair.create(selection, selectionArgs);
	}

	@Override
	public String toString() {
		Pair<String, String[]> sel = getSelection();
		return " on table '" + tableName + "', selection: '" + sel.first
				+ "', selectionArgs: '" + Arrays.toString(sel.second) + "'";
	}

}
