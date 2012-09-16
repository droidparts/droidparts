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
package org.droidparts.gram.persist;

import org.droidparts.gram.contract.DB;
import org.droidparts.gram.model.Filter;
import org.droidparts.gram.model.Image;
import org.droidparts.model.Entity;
import org.droidparts.persist.sql.AbstractDBOpenHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DBOpenHelper extends AbstractDBOpenHelper {

	public DBOpenHelper(Context ctx) {
		super(ctx, DB.FILE, DB.VERSION);
	}

	@Override
	protected Class<? extends Entity>[] getEntityClasses() {
		@SuppressWarnings("unchecked")
		Class<? extends Entity>[] arr = new Class[] { Filter.class, Image.class };
		return arr;
	}

	@Override
	protected void onCreateExtra(SQLiteDatabase db) {
		createIndex(db, DB.Table.IMAGES, false, DB.Column.CAPTION);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		dropAll(db, true, true);
		onCreate(db);
	}

}
