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

import org.droidparts.model.Entity;
import org.droidparts.util.L;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

public class Delete<EntityType extends Entity> extends Statement<EntityType> {

	public Delete(SQLiteDatabase db, String tableName) {
		super(db, tableName);
	}

	@Override
	public Delete<EntityType> whereId(long... oneOrMore) {
		return (Delete<EntityType>) super.whereId(oneOrMore);
	}

	@Override
	public Delete<EntityType> where(String columnName, Is operator,
			Object... columnValue) {
		return (Delete<EntityType>) super.where(columnName, operator,
				columnValue);
	}

	@Override
	public Delete<EntityType> where(Where where) {
		return (Delete<EntityType>) super.where(where);
	}

	@Override
	public Delete<EntityType> where(String selection, Object... selectionArgs) {
		return (Delete<EntityType>) super.where(selection, selectionArgs);
	}

	public int execute() {
		Pair<String, String[]> selection = getSelection();
		L.d(toString());
		int rowCount = 0;
		try {
			rowCount = db.delete(tableName, selection.first, selection.second);
		} catch (SQLException e) {
			L.e(e.getMessage());
			L.d(e);
		}
		return rowCount;
	}

	@Override
	public String toString() {
		return "DELETE" + super.toString() + ".";
	}

}
