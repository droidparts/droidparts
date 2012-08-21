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

import java.util.ArrayList;

import org.droidparts.contract.DB;

import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

public abstract class StatementBuilder implements DB {

	protected final SQLiteDatabase db;
	protected final String tableName;

	public StatementBuilder(SQLiteDatabase db, String tableName) {
		this.db = db;
		this.tableName = tableName;
	}

	//

	private final ArrayList<Pair<String, Pair<Where, Object>>> selection = new ArrayList<Pair<String, Pair<Where, Object>>>();

	protected StatementBuilder where(String column, Where operator, Object val) {
		selection.add(Pair.create(column, Pair.create(operator, val)));
		return this;
	}

	protected Pair<String, String[]> buildSelection() {
		StringBuilder whereBuilder = new StringBuilder();
		ArrayList<String> whereArgs = new ArrayList<String>();
		for (int i = 0; i < selection.size(); i++) {
			Pair<String, Pair<Where, Object>> p = selection.get(i);
			String columnName = p.first;
			String operator = p.second.first.str;
			String columnVal = StatementBuilder.toArg(p.second.second);
			if (i > 0) {
				whereBuilder.append(AND);
			}
			whereBuilder.append(columnName).append(operator);
			whereArgs.add(columnVal);
		}
		String where = whereBuilder.toString();
		String[] whereArgsArr = whereArgs.toArray(new String[whereArgs.size()]);
		return Pair.create(where, whereArgsArr);
	}

	//

	public static String toArg(Object arg) {
		if (arg == null) {
			arg = "NULL";
		} else if (arg instanceof Boolean) {
			arg = ((Boolean) arg) ? 1 : 0;
		}
		return String.valueOf(arg);
	}

	public static String sqlEscapeString(String val) {
		val = DatabaseUtils.sqlEscapeString(val);
		val = val.substring(1, val.length() - 1);
		return val;
	}

}
