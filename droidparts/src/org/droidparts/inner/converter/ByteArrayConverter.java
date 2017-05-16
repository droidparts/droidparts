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

import android.content.ContentValues;
import android.database.Cursor;

import org.droidparts.inner.TypeHelper;

public class ByteArrayConverter extends Converter<byte[]> {

	@Override
	public boolean canHandle(Class<?> cls) {
		return TypeHelper.isByteArray(cls);
	}

	@Override
	public String getDBColumnType() {
		return BLOB;
	}

	@Override
	public <G1, G2> void putToContentValues(Class<byte[]> valueType, Class<G1> genericArg1, Class<G2> genericArg2,
	                                        ContentValues cv, String key, byte[] val) {
		cv.put(key, val);
	}

	@Override
	public <G1, G2> byte[] readFromCursor(Class<byte[]> valType, Class<G1> genericArg1, Class<G2> genericArg2,
	                                      Cursor cursor, int columnIndex) {
		return cursor.getBlob(columnIndex);
	}

}
