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
package org.droidparts.inner.converter;

import static org.droidparts.inner.ReflectionUtils.newInstance;

import org.droidparts.inner.TypeHelper;
import org.droidparts.model.Entity;
import org.droidparts.model.Model;

import android.content.ContentValues;
import android.database.Cursor;

public class EntityConverter extends ModelConverter {

	@Override
	public boolean canHandle(Class<?> cls) {
		return TypeHelper.isEntity(cls);
	}

	@Override
	public String getDBColumnType() {
		return INTEGER;
	}

	@Override
	public <V> void putToContentValues(Class<Model> valueType,
			Class<V> componentType, ContentValues cv, String key, Model val) {
		cv.put(key, ((Entity) val).id);
	}

	@Override
	public <V> Entity readFromCursor(Class<Model> valType,
			Class<V> componentType, Cursor cursor, int columnIndex) {
		long id = cursor.getLong(columnIndex);
		Entity entity = (Entity) newInstance(valType);
		entity.id = id;
		return entity;
	}

	@Override
	protected <V> Model parseFromString(Class<Model> valType,
			Class<V> componentType, String str) {
		if (str.startsWith("{")) {
			// XXX it's a JSON Object
			return super.parseFromString(valType, componentType, str);
		} else {
			Entity entity = (Entity) newInstance(valType);
			entity.id = Long.valueOf(str);
			return entity;
		}
	}

}
