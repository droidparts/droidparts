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
package org.droidparts.inner.handler;

import static org.droidparts.inner.ReflectionUtils.newEnum;

import org.droidparts.inner.TypeHelper;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;

public class EnumHandler extends TypeHandler<Enum<?>> {

	@Override
	public boolean canHandle(Class<?> cls) {
		return TypeHelper.isEnum(cls);
	}

	@Override
	public String getDBColumnType() {
		return TEXT;
	}

	@Override
	public <V> Object convertForJSON(Class<Enum<?>> valType,
			Class<V> arrCollElementType, Enum<?> val) {
		return val.toString();
	}

	@Override
	public <V> Enum<?> readFromJSON(Class<Enum<?>> valType,
			Class<V> arrCollElementType, JSONObject obj, String key)
			throws JSONException {
		return parseFromString(valType, arrCollElementType, obj.getString(key));
	}

	@Override
	protected <V> Enum<?> parseFromString(Class<Enum<?>> valType,
			Class<V> arrCollElementType, String str) {
		return newEnum(valType, str);
	}

	@Override
	public <V> void putToContentValues(Class<Enum<?>> valueType,
			Class<V> arrCollElementType, ContentValues cv, String key,
			Enum<?> val) {
		cv.put(key, val.toString());
	}

	@Override
	public <V> Enum<?> readFromCursor(Class<Enum<?>> valType,
			Class<V> arrCollElementType, Cursor cursor, int columnIndex) {
		return newEnum(valType, cursor.getString(columnIndex));
	}

}
