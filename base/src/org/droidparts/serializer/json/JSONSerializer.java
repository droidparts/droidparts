/**
 * Copyright 2012 Alex Yanchenko
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
package org.droidparts.serializer.json;

import static org.droidparts.reflection.util.ReflectionUtils.getField;
import static org.droidparts.reflection.util.ReflectionUtils.getTypedFieldVal;
import static org.droidparts.reflection.util.ReflectionUtils.instantiate;
import static org.droidparts.reflection.util.ReflectionUtils.setFieldVal;
import static org.droidparts.reflection.util.TypeHelper.getArrayType;
import static org.droidparts.reflection.util.TypeHelper.isArray;
import static org.droidparts.reflection.util.TypeHelper.isBoolean;
import static org.droidparts.reflection.util.TypeHelper.isByte;
import static org.droidparts.reflection.util.TypeHelper.isByteArray;
import static org.droidparts.reflection.util.TypeHelper.isCharacter;
import static org.droidparts.reflection.util.TypeHelper.isCollection;
import static org.droidparts.reflection.util.TypeHelper.isDouble;
import static org.droidparts.reflection.util.TypeHelper.isEnum;
import static org.droidparts.reflection.util.TypeHelper.isFloat;
import static org.droidparts.reflection.util.TypeHelper.isInteger;
import static org.droidparts.reflection.util.TypeHelper.isLong;
import static org.droidparts.reflection.util.TypeHelper.isModel;
import static org.droidparts.reflection.util.TypeHelper.isShort;
import static org.droidparts.reflection.util.TypeHelper.isString;
import static org.droidparts.reflection.util.TypeHelper.isUUID;
import static org.droidparts.reflection.util.TypeHelper.toTypeArr;
import static org.json.JSONObject.NULL;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import org.droidparts.model.Model;
import org.droidparts.reflection.model.JSONModelField;
import org.droidparts.reflection.processor.JSONModelAnnotationProcessor;
import org.droidparts.reflection.util.ReflectionUtils;
import org.droidparts.serializer.Serializer;
import org.droidparts.util.L;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.util.Pair;

public class JSONSerializer<TypeFrom extends Model> implements
		Serializer<TypeFrom, JSONObject> {

	// ASCII GS (group separator), '->' for readability
	public static final String __ = "->" + (char) 29;

	private final Class<? extends Model> cls;
	private final JSONModelAnnotationProcessor processor;

	public JSONSerializer(Class<? extends Model> cls) {
		this.cls = cls;
		this.processor = new JSONModelAnnotationProcessor(cls);
	}

	@Override
	public String getModelClassName() {
		return processor.getModelClassName();
	}

	public final JSONArray serializeList(Collection<TypeFrom> coll)
			throws JSONException {
		JSONArray arr = new JSONArray();
		for (TypeFrom item : coll) {
			arr.put(serialize(item));
		}
		return arr;
	}

	public final ArrayList<TypeFrom> deserializeList(JSONArray arr)
			throws JSONException {
		ArrayList<TypeFrom> list = new ArrayList<TypeFrom>();
		for (int i = 0; i < arr.length(); i++) {
			list.add(deserialize(arr.getJSONObject(i)));
		}
		return list;
	}

	@Override
	public JSONObject serialize(TypeFrom item) throws JSONException {
		JSONObject obj = new JSONObject();
		JSONModelField[] fields = processor.getModelClassFields();
		for (JSONModelField jsonField : fields) {
			readFromModelAndPutToJSON(item, jsonField, obj, jsonField.keyName);
		}
		return obj;
	}

	@Override
	public TypeFrom deserialize(JSONObject obj) throws JSONException {
		TypeFrom model = instantiate(cls);
		JSONModelField[] fields = processor.getModelClassFields();
		for (JSONModelField jsonField : fields) {
			readFromJSONAndSetFieldVal(model, jsonField, obj, jsonField.keyName);
		}
		return model;
	}

	private void readFromModelAndPutToJSON(TypeFrom item,
			JSONModelField jsonField, JSONObject obj, String key)
			throws JSONException {
		Pair<String, String> keyParts = getNestedKeyParts(key);
		if (keyParts != null) {
			String subKey = keyParts.first;
			JSONObject subObj;
			if (gotNonNull(obj, subKey)) {
				subObj = obj.getJSONObject(subKey);
			} else {
				subObj = new JSONObject();
				obj.put(subKey, subObj);
			}
			readFromModelAndPutToJSON(item, jsonField, subObj, keyParts.second);
		} else {
			Field f = getField(item.getClass(), jsonField.fieldName);
			Object columnVal = getTypedFieldVal(f, item);
			try {
				putToJSONObject(obj, key, jsonField.fieldClass, columnVal);
			} catch (Exception e) {
				if (jsonField.keyRequired) {
					throw new JSONException(Log.getStackTraceString(e));
				} else {
					L.e("Failded to serialize " + processor.getModelClassName()
							+ "." + jsonField.fieldName);
					L.w(e);
				}
			}
		}
	}

	private void readFromJSONAndSetFieldVal(TypeFrom model,
			JSONModelField modelField, JSONObject obj, String key)
			throws JSONException {
		Pair<String, String> keyParts = getNestedKeyParts(key);
		if (keyParts != null) {
			String subKey = keyParts.first;
			if (gotNonNull(obj, subKey)) {
				JSONObject subObj = obj.getJSONObject(subKey);
				readFromJSONAndSetFieldVal(model, modelField, subObj,
						keyParts.second);
			} else {
				throwIfRequired(modelField);
			}
		} else if (obj.has(key)) {
			Object val = obj.get(key);
			Field f = getField(model.getClass(), modelField.fieldName);
			try {
				val = readFromJSON(modelField.fieldClass,
						modelField.fieldGenericArg, val);
				if (!NULL.equals(val)) {
					setFieldVal(f, model, val);
				} else {
					L.i("Received NULL '" + modelField.keyName + "', skipping.");
				}
			} catch (Exception e) {
				if (modelField.keyRequired) {
					throw new JSONException(Log.getStackTraceString(e));
				} else {
					L.e("Failed to deserialize '" + modelField.keyName + "'.");
					L.w(e);
				}
			}
		} else {
			throwIfRequired(modelField);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void putToJSONObject(JSONObject obj, String key,
			Class<?> valType, Object val) throws Exception {
		if (val == null) {
			obj.put(key, NULL);
		} else if (isByte(valType)) {
			obj.put(key, (Byte) val);
		} else if (isShort(valType)) {
			obj.put(key, (Short) val);
		} else if (isInteger(valType)) {
			obj.put(key, (Integer) val);
		} else if (isLong(valType)) {
			obj.put(key, (Long) val);
		} else if (isFloat(valType)) {
			obj.put(key, (Float) val);
		} else if (isDouble(valType)) {
			obj.put(key, (Double) val);
		} else if (isBoolean(valType)) {
			obj.put(key, (Boolean) val);
		} else if (isCharacter(valType)) {
			obj.put(key, (Character) val);
		} else if (isString(valType)) {
			obj.put(key, (String) val);
		} else if (isEnum(valType)) {
			obj.put(key, val.toString());
		} else if (isUUID(valType)) {
			obj.put(key, val.toString());
		} else if (isByteArray(valType)) {
			obj.put(key, val);
		} else if (isArray(valType) || isCollection(valType)) {
			ArrayList<Object> list = new ArrayList<Object>();
			if (isArray(valType)) {
				Object[] arr = (Object[]) val;
				list.addAll(Arrays.asList(arr));
			} else if (isCollection(valType)) {
				Collection<Object> coll = (Collection<Object>) val;
				list.addAll(coll);
			}
			JSONArray jarr = new JSONArray();
			JSONSerializer serializer = null;
			if (list.size() > 1) {
				serializer = getSerializer(list.get(0).getClass());
			}
			for (Object o : list) {
				jarr.put(serializer.serialize((Model) o));
			}
			obj.put(key, jarr);
		} else if (isModel(valType)) {
			JSONObject obj2 = getSerializer(valType).serialize((TypeFrom) val);
			obj.put(key, obj2);
		} else {
			throw new IllegalArgumentException("Unsupported class: " + valType);
		}
	}

	@SuppressWarnings("rawtypes")
	protected Object readFromJSON(Class<?> valType, Class<?> valGenericArgType,
			Object jsonVal) throws Exception {
		String strVal = String.valueOf(jsonVal);
		if (NULL.equals(jsonVal)) {
			return jsonVal;
		} else if (isByte(valType)) {
			return Byte.valueOf(strVal);
		} else if (isShort(valType)) {
			return Short.valueOf(strVal);
		} else if (isInteger(valType)) {
			return Integer.valueOf(strVal);
		} else if (isLong(valType)) {
			return Long.valueOf(strVal);
		} else if (isFloat(valType)) {
			return Float.valueOf(strVal);
		} else if (isDouble(valType)) {
			return Double.valueOf(strVal);
		} else if (isBoolean(valType)) {
			if ("0".equals(strVal) || "false".equalsIgnoreCase(strVal)) {
				return Boolean.FALSE;
			} else if ("1".equals(strVal) || "true".equalsIgnoreCase(strVal)) {
				return Boolean.TRUE;
			} else {
				throw new IllegalArgumentException("Unparseable boolean: '"
						+ strVal + "'.");
			}
		} else if (isCharacter(valType)) {
			return (strVal.length() == 0) ? ' ' : strVal.charAt(0);
		} else if (isString(valType)) {
			return strVal;
		} else if (isEnum(valType)) {
			return ReflectionUtils.instantiateEnum(valType, (String) jsonVal);
		} else if (isUUID(valType)) {
			return UUID.fromString((String) jsonVal);
		} else if (isByteArray(valType)) {
			return jsonVal;
		} else if (isArray(valType) || isCollection(valType)) {
			boolean isArr = isArray(valType);
			boolean isColl = isCollection(valType);
			JSONArray jArr = (JSONArray) jsonVal;
			Object[] arr = new Object[jArr.length()];
			Collection<Object> coll = null;
			Class<?> itemCls;
			if (isColl) {
				coll = instantiate(valType);
				itemCls = valGenericArgType;
			} else {
				itemCls = getArrayType(valType);
			}
			JSONSerializer serializer = getSerializer(itemCls);
			for (int i = 0; i < jArr.length(); i++) {
				Object obj = jArr.get(i);
				if (obj instanceof JSONObject) {
					obj = serializer.deserialize((JSONObject) obj);
				}
				if (isArr) {
					arr[i] = String.valueOf(obj);
				} else {
					coll.add(obj);
				}
			}
			if (isArr) {
				// XXX
				String[] arr2 = new String[arr.length];
				for (int i = 0; i < arr.length; i++) {
					arr2[i] = String.valueOf(arr[i]);
				}
				return toTypeArr(valType, arr2);
			} else {
				return coll;
			}

		} else if (isModel(valType)) {
			return getSerializer(valType).deserialize((JSONObject) jsonVal);
		} else {
			throw new IllegalArgumentException("Unsupported class: " + valType);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private JSONSerializer getSerializer(Class<?> cls) {
		JSONSerializer serializer = new JSONSerializer(cls);
		return serializer;
	}

	private boolean gotNonNull(JSONObject obj, String key) throws JSONException {
		return obj.has(key) && !NULL.equals(obj.get(key));
	}

	private Pair<String, String> getNestedKeyParts(String key) {
		int firstSep = key.indexOf(__);
		if (firstSep != -1) {
			String subKey = key.substring(0, firstSep);
			String leftKey = key.substring(firstSep + __.length());
			Pair<String, String> pair = Pair.create(subKey, leftKey);
			return pair;
		} else {
			return null;
		}
	}

	private void throwIfRequired(JSONModelField modelField)
			throws JSONException {
		if (modelField.keyRequired) {
			throw new JSONException("Required key '" + modelField.keyName
					+ "' not present.");
		}
	}

}
