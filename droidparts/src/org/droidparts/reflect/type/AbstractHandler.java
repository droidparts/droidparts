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
package org.droidparts.reflect.type;

import java.util.ArrayList;

import org.droidparts.contract.SQL;
import org.droidparts.reflect.util.TypeHandlerRegistry;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;

public abstract class AbstractHandler<T> implements SQL.DDL {

	public abstract boolean canHandle(Class<?> cls);

	public abstract String getDBColumnType();

	public abstract T readFromJSON(Class<?> cls, JSONObject obj, String key)
			throws JSONException;

	public abstract void putToJSONObject(JSONObject obj, String key, Object val)
			throws JSONException;

	public abstract void putToContentValues(ContentValues cv, String key,
			Object val) throws IllegalArgumentException;

	public abstract T readFromCursor(Class<?> cls, Cursor cursor,
			int columnIndex) throws IllegalArgumentException;

	public abstract Object parseTypeArr(Class<?> arrValType, String[] arr);

	// XXX
	public static <T> ArrayList<T> toTypeColl(Class<T> valCls,
			String[] valStrArr) throws IllegalArgumentException {
		ArrayList<Object> list = new ArrayList<Object>();
		String key = "key";
		JSONObject hackObj = new JSONObject();
		AbstractHandler<?> handler = TypeHandlerRegistry.get(valCls);
		for (String str : valStrArr) {
			try {
				hackObj.put(key, str);
				Object val = handler.readFromJSON(valCls, hackObj, key);
				list.add(val);
			} catch (JSONException e) {
				throw new IllegalArgumentException("Unable to convert '" + str
						+ "' to " + valCls.getSimpleName() + ".");
			}
		}
		@SuppressWarnings("unchecked")
		ArrayList<T> typedList = (ArrayList<T>) list;
		return typedList;
	}

}
