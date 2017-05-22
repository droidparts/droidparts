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
package org.droidparts.inner.converter;

import android.content.ContentValues;
import android.database.Cursor;

import org.json.JSONObject;
import org.w3c.dom.Node;

import org.droidparts.inner.PersistUtils;
import org.droidparts.inner.TypeHelper;
import org.droidparts.model.Model;
import org.droidparts.persist.serializer.JSONSerializer;
import org.droidparts.persist.serializer.XMLSerializer;

@SuppressWarnings("unchecked")
public class ModelConverter<M extends Model> extends Converter<M> {

	@Override
	public boolean canHandle(Class<?> cls) {
		return TypeHelper.isModel(cls) && !TypeHelper.isEntity(cls);
	}

	@Override
	public String getDBColumnType() {
		return BLOB;
	}

	@Override
	public <G1, G2> void putToContentValues(Class<M> valueType, Class<G1> genericArg1, Class<G2> genericArg2,
	                                        ContentValues cv, String key, M val) throws Exception {
		byte[] arr = PersistUtils.toBytes(val);
		cv.put(key, arr);
	}

	@Override
	public <G1, G2> M readFromCursor(Class<M> valType, Class<G1> genericArg1, Class<G2> genericArg2, Cursor cursor,
	                                 int columnIndex) throws Exception {
		M model = null;
		byte[] arr = cursor.getBlob(columnIndex);
		if (arr != null) {
			model = (M) PersistUtils.fromBytes(arr);
		}
		return model;
	}

	@Override
	public <G1, G2> void putToJSON(Class<M> valType, Class<G1> genericArg1, Class<G2> genericArg2, JSONObject obj,
	                               String key, M val) throws Exception {
		Class<M> cls = (Class<M>) val.getClass();
		JSONObject valStr = ((JSONSerializer<M>) getJSONSerializer(cls, null)).serialize(val);
		obj.put(key, valStr);
	}

	@Override
	public <G1, G2> M readFromJSON(Class<M> valType, Class<G1> genericArg1, Class<G2> genericArg2, JSONObject obj,
	                               String key) throws Exception {
		JSONObject src = obj.getJSONObject(key);
		return getJSONSerializer(valType, src).deserialize(src);
	}

	@Override
	public <G1, G2> M readFromXML(Class<M> valType, Class<G1> genericArg1, Class<G2> genericArg2, Node node,
	                              String nodeListItemTagHint) throws Exception {
		return getXMLSerializer(valType, node).deserialize(node);
	}

	@Override
	protected <G1, G2> M parseFromString(Class<M> valType, Class<G1> genericArg1, Class<G2> genericArg2, String str)
			throws Exception {
		JSONObject src = new JSONObject(str);
		return getJSONSerializer(valType, src).deserialize(src);
	}

	protected JSONSerializer<? extends M> getJSONSerializer(Class<M> valType, JSONObject src) throws Exception {
		return new JSONSerializer<M>(valType, null);
	}

	protected XMLSerializer<? extends M> getXMLSerializer(Class<M> valType, Node src) throws Exception {
		return new XMLSerializer<M>(valType, null);
	}

}
