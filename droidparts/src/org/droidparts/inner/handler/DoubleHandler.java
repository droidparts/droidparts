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

import static org.droidparts.util.Arrays2.toPrimitive;

import java.util.ArrayList;

import org.droidparts.inner.TypeHelper;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;

public class DoubleHandler extends TypeHandler<Double> {

	@Override
	public boolean canHandle(Class<?> cls) {
		return TypeHelper.isDouble(cls);
	}

	@Override
	public String getDBColumnType() {
		return REAL;
	}

	@Override
	public <V> Double readFromJSON(Class<Double> valType,
			Class<V> arrCollItemType, JSONObject obj, String key)
			throws JSONException {
		return obj.getDouble(key);
	}

	@Override
	protected <V> Double parseFromString(Class<Double> valType,
			Class<V> arrCollItemType, String str) {
		return Double.valueOf(str);
	}

	@Override
	public <V> void putToContentValues(Class<Double> valueType,
			Class<V> arrCollItemType, ContentValues cv, String key, Double val) {
		cv.put(key, val);
	}

	@Override
	public <V> Double readFromCursor(Class<Double> valType,
			Class<V> arrCollItemType, Cursor cursor, int columnIndex) {
		return cursor.getDouble(columnIndex);
	}

	@Override
	public Object parseTypeArr(Class<Double> valType, String[] arr) {
		ArrayList<Double> list = parseTypeColl(valType, arr);
		Double[] tArr = list.toArray(new Double[list.size()]);
		return (valType == double.class) ? toPrimitive(tArr) : tArr;
	}

}
