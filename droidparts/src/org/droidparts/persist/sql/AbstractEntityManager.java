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
package org.droidparts.persist.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.droidparts.contract.DB;
import org.droidparts.contract.SQL;
import org.droidparts.inner.PersistUtils;
import org.droidparts.model.Entity;
import org.droidparts.persist.sql.stmt.Delete;
import org.droidparts.persist.sql.stmt.Select;
import org.droidparts.persist.sql.stmt.Update;
import org.droidparts.util.L;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public abstract class AbstractEntityManager<EntityType extends Entity>
		implements SQL {

	// CRUD methods

	public boolean create(EntityType item) {
		createForeignKeys(item);
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
		return readFirst(select().whereId(id));
	}

	public boolean update(EntityType item) {
		createForeignKeys(item);
		ContentValues cv = toContentValues(item);
		cv.remove(DB.Column.ID);
		int rowCount = update().whereId(item.id).setValues(cv).execute();
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

	public int create(Collection<EntityType> items) {
		return cud(items, 1);
	}

	public int update(Collection<EntityType> items) {
		return cud(items, 2);
	}

	public int delete(Collection<EntityType> items) {
		return cud(items, 3);
	}

	private int cud(final Collection<EntityType> items, final int operation) {
		Callable<Integer> task = new Callable<Integer>() {

			@Override
			public Integer call() throws Exception {
				int count = 0;
				for (EntityType item : items) {
					boolean success = false;
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
					}
					if (success) {
						count++;
					}
				}
				return count;
			}
		};
		Integer result = executeInTransaction(task);
		return (result != null) ? result : 0;
	}

	// statement builders

	public Select<EntityType> select() {
		return new Select<EntityType>(getDB(), getTableName());
	}

	public Update<EntityType> update() {
		return new Update<EntityType>(getDB(), getTableName());
	}

	public Delete<EntityType> delete() {
		return new Delete<EntityType>(getDB(), getTableName());
	}

	//

	public <Result> Result executeInTransaction(Callable<Result> task) {
		return PersistUtils.executeInTransaction(getDB(), task);
	}

	public long[] readIds(Select<EntityType> select) {
		return PersistUtils.readIds(select.execute());
	}

	public EntityType readFirst(Select<EntityType> select) {
		EntityType item = PersistUtils.readFirst(this, select.execute());
		if (item != null) {
			fillEagerForeignKeys(item);
		}
		return item;
	}

	public ArrayList<EntityType> readAll(Select<EntityType> select) {
		ArrayList<EntityType> list = PersistUtils.readAll(this,
				select.execute());
		for (EntityType item : list) {
			fillEagerForeignKeys(item);
		}
		return list;
	}

	public abstract EntityType readRow(Cursor cursor);

	public abstract void fillForeignKeys(EntityType item, String... columnNames);

	protected abstract SQLiteDatabase getDB();

	protected abstract String getTableName();

	// boring stuff

	protected abstract ContentValues toContentValues(EntityType item);

	protected abstract void createForeignKeys(EntityType item);

	protected abstract void fillEagerForeignKeys(EntityType item);

}
