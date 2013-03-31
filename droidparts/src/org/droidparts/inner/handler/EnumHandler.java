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

import static org.droidparts.inner.ReflectionUtils.instantiateEnum;

import java.lang.reflect.Array;
import java.util.ArrayList;

import org.droidparts.inner.TypeHelper;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;

public class EnumHandler extends AbstractTypeHandler<Enum<?>> {

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
			Class<V> arrCollItemType, Enum<?> val) {
		return val.toString();
	}

	@Override
	public <V> Enum<?> readFromJSON(Class<Enum<?>> valType,
			Class<V> arrCollItemType, JSONObject obj, String key)
			throws JSONException {
		return parseFromString(valType, arrCollItemType, obj.getString(key));
	}

	@Override
	protected <V> Enum<?> parseFromString(Class<Enum<?>> valType,
			Class<V> arrCollItemType, String str) {
		return instantiateEnum(valType, str);
	}

	@Override
	public <V> void putToContentValues(Class<Enum<?>> valueType,
			Class<V> arrCollItemType, ContentValues cv, String key, Enum<?> val) {
		cv.put(key, val.toString());
	}

	@Override
	public <V> Enum<?> readFromCursor(Class<Enum<?>> valType,
			Class<V> arrCollItemType, Cursor cursor, int columnIndex) {
		return instantiateEnum(valType, cursor.getString(columnIndex));
	}

	@Override
	public Object parseTypeArr(Class<Enum<?>> valType, String[] arr) {
		ArrayList<? extends Enum<?>> list = (ArrayList<? extends Enum<?>>) parseTypeColl(
				valType, arr);
		Object enumArr = Array.newInstance(valType, list.size());
		for (int i = 0; i < list.size(); i++) {
			Array.set(enumArr, i, list.get(i));
		}
		return enumArr;
	}

}
