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

import static org.droidparts.reflect.util.ReflectionUtils.instantiate;
import static org.droidparts.reflect.util.TypeHelper.isArray;
import static org.droidparts.reflect.util.TypeHelper.isDate;
import static org.droidparts.reflect.util.TypeHelper.isEnum;
import static org.droidparts.reflect.util.TypeHelper.isModel;
import static org.droidparts.reflect.util.TypeHelper.isUUID;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.droidparts.model.Model;
import org.droidparts.persist.json.JSONSerializer;
import org.droidparts.reflect.util.TypeHandlerRegistry;
import org.droidparts.reflect.util.TypeHelper;
import org.droidparts.util.Arrays2;
import org.droidparts.util.Strings;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;

public class ArrayCollectionHandler extends TypeHandler<Object> {

	// ASCII RS (record separator), '|' for readability
	private static final String SEP = "|" + (char) 30;

	@Override
	public boolean canHandle(Class<?> cls) {
		return TypeHelper.isArray(cls) || TypeHelper.isCollection(cls);
	}

	@Override
	public String getDBColumnType() {
		return TEXT;
	}

	@Override
	public <V> Object convertForJSON(Class<Object> valType,
			Class<V> arrCollItemType, Object val) {
		final ArrayList<Object> list = new ArrayList<Object>();
		if (isArray(valType)) {
			list.addAll(Arrays.asList(Arrays2.toObjectArr(val)));
		} else {
			list.addAll((Collection<?>) val);
		}
		JSONArray jArr = new JSONArray();
		if (isModel(arrCollItemType)) {
			JSONSerializer serializer = new JSONSerializer(arrCollItemType,
					null);
			try {
				jArr = serializer.serialize(list);
			} catch (JSONException e) {
				throw new IllegalArgumentException(e);
			}
		} else {
			// XXX
			boolean isDate = isDate(arrCollItemType);
			boolean toString = isUUID(arrCollItemType)
					|| isEnum(arrCollItemType);
			for (Object o : list) {
				if (isDate) {
					o = ((Date) o).getTime();
				} else if (toString) {
					o = o.toString();
				}
				jArr.put(o);
			}
		}
		return jArr;
	}

	@Override
	public <V> Object convertFromJSON(Class<Object> valType,
			Class<V> arrCollItemType, Object val) {
		// TODO
		return super.convertFromJSON(valType, arrCollItemType, val);
	}

	@Override
	protected <V> Object parseFromString(Class<Object> valType,
			Class<V> arrCollItemType, String str) {
		JSONArray jArr;
		try {
			jArr = new JSONArray(str);
		} catch (JSONException e) {
			throw new IllegalArgumentException(e);
		}
		boolean isArr = isArray(valType);
		Object[] arr = null;
		Collection<Object> coll = null;
		if (isArr) {
			arr = new Object[jArr.length()];
		} else {
			@SuppressWarnings("unchecked")
			Class<? extends Collection<Object>> cl = (Class<? extends Collection<Object>>) valType;
			coll = instantiate(cl);
		}
		boolean isModel = isModel(arrCollItemType);
		JSONSerializer<Model> serializer = null;
		if (isModel) {
			serializer = subSerializer(arrCollItemType);
		}
		for (int i = 0; i < jArr.length(); i++) {
			Object obj1;
			try {
				obj1 = jArr.get(i);
				if (isModel) {
					obj1 = serializer.deserialize((JSONObject) obj1);
				}
			} catch (JSONException e) {
				throw new IllegalArgumentException(e);
			}
			if (isArr) {
				arr[i] = obj1;
			} else {
				coll.add(obj1);
			}
		}
		if (isArr) {
			if (isModel) {
				Object modelArr = Array
						.newInstance(arrCollItemType, arr.length);
				for (int i = 0; i < arr.length; i++) {
					Array.set(modelArr, i, arr[i]);
				}
				return modelArr;
			} else {
				String[] arr2 = new String[arr.length];
				for (int i = 0; i < arr.length; i++) {
					arr2[i] = arr[i].toString();
				}
				TypeHandler<V> arrItemHandler = TypeHandlerRegistry
						.get(arrCollItemType);
				if (arrItemHandler == null) {
					throw new IllegalArgumentException("Unable to convert to "
							+ arrCollItemType + ".");
				}
				return arrItemHandler.parseTypeArr(arrCollItemType, arr2);
			}
		} else {
			return coll;
		}
	}

	@Override
	public <V> void putToContentValues(Class<Object> valueType,
			Class<V> arrCollItemType, ContentValues cv, String key, Object val)
			throws IllegalArgumentException {
		final ArrayList<Object> list = new ArrayList<Object>();
		if (isArray(valueType)) {
			list.addAll(Arrays.asList(Arrays2.toObjectArr(val)));
		} else {
			list.addAll((Collection<?>) val);
		}
		if (isDate(arrCollItemType)) {
			for (int i = 0; i < list.size(); i++) {
				Long timestamp = ((Date) list.get(i)).getTime();
				list.set(i, timestamp);
			}
		}
		String strVal = Strings.join(list, SEP, null);
		cv.put(key, strVal);
	}

	@Override
	public <V> Object readFromCursor(Class<Object> valType,
			Class<V> arrCollItemType, Cursor cursor, int columnIndex)
			throws IllegalArgumentException {
		TypeHandler<V> arrItemHandler = TypeHandlerRegistry
				.get(arrCollItemType);
		if (arrItemHandler == null) {
			throw new IllegalArgumentException("Unable to convert to "
					+ arrCollItemType + ".");
		}
		String str = cursor.getString(columnIndex);
		String[] parts = (str.length() > 0) ? str.split("\\" + SEP)
				: new String[0];
		if (isArray(valType)) {
			return arrItemHandler.parseTypeArr(arrCollItemType, parts);
		} else {
			@SuppressWarnings("unchecked")
			Collection<Object> coll = (Collection<Object>) instantiate(valType);
			coll.addAll(arrItemHandler.parseTypeColl(arrCollItemType, parts));
			return coll;
		}
	}

	@Override
	public Object parseTypeArr(Class<Object> valType, String[] arr) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	private JSONSerializer<Model> subSerializer(Class<?> cls) {
		return new JSONSerializer<Model>((Class<Model>) cls, null);
	}

}
