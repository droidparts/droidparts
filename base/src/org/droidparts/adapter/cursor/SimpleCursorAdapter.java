/**
 * Copyright 2011 Alex Yanchenko
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

import org.droidparts.manager.sql.DBModelManager;
import org.droidparts.model.DBModel;

import android.app.Activity;
import android.database.Cursor;


public abstract class SimpleCursorAdapter<Model extends DBModel> extends
		TypedCursorAdapter<Model> {

	protected final DBModelManager<Model> modelManager;

	public SimpleCursorAdapter(Activity activity,
			DBModelManager<Model> modelManager) {
		this(activity, modelManager.list(null), modelManager);
	}

	public SimpleCursorAdapter(Activity activity, Cursor cursor,
			DBModelManager<Model> modelManager) {
		super(activity, cursor);
		this.modelManager = modelManager;
	}

	public boolean createItem(Model item) {
		boolean success = modelManager.create(item);
		return requeryOnSuccess(success);
	}

	public Model readItem(int position) {
		long id = getItemId(position);
		Model item = modelManager.read(id);
		modelManager.fillForeignKeys(item);
		return item;
	}

	public boolean updateItem(Model item) {
		boolean success = modelManager.update(item);
		return requeryOnSuccess(success);
	}

	public boolean deleteItem(int position) {
		long id = getItemId(position);
		boolean success = modelManager.delete(id);
		return requeryOnSuccess(success);
	}

	private boolean requeryOnSuccess(boolean success) {
		if (success) {
			requery();
		}
		return success;
	}

}
