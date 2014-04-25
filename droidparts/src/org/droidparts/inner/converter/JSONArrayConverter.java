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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;

public class JSONArrayConverter extends Converter<JSONArray> {

	@Override
	public boolean canHandle(Class<?> cls) {
		return TypeHelper.isJSONArray(cls);
	}

	@Override
	public String getDBColumnType() {
		return TEXT;
	}

	@Override
	public <V> void putToJSON(Class<JSONArray> valType, Class<V> componentType,
			JSONObject obj, String key, JSONArray val) throws JSONException {
		obj.put(key, val.toString());
	}

	@Override
	public <V> JSONArray readFromJSON(Class<JSONArray> valType,
			Class<V> componentType, JSONObject obj, String key)
			throws JSONException {
		return parseFromString(valType, componentType, obj.getString(key));
	}

	@Override
	protected <V> JSONArray parseFromString(Class<JSONArray> valType,
			Class<V> componentType, String str) {
		try {
			return new JSONArray(str);
		} catch (JSONException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public <V> void putToContentValues(Class<JSONArray> valueType,
			Class<V> componentType, ContentValues cv, String key, JSONArray val) {
		cv.put(key, val.toString());
	}

	@Override
	public <V> JSONArray readFromCursor(Class<JSONArray> valType,
			Class<V> componentType, Cursor cursor, int columnIndex) {
		try {
			return new JSONArray(cursor.getString(columnIndex));
		} catch (JSONException e) {
			throw new IllegalArgumentException(e);
		}
	}

}
