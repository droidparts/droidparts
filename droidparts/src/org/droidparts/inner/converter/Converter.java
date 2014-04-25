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

import org.droidparts.contract.SQL;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;

public abstract class Converter<T> implements SQL.DDL {

	public abstract boolean canHandle(Class<?> cls);

	public abstract String getDBColumnType();

	protected abstract <V> T parseFromString(Class<T> valType,
			Class<V> componentType, String str);

	public <V> void putToJSON(Class<T> valType, Class<V> componentType,
			JSONObject obj, String key, T val) throws JSONException {
		obj.put(key, val);
	}

	public abstract <V> T readFromJSON(Class<T> valType,
			Class<V> componentType, JSONObject obj, String key)
			throws JSONException;

	public abstract <V> void putToContentValues(Class<T> valueType,
			Class<V> componentType, ContentValues cv, String key, T val);

	public abstract <V> T readFromCursor(Class<T> valType,
			Class<V> componentType, Cursor cursor, int columnIndex);

}
