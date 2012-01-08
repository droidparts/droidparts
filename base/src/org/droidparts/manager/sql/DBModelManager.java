/**
 * Copyright 2011 Alex Yanchenko
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
package org.droidparts.manager.sql;

import org.droidparts.contract.SQLConstants;
import org.droidparts.model.DBModel;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public abstract class DBModelManager<Model extends DBModel> implements
		SQLConstants {

	public Cursor list(String... columns) {
		return list(null, columns);
	}

	protected Cursor list(String orderBy, String... columns) {
		if (columns != null && columns.length == 0) {
			columns = null;
		}
		Cursor cursor = getDB().query(getTableName(), columns, null, null,
				null, null, orderBy);
		return cursor;
	}

	public boolean create(Model item) {
		createOrUpdateForeignKeys(item);
		ContentValues cv = toContentValues(item);
		cv.remove(Column.ID);
		long id = getDB().insert(getTableName(), null, cv);
		if (id > 0) {
			item.id = id;
			return true;
		} else {
			return false;
		}
	}

	public Model read(long id) {
		Model item = null;
		Cursor cursor = getDB().query(getTableName(), null, Column.ID + EQUALS,
				toStrArr(id), null, null, null);
		if (cursor.moveToFirst()) {
			item = readFromCursor(cursor);
		}
		cursor.close();
		return item;
	}

	public boolean update(Model item) {
		createOrUpdateForeignKeys(item);
		ContentValues cv = toContentValues(item);
		cv.remove(Column.ID);
		int rowCount = getDB().update(getTableName(), cv, Column.ID + EQUALS,
				toStrArr(item.id));
		return rowCount > 0;
	}

	public boolean delete(long id) {
		int rowCount = getDB().delete(getTableName(), Column.ID + EQUALS,
				toStrArr(id));
		return rowCount > 0;
	}

	public boolean createOrUpdate(Model item) {
		boolean success;
		if (item.id > 0) {
			success = update(item);
		} else {
			success = create(item);
		}
		return success;
	}

	protected final String[] toStrArr(Object... args) {
		String[] strArgs = new String[args.length];
		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			if (arg != null) {
				strArgs[i] = String.valueOf(arg);
			} else {
				throw new IllegalArgumentException("null arg at index " + i);
			}
		}
		return strArgs;
	}

	public abstract Model readFromCursor(Cursor cursor);

	public abstract void fillForeignKeys(Model item);

	protected abstract SQLiteDatabase getDB();

	protected abstract String getTableName();

	protected abstract ContentValues toContentValues(Model item);

	protected abstract void createOrUpdateForeignKeys(Model item);

}
