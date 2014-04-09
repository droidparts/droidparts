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

public class IntegerConverter extends Converter<Integer> {

	@Override
	public boolean canHandle(Class<?> cls) {
		return TypeHelper.isInteger(cls, true);
	}

	@Override
	public String getDBColumnType() {
		return INTEGER;
	}

	@Override
	public <V> Integer readFromJSON(Class<Integer> valType,
			Class<V> componentType, JSONObject obj, String key)
			throws JSONException {
		return obj.getInt(key);
	}

	@Override
	protected <V> Integer parseFromString(Class<Integer> valType,
			Class<V> componentType, String str) {
		return Integer.valueOf(str);
	}

	@Override
	public <V> void putToContentValues(Class<Integer> valueType,
			Class<V> componentType, ContentValues cv, String key, Integer val) {
		cv.put(key, val);
	}

	@Override
	public <V> Integer readFromCursor(Class<Integer> valType,
			Class<V> componentType, Cursor cursor, int columnIndex) {
		return cursor.getInt(columnIndex);
	}

	// @Override
	// public Object parseTypeArr(Class<Integer> valType, String[] arr) {
	// Integer[] tArr = (Integer[]) super.parseTypeArr(valType, arr);
	// return (valType == int.class) ? toPrimitive(tArr) : tArr;
	// }

}
