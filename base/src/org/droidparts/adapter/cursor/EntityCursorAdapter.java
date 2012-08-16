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
package org.droidparts.adapter.cursor;

import org.droidparts.manager.sql.EntityManager;

import android.app.Activity;
import android.database.Cursor;

public abstract class EntityCursorAdapter<Entity extends org.droidparts.model.Entity>
		extends CursorAdapter<Entity> {

	protected final EntityManager<Entity> entityManager;

	public EntityCursorAdapter(Activity activity,
			EntityManager<Entity> entityManager) {
		this(activity, entityManager, entityManager.list());
	}

	public EntityCursorAdapter(Activity activity,
			EntityManager<Entity> entityManager, Cursor cursor) {
		super(activity, cursor);
		this.entityManager = entityManager;
	}

	public boolean create(Entity item) {
		boolean success = entityManager.create(item);
		return requeryOnSuccess(success);
	}

	public Entity read(int position) {
		long id = getItemId(position);
		Entity item = entityManager.read(id);
		String[] eagerFieldNames = entityManager.getEagerForeignKeyFieldNames();
		if (eagerFieldNames.length != 0) {
			entityManager.fillForeignKeys(item, eagerFieldNames);
		}
		return item;
	}

	public boolean update(Entity item) {
		boolean success = entityManager.update(item);
		return requeryOnSuccess(success);
	}

	public boolean delete(int position) {
		long id = getItemId(position);
		boolean success = entityManager.delete(id);
		return requeryOnSuccess(success);
	}

	private boolean requeryOnSuccess(boolean success) {
		if (success) {
			getCursor().requery();
		}
		return success;
	}

}
