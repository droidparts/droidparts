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

import static org.droidparts.contract.DB.Column.ID;
import static org.droidparts.gram.contract.DB.Column.NAME;

import org.droidparts.gram.model.Filter;
import org.droidparts.persist.sql.EntityManager;
import org.droidparts.persist.sql.stmt.Is;

import android.content.Context;
import android.database.Cursor;

public class FilterEntityManager extends EntityManager<Filter> {

	public FilterEntityManager(Context ctx) {
		super(ctx, Filter.class);
	}

	public void readOrCreateForName(Filter filter) {
		Cursor cursor = select().columns(ID).where(NAME, Is.EQUAL, filter.name)
				.execute();
		try {
			if (cursor.moveToFirst()) {
				filter.id = cursor.getLong(0);
			} else {
				create(filter);
			}
		} finally {
			cursor.close();
		}
	}
}
