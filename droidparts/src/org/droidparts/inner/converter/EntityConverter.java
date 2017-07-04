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
package org.droidparts.inner.converter;

import android.content.ContentValues;
import android.database.Cursor;

import org.droidparts.inner.TypeHelper;
import org.droidparts.model.Entity;

import static org.droidparts.inner.ReflectionUtils.newInstance;

public class EntityConverter<E extends Entity> extends ModelConverter<E> {

	@Override
	public boolean canHandle(Class<?> cls) {
		return TypeHelper.isEntity(cls);
	}

	@Override
	public String getDBColumnType() {
		return INTEGER;
	}

	@Override
	public <G1, G2> void putToContentValues(Class<E> valueType, Class<G1> genericArg1, Class<G2> genericArg2,
	                                        ContentValues cv, String key, E val) {
		cv.put(key, val._id);
	}

	@Override
	public <G1, G2> E readFromCursor(Class<E> valType, Class<G1> genericArg1, Class<G2> genericArg2, Cursor cursor,
	                                 int columnIndex) {
		long id = cursor.getLong(columnIndex);
		E entity = newInstance(valType);
		entity._id = id;
		return entity;
	}

	@Override
	protected <G1, G2> E parseFromString(Class<E> valType, Class<G1> genericArg1, Class<G2> genericArg2, String str)
			throws Exception {
		if (str.startsWith("{")) {
			// XXX it's a JSON Object
			return super.parseFromString(valType, genericArg1, genericArg2, str);
		} else {
			E entity = newInstance(valType);
			entity._id = Long.valueOf(str);
			return entity;
		}
	}

}
