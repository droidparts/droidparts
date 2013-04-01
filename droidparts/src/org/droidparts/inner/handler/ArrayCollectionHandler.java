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

import static org.droidparts.inner.ReflectionUtils.newInstance;
import static org.droidparts.inner.TypeHelper.isArray;
import static org.droidparts.inner.TypeHelper.isModel;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.droidparts.inner.TypeHandlerRegistry;
import org.droidparts.inner.TypeHelper;
import org.droidparts.model.Model;
import org.droidparts.persist.json.JSONSerializer;
import org.droidparts.util.Arrays2;
import org.droidparts.util.Strings;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;

public class ArrayCollectionHandler extends AbstractTypeHandler<Object> {

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
	public <V> Object readFromJSON(Class<Object> valType,
			Class<V> arrCollItemType, JSONObject obj, String key)
			throws JSONException {
		return parseFromString(valType, arrCollItemType, obj.getString(key));
	}

	@Override
	public <V> Object convertForJSON(Class<Object> valType,
			Class<V> arrCollItemType, Object val) {
		AbstractTypeHandler<V> handler = TypeHandlerRegistry
				.getHandlerOrThrow(arrCollItemType);
		ArrayList<V> list = arrOrCollToList(valType, arrCollItemType, val);
		JSONArray vals = new JSONArray();
		for (V obj : list) {
			Object jObj = handler.convertForJSON(arrCollItemType, null, obj);
			vals.put(jObj);
		}
		return vals;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <V> Object parseFromString(Class<Object> valType,
			Class<V> arrCollItemType, String str) {
		// XXX
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
			coll = (Collection<Object>) newInstance(valType);
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
				AbstractTypeHandler<V> handler = TypeHandlerRegistry
						.getHandlerOrThrow(arrCollItemType);
				return handler.parseTypeArr(arrCollItemType, arr2);
			}
		} else {
			return coll;
		}
	}

	@Override
	public <V> void putToContentValues(Class<Object> valueType,
			Class<V> arrCollItemType, ContentValues cv, String key, Object val)
			throws IllegalArgumentException {
		AbstractTypeHandler<V> handler = TypeHandlerRegistry
				.getHandlerOrThrow(arrCollItemType);
		ArrayList<V> list = arrOrCollToList(valueType, arrCollItemType, val);
		ArrayList<Object> vals = new ArrayList<Object>();
		for (V obj : list) {
			Object jObj = handler.convertForJSON(arrCollItemType, null, obj);
			vals.add(jObj);
		}
		String strVal = Strings.join(vals, SEP, null);
		cv.put(key, strVal);
	}

	@Override
	public <V> Object readFromCursor(Class<Object> valType,
			Class<V> arrCollItemType, Cursor cursor, int columnIndex)
			throws IllegalArgumentException {
		AbstractTypeHandler<V> handler = TypeHandlerRegistry
				.getHandlerOrThrow(arrCollItemType);
		String str = cursor.getString(columnIndex);
		String[] parts = (str.length() > 0) ? str.split("\\" + SEP)
				: new String[0];
		if (isArray(valType)) {
			return handler.parseTypeArr(arrCollItemType, parts);
		} else {
			@SuppressWarnings("unchecked")
			Collection<Object> coll = (Collection<Object>) newInstance(valType);
			coll.addAll(handler.parseTypeColl(arrCollItemType, parts));
			return coll;
		}
	}

	@Override
	public Object parseTypeArr(Class<Object> valType, String[] arr) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	private <T> ArrayList<T> arrOrCollToList(Class<?> valType,
			Class<T> arrCollItemType, Object val) {
		ArrayList<T> list = new ArrayList<T>();
		if (isArray(valType)) {
			list.addAll((List<T>) Arrays.asList(Arrays2.toObjectArr(val)));
		} else {
			list.addAll((Collection<T>) val);
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	private JSONSerializer<Model> subSerializer(Class<?> cls) {
		return new JSONSerializer<Model>((Class<Model>) cls, null);
	}

}
