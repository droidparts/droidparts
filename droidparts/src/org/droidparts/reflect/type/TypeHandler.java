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

import org.droidparts.contract.SQL;

import android.content.ContentValues;
import android.database.Cursor;

public abstract class TypeHandler<T> implements SQL.DDL {

	public abstract boolean canHandle(Class<?> cls);

	public abstract String getDBColumnType();

	public Object convertForJSON(T val) {
		return val;
	}

	@SuppressWarnings("unchecked")
	public T convertFromJSON(Class<T> cls, Object val) {
		if (cls.isAssignableFrom(val.getClass())) {
			return (T) val;
		} else {
			return parseFromString(cls, val.toString());
		}
	}

	protected abstract T parseFromString(Class<T> cls, String str);

	public abstract void putToContentValues(ContentValues cv, String key,
			Object val) throws IllegalArgumentException;

	public abstract T readFromCursor(Class<T> cls, Cursor cursor,
			int columnIndex) throws IllegalArgumentException;

	// say hello to arrays of primitives
	public abstract Object parseTypeArr(Class<T> arrValType, String[] arr);

	public ArrayList<T> parseTypeColl(Class<T> valCls, String[] arr)
			throws IllegalArgumentException {
		ArrayList<T> list = new ArrayList<T>();
		for (String str : arr) {
			try {
				list.add(parseFromString(valCls, str));
			} catch (Exception e) {
				throw new IllegalArgumentException("Unable to convert '" + str
						+ "' to " + valCls.getSimpleName() + ".");
			}
		}
		return list;
	}

}
