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

import org.droidparts.inner.TypeHelper;
import org.droidparts.model.Model;
import org.droidparts.persist.json.JSONSerializer;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;

public class ModelConverter extends Converter<Model> {

	@Override
	public boolean canHandle(Class<?> cls) {
		return TypeHelper.isModel(cls) && !TypeHelper.isEntity(cls);
	}

	@Override
	public String getDBColumnType() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <V> void putToContentValues(Class<Model> valueType,
			Class<V> componentType, ContentValues cv, String key, Model val) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <V> Model readFromCursor(Class<Model> valType,
			Class<V> componentType, Cursor cursor, int columnIndex) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <V> void putToJSON(Class<Model> valType, Class<V> componentType,
			JSONObject obj, String key, Model val) throws JSONException {
		@SuppressWarnings("unchecked")
		Class<Model> cls = (Class<Model>) val.getClass();
		JSONObject valStr = new JSONSerializer<Model>(cls, null).serialize(val);
		obj.put(key, valStr);
	}

	@Override
	public <V> Model readFromJSON(Class<Model> valType, Class<V> componentType,
			JSONObject obj, String key) throws JSONException {
		return new JSONSerializer<Model>(valType, null).deserialize(obj
				.getJSONObject(key));
	}

	@Override
	protected <V> Model parseFromString(Class<Model> valType,
			Class<V> componentType, String str) {
		try {
			return new JSONSerializer<Model>(valType, null)
					.deserialize(new JSONObject(str));
		} catch (JSONException e) {
			throw new IllegalArgumentException(e);
		}
	}

}
