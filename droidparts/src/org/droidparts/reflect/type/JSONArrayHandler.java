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

import org.droidparts.reflect.util.TypeHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;

public class JSONArrayHandler extends AbstractTypeHandler<JSONArray> {

	@Override
	public boolean canHandle(Class<?> cls) {
		return TypeHelper.isJSONArray(cls);
	}

	@Override
	public String getDBColumnType() {
		return TEXT;
	}

	@Override
	public Object getJSONValue(Object val) {
		return val.toString();
	}

	@Override
	public JSONArray readFromJSON(Class<?> cls, JSONObject obj, String key)
			throws JSONException {
		return new JSONArray(obj.getString(key));
	}

	@Override
	public void putToContentValues(ContentValues cv, String key, Object val) {
		cv.put(key, val.toString());
	}

	@Override
	public JSONArray readFromCursor(Class<?> cls, Cursor cursor, int columnIndex)
			throws IllegalArgumentException {
		try {
			return new JSONArray(cursor.getString(columnIndex));
		} catch (JSONException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public Object parseTypeArr(Class<?> arrValType, String[] arr) {
		ArrayList<JSONArray> list = toTypeColl(JSONArray.class, arr);
		return list.toArray(new JSONArray[list.size()]);
	}

}
