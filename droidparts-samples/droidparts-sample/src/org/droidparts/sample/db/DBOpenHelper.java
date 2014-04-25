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
package org.droidparts.sample.db;

import org.droidparts.persist.sql.AbstractDBOpenHelper;
import org.droidparts.sample.model.Entry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DBOpenHelper extends AbstractDBOpenHelper {

	private static final String DB_FILE = "droidparts_sample.sqlite";
	private static final int DB_VER = 1;

	public DBOpenHelper(Context ctx) {
		super(ctx, DB_FILE, DB_VER);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreateTables(SQLiteDatabase db) {
		createTables(db, Entry.class);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		dropTables(db);
		onCreate(db);
	}

}
