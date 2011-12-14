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
package org.droidparts.manager.json;

import static org.droidparts.reflection.util.ReflectionUtils.getField;
import static org.droidparts.reflection.util.ReflectionUtils.getTypedFieldVal;
import static org.droidparts.reflection.util.ReflectionUtils.instantiate;
import static org.droidparts.reflection.util.ReflectionUtils.setFieldVal;
import static org.droidparts.reflection.util.TypeHelper.isBoolean;
import static org.droidparts.reflection.util.TypeHelper.isDouble;
import static org.droidparts.reflection.util.TypeHelper.isEnum;
import static org.droidparts.reflection.util.TypeHelper.isFloat;
import static org.droidparts.reflection.util.TypeHelper.isInteger;
import static org.droidparts.reflection.util.TypeHelper.isLong;
import static org.droidparts.reflection.util.TypeHelper.isModel;
import static org.droidparts.reflection.util.TypeHelper.isString;
import static org.droidparts.reflection.util.TypeHelper.isUUID;

import java.lang.reflect.Field;
import java.util.UUID;

import org.droidparts.model.Model;
import org.droidparts.reflection.model.JSONField;
import org.droidparts.reflection.processor.JSONAnnotationProcessor;
import org.droidparts.reflection.util.ReflectionUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONSerializer<T extends Model> {

	private final Class<? extends Model> cls;
	private final JSONAnnotationProcessor processor;

	public JSONSerializer(Class<? extends Model> cls) {
		this.cls = cls;
		this.processor = new JSONAnnotationProcessor(cls);
	}

	public JSONObject toJson(T item) throws JSONException {
		JSONObject obj = new JSONObject();
		JSONField[] fields = processor.getFields();
		for (JSONField jsonField : fields) {
			Field f = getField(item.getClass(), jsonField.fieldName);
			Object columnVal = getTypedFieldVal(f, item);
			putToJSONObject(obj, jsonField.keyName, jsonField.fieldType, columnVal);
		}
		return obj;
	}

	public T fromJson(JSONObject obj) throws JSONException {
		T model = instantiate(cls);
		JSONField[] fields = processor.getFields();
		for (JSONField jsonField : fields) {
			if (obj.has(jsonField.keyName)) {
				Object val = obj.get(jsonField.keyName);
				Field f = getField(model.getClass(), jsonField.fieldName);
				val = readFromJSON(jsonField.fieldType, model, val);
				setFieldVal(f, model, val);
			}
		}
		return model;
	}

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
		} else if (isModel(valueCls)) {
			@SuppressWarnings("unchecked")
			JSONObject obj2 = getManager(valueCls).toJson((T) value);
			obj.put(key, obj2);
		} else {
			throw new IllegalArgumentException("Unsupported class: " + valueCls);
		}
	}

	private Object readFromJSON(Class<?> fieldCls, Object model, Object val)
			throws JSONException {
		if (isUUID(fieldCls)) {
			return UUID.fromString((String) val);
		} else if (isEnum(fieldCls)) {
			return ReflectionUtils.instantiateEnum(fieldCls, (String) val);
		} else if (isModel(fieldCls)) {
			return getManager(fieldCls).fromJson((JSONObject) val);
		} else {
			return val;
		}
	}

	private JSONSerializer<T> getManager(Class<?> cls) {
		@SuppressWarnings("unchecked")
		JSONSerializer<T> manager = new JSONSerializer<T>(
				(Class<? extends Model>) cls);
		return manager;
	}

}
