/**
 * Copyright 2014 Alex Yanchenko
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

import static org.droidparts.inner.ReflectionUtils.getFieldVal;
import static org.droidparts.inner.ReflectionUtils.newInstance;
import static org.droidparts.inner.ReflectionUtils.setFieldVal;
import static org.json.JSONObject.NULL;

import java.util.ArrayList;
import java.util.Collection;

import org.droidparts.Injector;
import org.droidparts.annotation.json.Key;
import org.droidparts.inner.ClassSpecRegistry;
import org.droidparts.inner.ConverterRegistry;
import org.droidparts.inner.PersistUtils;
import org.droidparts.inner.ann.FieldSpec;
import org.droidparts.inner.ann.json.KeyAnn;
import org.droidparts.inner.converter.Converter;
import org.droidparts.model.Model;
import org.droidparts.util.L;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

public class JSONSerializer<ModelType extends Model> {

	private final Class<ModelType> cls;
	private Context ctx;

	public JSONSerializer(Class<ModelType> cls, Context ctx) {
		this.cls = cls;
		if (ctx != null) {
			this.ctx = ctx.getApplicationContext();
			Injector.inject(ctx, this);
		}
	}

	protected Context getContext() {
		return ctx;
	}

	public JSONObject serialize(ModelType item) throws JSONException {
		JSONObject obj = new JSONObject();
		FieldSpec<KeyAnn>[] keySpecs = ClassSpecRegistry.getJsonKeySpecs(cls);
		for (FieldSpec<KeyAnn> spec : keySpecs) {
			readFromModelAndPutToJSON(item, spec, obj, spec.ann.name);
		}
		return obj;
	}

	public ModelType deserialize(JSONObject obj) throws JSONException {
		ModelType model = newInstance(cls);
		FieldSpec<KeyAnn>[] keySpecs = ClassSpecRegistry.getJsonKeySpecs(cls);
		for (FieldSpec<KeyAnn> spec : keySpecs) {
			readFromJSONAndSetFieldVal(model, spec, obj, spec.ann.name);
		}
		return model;
	}

	public JSONArray serialize(Collection<ModelType> items)
			throws JSONException {
		JSONArray arr = new JSONArray();
		for (ModelType item : items) {
			arr.put(serialize(item));
		}
		return arr;
	}

	public ArrayList<ModelType> deserialize(JSONArray arr) throws JSONException {
		ArrayList<ModelType> list = new ArrayList<ModelType>();
		for (int i = 0; i < arr.length(); i++) {
			list.add(deserialize(arr.getJSONObject(i)));
		}
		return list;
	}

	protected final boolean hasNonNull(JSONObject obj, String key)
			throws JSONException {
		return PersistUtils.hasNonNull(obj, key);
	}

	private void readFromModelAndPutToJSON(ModelType item,
			FieldSpec<KeyAnn> spec, JSONObject obj, String key)
			throws JSONException {
		Pair<String, String> keyParts = getNestedKeyParts(key);
		if (keyParts != null) {
			String subKey = keyParts.first;
			JSONObject subObj;
			if (hasNonNull(obj, subKey)) {
				subObj = obj.getJSONObject(subKey);
			} else {
				subObj = new JSONObject();
				obj.put(subKey, subObj);
			}
			readFromModelAndPutToJSON(item, spec, subObj, keyParts.second);
		} else {
			Object columnVal = getFieldVal(item, spec.field);
			try {
				putToJSONObject(obj, key, spec.field.getType(),
						spec.componentType, columnVal);
			} catch (Exception e) {
				if (spec.ann.optional) {
					L.w("Failded to serialize %s.%s: %s.", cls.getSimpleName(),
							spec.field.getName(), e.getMessage());
				} else {
					throw new JSONException(Log.getStackTraceString(e));
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <T> void putToJSONObject(JSONObject obj, String key,
			Class<T> valType, Class<?> componentType, Object val)
			throws Exception {
		if (val == null) {
			obj.put(key, NULL);
		} else {
			Converter<T> converter = ConverterRegistry.getConverter(valType);
			converter.putToJSON(valType, componentType, obj, key, (T) val);
		}
	}

	private void readFromJSONAndSetFieldVal(ModelType model,
			FieldSpec<KeyAnn> spec, JSONObject obj, String key)
			throws JSONException {
		Pair<String, String> keyParts = getNestedKeyParts(key);
		if (keyParts != null) {
			String subKey = keyParts.first;
			if (hasNonNull(obj, subKey)) {
				JSONObject subObj = obj.getJSONObject(subKey);
				readFromJSONAndSetFieldVal(model, spec, subObj, keyParts.second);
			} else {
				throwIfRequired(spec);
			}
		} else if (obj.has(key)) {
			try {
				Object val = readFromJSON(spec.field.getType(),
						spec.componentType, obj, key);
				if (!NULL.equals(val)) {
					setFieldVal(model, spec.field, val);
				} else {
					L.i("Received NULL '%s', skipping.", spec.ann.name);
				}
			} catch (Exception e) {
				if (spec.ann.optional) {
					L.w("Failed to deserialize '%s': %s.", spec.ann.name,
							e.getMessage());
				} else {
					throw new JSONException(Log.getStackTraceString(e));
				}
			}
		} else {
			throwIfRequired(spec);
		}
	}

	private <T, V> Object readFromJSON(Class<T> valType,
			Class<V> componentType, JSONObject obj, String key)
			throws Exception {
		Object jsonVal = obj.get(key);
		if (NULL.equals(jsonVal)) {
			return jsonVal;
		} else {
			Converter<T> converter = ConverterRegistry.getConverter(valType);
			return converter.readFromJSON(valType, componentType, obj, key);
		}
	}

	private Pair<String, String> getNestedKeyParts(String key) {
		int firstSep = key.indexOf(Key.SUB);
		if (firstSep != -1) {
			String subKey = key.substring(0, firstSep);
			String leftKey = key.substring(firstSep + Key.SUB.length());
			Pair<String, String> pair = Pair.create(subKey, leftKey);
			return pair;
		} else {
			return null;
		}
	}

	private void throwIfRequired(FieldSpec<KeyAnn> spec) throws JSONException {
		if (!spec.ann.optional) {
			throw new JSONException("Required key '" + spec.ann.name
					+ "' not present.");
		}
	}

}