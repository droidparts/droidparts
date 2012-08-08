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

import org.droidparts.manager.sql.AnnotatedEntityManager;
import org.droidparts.manager.sql.EntityManager;
import org.droidparts.model.Entity;

import android.app.Activity;
import android.database.Cursor;

public abstract class SimpleCursorAdapter<Model extends Entity> extends
		TypedCursorAdapter<Model> {

	protected final EntityManager<Model> entityManager;

	public SimpleCursorAdapter(Activity activity,
			EntityManager<Model> entityManager) {
		this(activity, entityManager, entityManager.list());
	}

	public SimpleCursorAdapter(Activity activity,
			EntityManager<Model> entityManager, Cursor cursor) {
		super(activity, cursor);
		this.entityManager = entityManager;
	}

	public boolean create(Model item) {
		boolean success = entityManager.create(item);
		return requeryOnSuccess(success);
	}

	public Model read(int position) {
		long id = getItemId(position);
		Model item = entityManager.read(id);
		String[] fieldNames = new String[0];
		if (entityManager instanceof AnnotatedEntityManager) {
			@SuppressWarnings("rawtypes")
			AnnotatedEntityManager aem = ((AnnotatedEntityManager) entityManager);
			fieldNames = aem.getEagerForeignKeyFieldNames();
		}
		entityManager.fillForeignKeys(item, fieldNames);
		return item;
	}

	public boolean update(Model item) {
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
			requery();
		}
		return success;
	}

	//
	@Deprecated
	public SimpleCursorAdapter(Activity activity, Cursor cursor,
			EntityManager<Model> entityManager) {
		this(activity, entityManager, cursor);
	}

}
