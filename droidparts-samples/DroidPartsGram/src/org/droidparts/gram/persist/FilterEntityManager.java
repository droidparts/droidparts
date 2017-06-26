/**
 * Copyright 2017 Alex Yanchenko
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

import android.content.Context;

import org.droidparts.gram.model.Filter;
import org.droidparts.persist.sql.EntityManager;
import org.droidparts.persist.sql.stmt.Is;
import org.droidparts.persist.sql.stmt.Select;

import org.droidparts.gram.contract.DB.Column;

public class FilterEntityManager extends EntityManager<Filter> {

	public FilterEntityManager(Context ctx) {
		super(Filter.class, ctx);
	}

	public void setIdOrCreateForName(Filter filter) {
		Select<Filter> select = select().columns(Column._ID).where(Column.NAME, Is.EQUAL, filter.name);
		if (select.count() == 1) {
			filter._id = readFirst(select)._id;
		} else {
			create(filter);
		}
	}
}
