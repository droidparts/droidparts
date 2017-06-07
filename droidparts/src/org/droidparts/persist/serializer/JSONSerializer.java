/**
 * Copyright 2017 Alex Yanchenko
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
package org.droidparts.persist.serializer;

import java.util.ArrayList;
import java.util.Collection;

import android.content.Context;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.droidparts.inner.ClassSpecRegistry;
import org.droidparts.inner.ConverterRegistry;
import org.droidparts.inner.PersistUtils;
import org.droidparts.inner.ann.FieldSpec;
import org.droidparts.inner.ann.serialize.JSONAnn;
import org.droidparts.inner.converter.Converter;
import org.droidparts.model.Model;
import org.droidparts.util.L;

import static org.json.JSONObject.NULL;

import static org.droidparts.inner.ReflectionUtils.getFieldVal;
import static org.droidparts.inner.ReflectionUtils.newInstance;
import static org.droidparts.inner.ReflectionUtils.setFieldVal;

public class JSONSerializer<ModelType extends Model> extends AbstractSerializer<ModelType, JSONObject, JSONArray> {

	public JSONSerializer(Class<ModelType> cls, Context ctx) {
		super(cls, ctx);
	}

	public JSONObject serialize(ModelType item) throws Exception {
		JSONObject obj = new JSONObject();
		FieldSpec<JSONAnn>[] jsonSpecs = ClassSpecRegistry.getJSONSpecs(cls);
		for (FieldSpec<JSONAnn> spec : jsonSpecs) {
			readFromModelAndPutToJSON(item, spec, obj, spec.ann.key);
		}
		return obj;
	}

	@Override
	public ModelType deserialize(JSONObject obj) throws Exception {
		ModelType model = newInstance(cls);
		FieldSpec<JSONAnn>[] jsonSpecs = ClassSpecRegistry.getJSONSpecs(cls);
		for (FieldSpec<JSONAnn> spec : jsonSpecs) {
			readFromJSONAndSetFieldVal(model, spec, obj, spec.ann.key);
		}
		return model;
	}

	public JSONArray serializeAll(Collection<ModelType> items) throws Exception {
		JSONArray arr = new JSONArray();
		for (ModelType item : items) {
			arr.put(serialize(item));
		}
		return arr;
	}

	@Override
	public ArrayList<ModelType> deserializeAll(JSONArray arr) throws Exception {
		ArrayList<ModelType> list = new ArrayList<ModelType>();
		for (int i = 0; i < arr.length(); i++) {
			list.add(deserialize(arr.getJSONObject(i)));
		}
		return list;
	}

	//

	protected final boolean hasNonNull(JSONObject obj, String key) throws JSONException {
		return PersistUtils.hasNonNull(obj, key);
	}

	//

	private void readFromModelAndPutToJSON(ModelType item, FieldSpec<JSONAnn> spec, JSONObject obj, String key)
			throws Exception {
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
			Object val = getFieldVal(item, spec.field);
			putToJSON(obj, key, spec.ann.optional, spec.field.getType(), spec.genericArg1, spec.genericArg2, val);
		}
	}

	private void readFromJSONAndSetFieldVal(ModelType model, FieldSpec<JSONAnn> spec, JSONObject obj, String key)
			throws Exception {
		Pair<String, String> keyParts = getNestedKeyParts(key);
		if (keyParts != null) {
			JSONObject subObj = null;
			try {
				subObj = (JSONObject) readFromJSON(JSONObject.class, null, null, obj, keyParts.first);
			} catch (Exception e) {
				handleParseException(obj, spec.ann.optional, keyParts.first, e);
				return;
			}
			readFromJSONAndSetFieldVal(model, spec, subObj, keyParts.second);
		} else {
			try {
				Object val = readFromJSON(spec.field.getType(), spec.genericArg1, spec.genericArg2, obj, key);
				if (!NULL.equals(val)) {
					setFieldVal(model, spec.field, val);
				} else {
					L.d("Received NULL '%s', skipping.", spec.ann.key);
				}
			} catch (Exception e) {
				handleParseException(obj, spec.ann.optional, key, e);
			}
		}
	}

	protected <T, G1, G2> Object readFromJSON(Class<T> valType, Class<G1> genericArg1, Class<G2> genericArg2,
	                                          JSONObject obj, String key) throws Exception {
		Object jsonVal = obj.get(key);
		if (NULL.equals(jsonVal)) {
			return jsonVal;
		} else {
			Converter<T> converter = ConverterRegistry.getConverter(valType);
			return converter.readFromJSON(valType, genericArg1, genericArg2, obj, key);
		}
	}

	@SuppressWarnings("unchecked")
	protected <T> void putToJSON(JSONObject obj, String key, boolean optional, Class<T> valType, Class<?> genericArg1,
	                             Class<?> genericArg2, Object val) throws Exception {
		if (val == null) {
			if (!optional) {
				obj.put(key, NULL);
			}
		} else {
			Converter<T> converter = ConverterRegistry.getConverter(valType);
			converter.putToJSON(valType, genericArg1, genericArg2, obj, key, (T) val);
		}
	}

	private static void handleParseException(JSONObject obj, boolean optional, String key, Exception e)
			throws SerializerException {
		logOrThrow(obj, optional, String.format("key '%s'", key), e);
	}

}
