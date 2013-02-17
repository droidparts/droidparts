/**
 * Copyright 2013 Alex Yanchenko
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

import static org.droidparts.reflect.FieldSpecBuilder.getTableColumnSpecs;
import static org.droidparts.reflect.FieldSpecBuilder.getTableName;

import java.util.ArrayList;

import org.droidparts.contract.SQL;
import org.droidparts.model.Entity;
import org.droidparts.util.PersistUtils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class AbstractDBOpenHelper extends SQLiteOpenHelper implements
		SQL.DDL {

	public AbstractDBOpenHelper(Context ctx, String name, int version) {
		super(ctx.getApplicationContext(), name, null, version);
	}

	protected abstract Class<? extends Entity>[] getEntityClasses();

	protected void onCreateExtra(SQLiteDatabase db) {
	}

	protected void onOpenExtra(SQLiteDatabase db) {
	}

	// helpers

	protected boolean executeStatements(SQLiteDatabase db,
			ArrayList<String> queries) {
		return PersistUtils.executeStatements(db, queries);
	}

	protected boolean createIndex(SQLiteDatabase db, String table,
			boolean unique, String firstColumn, String... otherColumns) {
		ArrayList<String> statements = new ArrayList<String>();
		statements.add(PersistUtils.getCreateIndex(table, unique, firstColumn,
				otherColumns));
		return executeStatements(db, statements);
	}

	protected boolean dropTables(SQLiteDatabase db,
			String... optionalTableNames) {
		return PersistUtils.dropTables(db, optionalTableNames);
	}

	//

	@Override
	public final void onCreate(SQLiteDatabase db) {
		ArrayList<String> statements = new ArrayList<String>();
		for (Class<? extends Entity> cls : getEntityClasses()) {
			String query = PersistUtils.getSQLCreate(getTableName(cls),
					getTableColumnSpecs(cls));
			statements.add(query);
		}
		onOpen(db);
		executeStatements(db, statements);
		onCreateExtra(db);
	}

	@Override
	public final void onOpen(SQLiteDatabase db) {
		if (!db.isReadOnly()) {
			db.execSQL("PRAGMA foreign_keys = ON;");
		}
		onOpenExtra(db);
	}

}
