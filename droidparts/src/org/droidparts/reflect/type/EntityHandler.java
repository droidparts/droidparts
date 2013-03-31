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
package org.droidparts.reflect.type;

import static org.droidparts.reflect.util.ReflectionUtils.instantiate;

import org.droidparts.model.Entity;
import org.droidparts.model.Model;
import org.droidparts.reflect.util.TypeHelper;

import android.content.ContentValues;
import android.database.Cursor;

public class EntityHandler extends ModelHandler {

	@Override
	public boolean canHandle(Class<?> cls) {
		return TypeHelper.isEntity(cls);
	}

	@Override
	public String getDBColumnType() {
		return INTEGER;
	}

	@Override
	public void putToContentValues(ContentValues cv, String key, Model val)
			throws IllegalArgumentException {
		Long id = (val != null) ? ((Entity) val).id : null;
		cv.put(key, id);
	}

	@Override
	public Entity readFromCursor(Class<Model> cls, Cursor cursor,
			int columnIndex) throws IllegalArgumentException {
		long id = cursor.getLong(columnIndex);
		Entity entity = (Entity) instantiate(cls);
		entity.id = id;
		return entity;
	}

}
