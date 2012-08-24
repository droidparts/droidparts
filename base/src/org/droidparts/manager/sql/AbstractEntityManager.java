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
package org.droidparts.manager.sql;

import java.util.Collection;

import org.droidparts.contract.DB;
import org.droidparts.contract.SQL;
import org.droidparts.manager.sql.stmt.DeleteBuilder;
import org.droidparts.manager.sql.stmt.SelectBuilder;
import org.droidparts.manager.sql.stmt.StatementBuilder;
import org.droidparts.manager.sql.stmt.UpdateBuilder;
import org.droidparts.model.Entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public abstract class AbstractEntityManager<EntityType extends Entity>
		implements SQL {

	// CRUD methods

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

	public boolean create(EntityType item) {
		createOrUpdateForeignKeys(item);
		ContentValues cv = toContentValues(item);
		cv.remove(DB.Column.ID);
		long id = getDB().insert(getTableName(), null, cv);
		if (id > 0) {
			item.id = id;
			return true;
		} else {
			return false;
		}
	}

	public EntityType read(long id) {
		EntityType item = null;
		Cursor cursor = getDB().query(getTableName(), null,
				DB.Column.ID + EQUAL, toArgs(id), null, null, null);
		if (cursor.moveToFirst()) {
			item = readFromCursor(cursor);
		}
		cursor.close();
		return item;
	}

	public boolean update(EntityType item) {
		createOrUpdateForeignKeys(item);
		ContentValues cv = toContentValues(item);
		cv.remove(DB.Column.ID);
		int rowCount = getDB().update(getTableName(), cv, DB.Column.ID + EQUAL,
				toArgs(item.id));
		return rowCount > 0;
	}

	public boolean delete(long id) {
		int rowCount = getDB().delete(getTableName(), DB.Column.ID + EQUAL,
				toArgs(id));
		return rowCount > 0;
	}

	public boolean createOrUpdate(EntityType item) {
		boolean success;
		if (item.id > 0) {
			success = update(item);
		} else {
			success = create(item);
		}
		return success;
	}

	//

	public boolean create(Collection<EntityType> items) {
		int count = 0;
		getDB().beginTransaction();
		try {
			for (EntityType item : items) {
				boolean success = create(item);
				if (success) {
					count++;
				}
			}
			boolean success = (count == items.size());
			if (success) {
				getDB().setTransactionSuccessful();
			}
			return success;
		} finally {
			getDB().endTransaction();
		}
	}

	public boolean update(Collection<EntityType> items) {
		int count = 0;
		getDB().beginTransaction();
		try {
			for (EntityType item : items) {
				boolean success = update(item);
				if (success) {
					count++;
				}
			}
			boolean success = (count == items.size());
			if (success) {
				getDB().setTransactionSuccessful();
			}
			return success;
		} finally {
			getDB().endTransaction();
		}
	}

	public boolean delete(Collection<EntityType> items) {
		int count = 0;
		getDB().beginTransaction();
		try {
			for (EntityType item : items) {
				boolean success = delete(item.id);
				if (success) {
					count++;
				}
			}
			boolean success = (count == items.size());
			if (success) {
				getDB().setTransactionSuccessful();
			}
			return success;
		} finally {
			getDB().endTransaction();
		}
	}

	// statement builders

	public SelectBuilder select() {
		return new SelectBuilder(getDB(), getTableName());
	};

	public UpdateBuilder update() {
		return new UpdateBuilder(getDB(), getTableName());
	};

	public DeleteBuilder delete() {
		return new DeleteBuilder(getDB(), getTableName());
	};

	//

	public abstract EntityType readFromCursor(Cursor cursor);

	public abstract void fillForeignKeys(EntityType item, String... fieldNames);

	// utility methods

	public static final String[] toArgs(Object... args) {
		String[] arr = new String[args.length];
		for (int i = 0; i < args.length; i++) {
			arr[i] = StatementBuilder.toArg(args[i]);
		}
		return arr;
	}

	public static String sqlEscapeString(String val) {
		return StatementBuilder.sqlEscapeString(val);
	}

	protected abstract SQLiteDatabase getDB();

	protected abstract String getTableName();

	// boring stuff

	protected abstract ContentValues toContentValues(EntityType item);

	protected abstract void createOrUpdateForeignKeys(EntityType item);

}
