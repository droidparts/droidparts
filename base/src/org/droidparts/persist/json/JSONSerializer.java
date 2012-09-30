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
package org.droidparts.persist.json;

import static org.droidparts.reflect.util.ReflectionUtils.getFieldVal;
import static org.droidparts.reflect.util.ReflectionUtils.instantiate;
import static org.droidparts.reflect.util.ReflectionUtils.setFieldVal;
import static org.droidparts.reflect.util.TypeHelper.isArray;
import static org.droidparts.reflect.util.TypeHelper.isBoolean;
import static org.droidparts.reflect.util.TypeHelper.isByte;
import static org.droidparts.reflect.util.TypeHelper.isByteArray;
import static org.droidparts.reflect.util.TypeHelper.isCharacter;
import static org.droidparts.reflect.util.TypeHelper.isCollection;
import static org.droidparts.reflect.util.TypeHelper.isDate;
import static org.droidparts.reflect.util.TypeHelper.isDouble;
import static org.droidparts.reflect.util.TypeHelper.isEnum;
import static org.droidparts.reflect.util.TypeHelper.isFloat;
import static org.droidparts.reflect.util.TypeHelper.isInteger;
import static org.droidparts.reflect.util.TypeHelper.isLong;
import static org.droidparts.reflect.util.TypeHelper.isModel;
import static org.droidparts.reflect.util.TypeHelper.isShort;
import static org.droidparts.reflect.util.TypeHelper.isString;
import static org.droidparts.reflect.util.TypeHelper.isUUID;
import static org.droidparts.reflect.util.TypeHelper.parseValue;
import static org.droidparts.reflect.util.TypeHelper.toTypeArr;
import static org.json.JSONObject.NULL;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.droidparts.model.Model;
import org.droidparts.reflect.model.json.ModelSpec;
import org.droidparts.reflect.processor.ModelAnnotationProcessor;
import org.droidparts.util.L;
import org.droidparts.util.PersistUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.util.Pair;

public class JSONSerializer<ModelType extends Model> {

	// ASCII GS (group separator), '->' for readability
	public static final String __ = "->" + (char) 29;

	public static <ModelType extends Model> JSONSerializer<ModelType> getInstance(
			Class<ModelType> cls) {
		return new JSONSerializer<ModelType>(cls);
	}

	private final Class<? extends Model> cls;
	private final ModelAnnotationProcessor processor;

	public JSONSerializer(Class<ModelType> cls) {
		this.cls = cls;
		this.processor = new ModelAnnotationProcessor(cls);
	}

	public JSONObject serialize(ModelType item) throws JSONException {
		JSONObject obj = new JSONObject();
		ModelSpec[] fields = processor.getModelClassFields();
		for (ModelSpec jsonField : fields) {
			readFromModelAndPutToJSON(item, jsonField, obj, jsonField.key.name);
		}
		return obj;
	}

	public ModelType deserialize(JSONObject obj) throws JSONException {
		ModelType model = instantiate(cls);
		ModelSpec[] fields = processor.getModelClassFields();
		for (ModelSpec jsonField : fields) {
			readFromJSONAndSetFieldVal(model, jsonField, obj,
					jsonField.key.name);
		}
		return model;
	}

	public final JSONArray serialize(Collection<ModelType> items)
			throws JSONException {
		JSONArray arr = new JSONArray();
		for (ModelType item : items) {
			arr.put(serialize(item));
		}
		return arr;
	}

	public final ArrayList<ModelType> deserialize(JSONArray arr)
			throws JSONException {
		ArrayList<ModelType> list = new ArrayList<ModelType>();
		for (int i = 0; i < arr.length(); i++) {
			list.add(deserialize(arr.getJSONObject(i)));
		}
		return list;
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
		} else if (isDate(valType)) {
			obj.put(key, ((Date) val).getTime());
		} else if (isByteArray(valType)) {
			obj.put(key, val);
		} else if (isModel(valType)) {
			JSONObject obj2 = getInstance(dirtyCast(valType)).serialize(
					(Model) val);
			obj.put(key, obj2);
		} else if (isArray(valType) || isCollection(valType)) {
			ArrayList<Object> list = new ArrayList<Object>();
			if (isArray(valType)) {
				Object[] arr = (Object[]) val;
				list.addAll(Arrays.asList(arr));
			} else if (isCollection(valType)) {
				Collection<Object> coll = (Collection<Object>) val;
				list.addAll(coll);
			}
			JSONArray jArr = new JSONArray();
			if (list.size() > 0) {
				Class<?> itemCls = list.get(0).getClass();
				if (isModel(itemCls)) {
					JSONSerializer serializer = getInstance(dirtyCast(itemCls));
					jArr = serializer.serialize(list);
				} else {
					for (Object o : list) {
						jArr.put(o);
					}
				}
			}
			obj.put(key, jArr);
		} else {
			throw new IllegalArgumentException("Unsupported class: " + valType);
		}
	}

	@SuppressWarnings("rawtypes")
	protected Object readFromJSON(Field field, Class<?> multiFieldArgType,
			Object jsonVal) throws Exception {
		Class<?> fieldType = field.getType();
		String strVal = String.valueOf(jsonVal);
		if (NULL.equals(jsonVal)) {
			return jsonVal;
		} else if (isBoolean(fieldType)) {
			if ("1".equals(strVal)) {
				strVal = "true";
			}
		}

		Object parsedVal = parseValue(fieldType, strVal);
		if (parsedVal != null) {
			return parsedVal;
		}

		if (isByteArray(fieldType)) {
			return jsonVal;
		} else if (isModel(fieldType)) {
			return getInstance(dirtyCast(fieldType)).deserialize(
					(JSONObject) jsonVal);
		} else if (isArray(fieldType) || isCollection(fieldType)) {
			JSONArray jArr = (JSONArray) jsonVal;
			boolean isArr = isArray(fieldType);
			Object[] arr = null;
			Collection<Object> coll = null;
			if (isArr) {
				arr = new Object[jArr.length()];
			} else {
				coll = instantiate(fieldType);
			}
			JSONSerializer serializer = null;
			if (isModel(multiFieldArgType)) {
				serializer = getInstance(dirtyCast(multiFieldArgType));
			}
			for (int i = 0; i < jArr.length(); i++) {
				Object obj = jArr.get(i);
				if (serializer != null) {
					obj = serializer.deserialize((JSONObject) obj);
				}
				if (isArr) {
					arr[i] = obj;
				} else {
					coll.add(obj);
				}
			}
			if (isArr) {
				return toTypeArr(multiFieldArgType, arr);
			} else {
				return coll;
			}
		} else {
			throw new IllegalArgumentException("Unsupported class: "
					+ fieldType);
		}
	}

	protected boolean gotNonNull(JSONObject obj, String key)
			throws JSONException {
		return PersistUtils.gotNonNull(obj, key);
	}

	private void readFromModelAndPutToJSON(ModelType item, ModelSpec jsonField,
			JSONObject obj, String key) throws JSONException {
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
			Object columnVal = getFieldVal(item, jsonField.field);
			try {
				putToJSONObject(obj, key, jsonField.field.getType(), columnVal);
			} catch (Exception e) {
				if (jsonField.key.optional) {
					L.w("Failded to serialize " + processor.getModelClassName()
							+ "." + jsonField.field.getName() + ": "
							+ e.getMessage());
				} else {
					throw new JSONException(Log.getStackTraceString(e));
				}
			}
		}
	}

	private void readFromJSONAndSetFieldVal(ModelType model,
			ModelSpec modelField, JSONObject obj, String key)
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
			try {
				val = readFromJSON(modelField.field,
						modelField.multiFieldArgType, val);
				if (!NULL.equals(val)) {
					setFieldVal(model, modelField.field, val);
				} else {
					L.i("Received NULL '" + modelField.key.name
							+ "', skipping.");
				}
			} catch (Exception e) {
				if (modelField.key.optional) {
					L.w("Failed to deserialize '" + modelField.key.name + "': "
							+ e.getMessage());
				} else {
					throw new JSONException(Log.getStackTraceString(e));
				}
			}
		} else {
			throwIfRequired(modelField);
		}
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

	private Class<Model> dirtyCast(Class<?> cls) {
		@SuppressWarnings("unchecked")
		Class<Model> cls2 = (Class<Model>) cls;
		return cls2;
	}

	private void throwIfRequired(ModelSpec modelField) throws JSONException {
		if (!modelField.key.optional) {
			throw new JSONException("Required key '" + modelField.key.name
					+ "' not present.");
		}
	}

}