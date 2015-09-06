/**
 * Copyright 2015 Alex Yanchenko
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
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;

public class ByteConverter extends Converter<Byte> {

	@Override
	public boolean canHandle(Class<?> cls) {
		return TypeHelper.isByte(cls, true);
	}

	@Override
	public String getDBColumnType() {
		return INTEGER;
	}

	@Override
	public <G1, G2> Byte readFromJSON(Class<Byte> valType, Class<G1> genericType1, Class<G2> genericType2,
			JSONObject obj, String key) throws Exception {
		return parseFromString(valType, genericType1, genericType2, obj.getString(key));
	}

	@Override
	protected <G1, G2> Byte parseFromString(Class<Byte> valType, Class<G1> genericType1, Class<G2> genericType2,
			String str) {
		return Byte.valueOf(str);
	}

	@Override
	public <G1, G2> void putToContentValues(Class<Byte> valueType, Class<G1> genericType1, Class<G2> genericType2,
			ContentValues cv, String key, Byte val) {
		cv.put(key, val);
	}

	@Override
	public <G1, G2> Byte readFromCursor(Class<Byte> valType, Class<G1> genericType1, Class<G2> genericType2,
			Cursor cursor, int columnIndex) {
		return Byte.valueOf(cursor.getString(columnIndex));
	}

}
