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

public class ByteHandler extends AbstractTypeHandler<Byte> {

	@Override
	public boolean canHandle(Class<?> cls) {
		return TypeHelper.isByte(cls);
	}

	@Override
	public String getDBColumnType() {
		return INTEGER;
	}

	@Override
	public <V> Byte readFromJSON(Class<Byte> valType, Class<V> arrCollItemType,
			JSONObject obj, String key) throws JSONException {
		return parseFromString(valType, arrCollItemType, obj.getString(key));
	}

	@Override
	protected <V> Byte parseFromString(Class<Byte> valType,
			Class<V> arrCollItemType, String str) {
		return Byte.valueOf(str);
	}

	@Override
	public <V> void putToContentValues(Class<Byte> valueType,
			Class<V> arrCollItemType, ContentValues cv, String key, Byte val) {
		cv.put(key, val);
	}

	@Override
	public <V> Byte readFromCursor(Class<Byte> valType,
			Class<V> arrCollItemType, Cursor cursor, int columnIndex) {
		return Byte.valueOf(cursor.getString(columnIndex));
	}

	@Override
	public Object parseTypeArr(Class<Byte> valType, String[] arr) {
		ArrayList<Byte> list = parseTypeColl(valType, arr);
		Byte[] tArr = list.toArray(new Byte[list.size()]);
		return (valType == byte.class) ? toPrimitive(tArr) : tArr;
	}

}
