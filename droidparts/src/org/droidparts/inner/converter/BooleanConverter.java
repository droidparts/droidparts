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
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;

public class BooleanConverter extends Converter<Boolean> {

	@Override
	public boolean canHandle(Class<?> cls) {
		return TypeHelper.isBoolean(cls, true);
	}

	@Override
	public String getDBColumnType() {
		return INTEGER;
	}

	@Override
	public <V> Boolean readFromJSON(Class<Boolean> valType,
			Class<V> componentType, JSONObject obj, String key)
			throws JSONException {
		try {
			return obj.getBoolean(key);
		} catch (JSONException e) {
			return parseFromString(valType, componentType, obj.getString(key));
		}
	}

	@Override
	protected <V> Boolean parseFromString(Class<Boolean> valType,
			Class<V> componentType, String str) {
		if ("1".equals(str)) {
			str = "true";
		}
		return Boolean.valueOf(str);
	}

	@Override
	public <V> void putToContentValues(Class<Boolean> valueType,
			Class<V> componentType, ContentValues cv, String key, Boolean val) {
		cv.put(key, val);
	}

	@Override
	public <V> Boolean readFromCursor(Class<Boolean> valType,
			Class<V> componentType, Cursor cursor, int columnIndex) {
		return (cursor.getInt(columnIndex) == 1);
	}

}
