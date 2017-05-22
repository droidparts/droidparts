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

import java.io.ByteArrayOutputStream;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

import org.droidparts.inner.TypeHelper;

public class BitmapConverter extends Converter<Bitmap> {

	@Override
	public boolean canHandle(Class<?> cls) {
		return TypeHelper.isBitmap(cls);
	}

	@Override
	public String getDBColumnType() {
		return BLOB;
	}

	@Override
	public <G1, G2> void putToContentValues(Class<Bitmap> valueType, Class<G1> genericArg1, Class<G2> genericArg2,
	                                        ContentValues cv, String key, Bitmap val) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		val.compress(CompressFormat.PNG, 0, baos);
		cv.put(key, baos.toByteArray());
	}

	@Override
	public <G1, G2> Bitmap readFromCursor(Class<Bitmap> valType, Class<G1> genericArg1, Class<G2> genericArg2,
	                                      Cursor cursor, int columnIndex) {
		byte[] arr = cursor.getBlob(columnIndex);
		return BitmapFactory.decodeByteArray(arr, 0, arr.length);
	}

}
