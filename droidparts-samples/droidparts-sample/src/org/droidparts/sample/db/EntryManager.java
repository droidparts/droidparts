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

import java.util.Date;

import org.droidparts.persist.sql.EntityManager;
import org.droidparts.sample.model.Entry;

import android.content.Context;

public class EntryManager extends EntityManager<Entry> {

	public EntryManager(Context ctx) {
		super(Entry.class, ctx);
	}

	@Override
	public boolean create(Entry item) {
		item.created = new Date();
		return super.create(item);
	}

}
