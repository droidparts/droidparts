/**
 * Copyright 2017 Alex Yanchenko
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

import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;

import org.json.JSONObject;

import org.droidparts.inner.TypeHelper;

public class DateConverter extends Converter<Date> {

	@Override
	public boolean canHandle(Class<?> cls) {
		return TypeHelper.isDate(cls);
	}

	@Override
	public String getDBColumnType() {
		return INTEGER;
	}

	@Override
	public <G1, G2> void putToJSON(Class<Date> valType, Class<G1> genericArg1, Class<G2> genericArg2, JSONObject obj,
	                               String key, Date val) throws Exception {
		obj.put(key, val.getTime());
	}

	@Override
	public <G1, G2> Date readFromJSON(Class<Date> valType, Class<G1> genericArg1, Class<G2> genericArg2, JSONObject obj,
	                                  String key) throws Exception {
		try {
			return new Date(obj.getLong(key));
		} catch (Exception e) {
			return parseFromString(valType, genericArg1, genericArg2, obj.getString(key));
		}
	}

	@Override
	protected <G1, G2> Date parseFromString(Class<Date> valType, Class<G1> genericArg1, Class<G2> genericArg2,
	                                        String str) {
		return new Date(Long.valueOf(str));
	}

	@Override
	public <G1, G2> void putToContentValues(Class<Date> valueType, Class<G1> genericArg1, Class<G2> genericArg2,
	                                        ContentValues cv, String key, Date val) {
		cv.put(key, val.getTime());
	}

	@Override
	public <G1, G2> Date readFromCursor(Class<Date> valType, Class<G1> genericArg1, Class<G2> genericArg2,
	                                    Cursor cursor, int columnIndex) {
		return new Date(cursor.getLong(columnIndex));
	}

}
