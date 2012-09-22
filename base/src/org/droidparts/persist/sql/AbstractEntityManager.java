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
package org.droidparts.persist.sql;

import java.util.ArrayList;
import java.util.Collection;

import org.droidparts.contract.DB;
import org.droidparts.contract.SQL;
import org.droidparts.model.Entity;
import org.droidparts.persist.sql.stmt.DeleteBuilder;
import org.droidparts.persist.sql.stmt.SelectBuilder;
import org.droidparts.persist.sql.stmt.UpdateBuilder;
import org.droidparts.util.L;
import org.droidparts.util.PersistUtils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public abstract class AbstractEntityManager<EntityType extends Entity>
		implements SQL {

	// CRUD methods

	public boolean create(EntityType item) {
		createOrUpdateForeignKeys(item);
		ContentValues cv = toContentValues(item);
		cv.remove(DB.Column.ID);
		long id = 0;
		try {
			id = getDB().insertOrThrow(getTableName(), null, cv);
		} catch (SQLException e) {
			L.e(e.getMessage());
			L.d(e);
		}
		if (id > 0) {
			item.id = id;
			return true;
		} else {
			return false;
		}
	}

	public EntityType read(long id) {
		Cursor cursor = select().whereId(id).execute();
		return readFirst(cursor);
	}

	public boolean update(EntityType item) {
		createOrUpdateForeignKeys(item);
		ContentValues cv = toContentValues(item);
		cv.remove(DB.Column.ID);
		int rowCount = update().whereId(item.id).setContent(cv).execute();
		return rowCount > 0;
	}

	public boolean delete(long id) {
		int rowCount = delete().whereId(id).execute();
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

	// mass CUD

	public boolean create(Collection<EntityType> items) {
		return cud(items, 1);
	}

	public boolean update(Collection<EntityType> items) {
		return cud(items, 2);
	}

	public boolean delete(Collection<EntityType> items) {
		return cud(items, 3);
	}

	private boolean cud(Collection<EntityType> items, int operation) {
		int count = 0;
		getDB().beginTransaction();
		try {
			for (EntityType item : items) {
				boolean success;
				switch (operation) {
				case 1:
					success = create(item);
					break;
				case 2:
					success = update(item);
					break;
				case 3:
					success = delete(item.id);
					break;
				default:
					throw new IllegalArgumentException();
				}
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

	public SelectBuilder<EntityType> select() {
		return new SelectBuilder<EntityType>(getDB(), getTableName());
	}

	public UpdateBuilder<EntityType> update() {
		return new UpdateBuilder<EntityType>(getDB(), getTableName());
	}

	public DeleteBuilder<EntityType> delete() {
		return new DeleteBuilder<EntityType>(getDB(), getTableName());
	}

	//

	public long[] readIds(Cursor cursor) {
		return PersistUtils.readIds(cursor);
	}

	public EntityType readFirst(Cursor cursor) {
		return PersistUtils.readFirst(this, cursor);
	}

	public ArrayList<EntityType> readAll(Cursor cursor) {
		return PersistUtils.readAll(this, cursor);
	}

	public abstract EntityType readRow(Cursor cursor);

	public abstract void fillForeignKeys(EntityType item, String... fieldNames);

	protected abstract SQLiteDatabase getDB();

	protected abstract String getTableName();

	// boring stuff

	protected abstract ContentValues toContentValues(EntityType item);

	protected abstract void createOrUpdateForeignKeys(EntityType item);

}
