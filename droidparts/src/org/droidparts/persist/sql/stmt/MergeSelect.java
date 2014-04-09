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

import android.database.Cursor;
import android.database.MergeCursor;

public class MergeSelect<EntityType extends Entity> implements
		AbstractSelect<EntityType> {

	private final Select<EntityType>[] selects;

	public MergeSelect(Select<EntityType>... selects) {
		this.selects = selects;
	}

	@Override
	public Cursor execute() {
		Cursor[] cursors = new Cursor[selects.length];
		for (int i = 0; i < cursors.length; i++) {
			cursors[i] = selects[i].execute();
		}
		return new MergeCursor(cursors);
	}

	@Override
	public int count() {
		int count = 0;
		for (Select<EntityType> select : selects) {
			count += select.count();
		}
		return count;
	}

}
