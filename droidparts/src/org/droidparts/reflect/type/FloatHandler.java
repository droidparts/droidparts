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

import static org.droidparts.util.Arrays2.toPrimitive;

import java.util.ArrayList;

import org.droidparts.reflect.util.TypeHelper;

import android.content.ContentValues;
import android.database.Cursor;

public class FloatHandler extends TypeHandler<Float> {

	@Override
	public boolean canHandle(Class<?> cls) {
		return TypeHelper.isFloat(cls);
	}

	@Override
	public String getDBColumnType() {
		return REAL;
	}

	@Override
	protected Float parseFromString(Class<Float> cls, String str) {
		return Float.valueOf(str);
	}

	@Override
	public void putToContentValues(ContentValues cv, String key, Object val) {
		cv.put(key, (Float) val);
	}

	@Override
	public Float readFromCursor(Class<Float> cls, Cursor cursor, int columnIndex) {
		return cursor.getFloat(columnIndex);
	}

	@Override
	public Object parseTypeArr(Class<?> arrValType, String[] arr) {
		ArrayList<Float> list = toTypeColl(Float.class, arr);
		Float[] tArr = list.toArray(new Float[list.size()]);
		return (arrValType == float.class) ? toPrimitive(tArr) : tArr;
	}

}
