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
	protected <V> Float parseFromString(Class<Float> valType,
			Class<V> arrCollItemType, String str) {
		return Float.valueOf(str);
	}

	@Override
	public <V> void putToContentValues(Class<Float> valueType,
			Class<V> arrCollItemType, ContentValues cv, String key, Float val) {
		cv.put(key, val);
	}

	@Override
	public <V> Float readFromCursor(Class<Float> valType,
			Class<V> arrCollItemType, Cursor cursor, int columnIndex) {
		return cursor.getFloat(columnIndex);
	}

	@Override
	public Object parseTypeArr(Class<Float> valType, String[] arr) {
		ArrayList<Float> list = parseTypeColl(valType, arr);
		Float[] tArr = list.toArray(new Float[list.size()]);
		return (valType == float.class) ? toPrimitive(tArr) : tArr;
	}

}
