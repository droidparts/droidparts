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
package org.droidparts.type.handler;

import java.util.ArrayList;
import java.util.Date;

import org.droidparts.type.TypeHelper;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;

public class DateHandler extends AbstractTypeHandler<Date> {

	@Override
	public boolean canHandle(Class<?> cls) {
		return TypeHelper.isDate(cls);
	}

	@Override
	public String getDBColumnType() {
		return INTEGER;
	}

	@Override
	public <V> Date readFromJSON(Class<Date> valType, Class<V> arrCollItemType,
			JSONObject obj, String key) throws JSONException {
		try {
			return new Date(obj.getLong(key));
		} catch (Exception e) {
			return parseFromString(valType, arrCollItemType, obj.getString(key));
		}
	}

	@Override
	protected <V> Date parseFromString(Class<Date> valType,
			Class<V> arrCollItemType, String str) {
		return new Date(Long.valueOf(str));
	}

	@Override
	public <V> Object convertForJSON(Class<Date> valType,
			Class<V> arrCollItemType, Date val) {
		return val.getTime();
	}

	@Override
	public <V> void putToContentValues(Class<Date> valueType,
			Class<V> arrCollItemType, ContentValues cv, String key, Date val) {
		cv.put(key, val.getTime());
	}

	@Override
	public <V> Date readFromCursor(Class<Date> valType,
			Class<V> arrCollItemType, Cursor cursor, int columnIndex) {
		return new Date(cursor.getLong(columnIndex));
	}

	@Override
	public Object parseTypeArr(Class<Date> valType, String[] arr) {
		ArrayList<Date> list = parseTypeColl(valType, arr);
		return list.toArray(new Date[list.size()]);
	}

}
