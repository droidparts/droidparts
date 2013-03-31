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
package org.droidparts.type.handler;

import static org.droidparts.util.Arrays2.toPrimitive;

import java.util.ArrayList;

import org.droidparts.type.TypeHelper;

import android.content.ContentValues;
import android.database.Cursor;

public class LongHandler extends AbstractTypeHandler<Long> {

	@Override
	public boolean canHandle(Class<?> cls) {
		return TypeHelper.isLong(cls);
	}

	@Override
	public String getDBColumnType() {
		return INTEGER;
	}

	@Override
	protected <V> Long parseFromString(Class<Long> valType,
			Class<V> arrCollItemType, String str) {
		return Long.valueOf(str);
	}

	@Override
	public <V> void putToContentValues(Class<Long> valueType,
			Class<V> arrCollItemType, ContentValues cv, String key, Long val) {
		cv.put(key, val);
	}

	@Override
	public <V> Long readFromCursor(Class<Long> valType,
			Class<V> arrCollItemType, Cursor cursor, int columnIndex) {
		return cursor.getLong(columnIndex);
	}

	@Override
	public Object parseTypeArr(Class<Long> valType, String[] arr) {
		ArrayList<Long> list = parseTypeColl(valType, arr);
		Long[] tArr = list.toArray(new Long[list.size()]);
		return (valType == long.class) ? toPrimitive(tArr) : tArr;
	}

}
