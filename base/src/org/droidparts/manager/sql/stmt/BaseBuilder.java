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

import org.droidparts.contract.DB;

import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

public abstract class BaseBuilder implements DB {

	protected final SQLiteDatabase db;
	protected final String tableName;

	public BaseBuilder(SQLiteDatabase db, String tableName) {
		this.db = db;
		this.tableName = tableName;
	}

	//

	private String seleciton;
	private String[] selectionArgs;
	private Where where;

	protected BaseBuilder where(String selection,
			Object... selectionArgs) {
		this.seleciton = selection;
		this.selectionArgs = toArgs(selectionArgs);
		return this;
	}

	protected BaseBuilder where(Where where) {
		this.where = where;
		return this;
	}

	protected Pair<String, String[]> getSelection() {
		if (where != null) {
			return where.buildSelection();
		} else {
			return Pair.create(seleciton, selectionArgs);
		}
	}

	//
	public static final String[] toArgs(Object... args) {
		String[] arr = new String[args.length];
		for (int i = 0; i < args.length; i++) {
			arr[i] = toArg(args[i]);
		}
		return arr;
	}

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
