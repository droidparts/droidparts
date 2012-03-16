/**
 * Copyright 2011 Alex Yanchenko
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
import static org.droidparts.reflection.util.TypeHelper.isArray;
import static org.droidparts.reflection.util.TypeHelper.isBoolean;
import static org.droidparts.reflection.util.TypeHelper.isCollection;
import static org.droidparts.reflection.util.TypeHelper.isDouble;
import static org.droidparts.reflection.util.TypeHelper.isEnum;
import static org.droidparts.reflection.util.TypeHelper.isFloat;
import static org.droidparts.reflection.util.TypeHelper.isInteger;
import static org.droidparts.reflection.util.TypeHelper.isLong;
import static org.droidparts.reflection.util.TypeHelper.isModel;
import static org.droidparts.reflection.util.TypeHelper.isString;
import static org.droidparts.reflection.util.TypeHelper.isUUID;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONSerializer<TypeFrom extends Model> implements
		Serializer<TypeFrom, JSONObject> {

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

	@Override
	public JSONObject serialize(TypeFrom item) throws JSONException {
		JSONObject obj = new JSONObject();
		JSONModelField[] fields = processor.getModelClassFields();
		for (JSONModelField jsonField : fields) {
			Field f = getField(item.getClass(), jsonField.fieldName);
			Object columnVal = getTypedFieldVal(f, item);
			putToJSONObject(obj, jsonField.keyName, jsonField.fieldClass,
					columnVal);
		}
		return obj;
	}

	@Override
	public TypeFrom deserialize(JSONObject obj) throws JSONException {
		TypeFrom model = instantiate(cls);
		JSONModelField[] fields = processor.getModelClassFields();
		for (JSONModelField jsonField : fields) {
			if (obj.has(jsonField.keyName)) {
				Object val = obj.get(jsonField.keyName);
				Field f = getField(model.getClass(), jsonField.fieldName);
				val = readFromJSON(jsonField.fieldClass,
						jsonField.fieldClassGenericArgs, model, val);
				setFieldVal(f, model, val);
			}
		}
		return model;
	}

	public final JSONArray serialize(Collection<TypeFrom> coll)
			throws JSONException {
		JSONArray arr = new JSONArray();
		for (TypeFrom item : coll) {
			arr.put(serialize(item));
		}
		return arr;
	}

	public final ArrayList<TypeFrom> deserialize(JSONArray arr)
			throws JSONException {
		ArrayList<TypeFrom> list = new ArrayList<TypeFrom>();
		for (int i = 0; i < arr.length(); i++) {
			list.add(deserialize(arr.getJSONObject(i)));
		}
		return list;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void putToJSONObject(JSONObject obj, String key, Class<?> valueCls,
			Object value) throws JSONException {
		if (isBoolean(valueCls)) {
			obj.put(key, ((Boolean) value));
		} else if (isDouble(valueCls)) {
			obj.put(key, (Double) value);
		} else if (isFloat(valueCls)) {
			obj.put(key, (Float) value);
		} else if (isInteger(valueCls)) {
			obj.put(key, (Integer) value);
		} else if (isLong(valueCls)) {
			obj.put(key, (Long) value);
		} else if (isString(valueCls)) {
			obj.put(key, (String) value);
		} else if (isUUID(valueCls)) {
			obj.put(key, value.toString());
		} else if (isEnum(valueCls)) {
			obj.put(key, value.toString());
		} else if (isArray(valueCls) || isCollection(valueCls)) {
			ArrayList<Object> list = new ArrayList<Object>();
			if (isArray(valueCls)) {
				Object[] arr = (Object[]) value;
				list.addAll(Arrays.asList(arr));
			} else if (isCollection(valueCls)) {
				Collection<Object> coll = (Collection<Object>) value;
				list.addAll(coll);
			}
			JSONArray jarr = new JSONArray();
			JSONSerializer serializer = null;
			if (list.size() > 1) {
				serializer = getSerializer(list.get(0).getClass());
			}
			for (Object o : list) {
				try {
					jarr.put(serializer.serialize(o));
				} catch (Exception e) {
					throw new JSONException(e.getMessage());
				}
			}
			obj.put(key, jarr);
		} else if (isCollection(valueCls)) {
		} else if (isModel(valueCls)) {
			JSONObject obj2 = getSerializer(valueCls).serialize(
					(TypeFrom) value);
			obj.put(key, obj2);
		} else {
			throw new IllegalArgumentException("Unsupported class: " + valueCls);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object readFromJSON(Class<?> fieldCls,
			Class<?>[] fieldClassGenericArguments, Object model, Object val)
			throws JSONException {
		if (isUUID(fieldCls)) {
			return UUID.fromString((String) val);
		} else if (isEnum(fieldCls)) {
			return ReflectionUtils.instantiateEnum(fieldCls, (String) val);
		} else if (isModel(fieldCls)) {
			return getSerializer(fieldCls).deserialize((JSONObject) val);
		} else if (isArray(fieldCls) || isCollection(fieldCls)) {
			boolean isArr = isArray(fieldCls);
			boolean isColl = isCollection(fieldCls);
			JSONArray jArr = (JSONArray) val;
			Object[] arr = new Object[jArr.length()];
			Collection<Object> coll = null;
			if (isColl) {
				coll = instantiate(fieldCls);
			}
			JSONSerializer serializer = getSerializer(fieldClassGenericArguments[0]);
			for (int i = 0; i < jArr.length(); i++) {
				try {
					Object obj = serializer.deserialize(jArr.get(i));
					if (isArr) {
						arr[i] = obj;
					} else {
						coll.add(obj);
					}
				} catch (Exception e) {
					throw new JSONException(e.getMessage());
				}
			}
			if (isArr) {
				return arr;
			} else {
				return coll;
			}
		} else {
			return val;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private JSONSerializer getSerializer(Class<?> cls) {
		JSONSerializer serializer = new JSONSerializer(cls);
		return serializer;
	}

}
