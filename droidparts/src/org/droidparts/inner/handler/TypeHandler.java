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

import java.util.ArrayList;

import org.droidparts.contract.SQL;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;

public abstract class TypeHandler<T> implements SQL.DDL {

	public abstract boolean canHandle(Class<?> cls);

	public abstract String getDBColumnType();

	public <V> Object convertForJSON(Class<T> valType,
			Class<V> arrCollItemType, T val) {
		return val;
	}

	public abstract <V> T readFromJSON(Class<T> valType,
			Class<V> arrCollItemType, JSONObject obj, String key)
			throws JSONException;

	protected abstract <V> T parseFromString(Class<T> valType,
			Class<V> arrCollItemType, String str);

	public abstract <V> void putToContentValues(Class<T> valueType,
			Class<V> arrCollItemType, ContentValues cv, String key, T val)
			throws IllegalArgumentException;

	public abstract <V> T readFromCursor(Class<T> valType,
			Class<V> arrCollItemType, Cursor cursor, int columnIndex)
			throws IllegalArgumentException;

	// say hello to arrays of primitives
	public abstract Object parseTypeArr(Class<T> valType, String[] arr);

	public final ArrayList<T> parseTypeColl(Class<T> valType, String[] arr)
			throws IllegalArgumentException {
		ArrayList<T> list = new ArrayList<T>();
		for (String str : arr) {
			list.add(parseFromString(valType, null, str));
		}
		return list;
	}

}
