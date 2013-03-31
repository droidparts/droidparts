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

import static org.droidparts.reflect.util.ReflectionUtils.instantiateEnum;

import java.lang.reflect.Array;
import java.util.ArrayList;

import org.droidparts.reflect.util.TypeHelper;

import android.content.ContentValues;
import android.database.Cursor;

public class EnumHandler extends TypeHandler<Enum<?>> {

	@Override
	public boolean canHandle(Class<?> cls) {
		return TypeHelper.isEnum(cls);
	}

	@Override
	public String getDBColumnType() {
		return TEXT;
	}

	@Override
	public Object convertForJSON(Enum<?> val) {
		return val.toString();
	}

	@Override
	protected Enum<?> parseFromString(Class<Enum<?>> cls, String str) {
		return instantiateEnum(cls, str);
	}

	@Override
	public void putToContentValues(ContentValues cv, String key, Enum<?> val) {
		cv.put(key, val.toString());
	}

	@Override
	public Enum<?> readFromCursor(Class<Enum<?>> cls, Cursor cursor,
			int columnIndex) {
		return instantiateEnum(cls, cursor.getString(columnIndex));
	}

	@Override
	public Object parseTypeArr(Class<Enum<?>> arrValType, String[] arr) {
		ArrayList<? extends Enum<?>> list = (ArrayList<? extends Enum<?>>) parseTypeColl(
				arrValType, arr);
		Object enumArr = Array.newInstance(arrValType, list.size());
		for (int i = 0; i < list.size(); i++) {
			Array.set(enumArr, i, list.get(i));
		}
		return enumArr;
	}

}
