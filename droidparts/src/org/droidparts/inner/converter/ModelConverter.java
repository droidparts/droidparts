/**
 * Copyright 2015 Alex Yanchenko
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

import org.droidparts.inner.PersistUtils;
import org.droidparts.inner.TypeHelper;
import org.droidparts.model.Model;
import org.droidparts.persist.serializer.JSONSerializer;
import org.droidparts.persist.serializer.XMLSerializer;
import org.json.JSONObject;
import org.w3c.dom.Node;

import android.content.ContentValues;
import android.database.Cursor;

public class ModelConverter extends Converter<Model> {

	@Override
	public boolean canHandle(Class<?> cls) {
		return TypeHelper.isModel(cls) && !TypeHelper.isEntity(cls);
	}

	@Override
	public String getDBColumnType() {
		return BLOB;
	}

	@Override
	public <G1, G2> void putToContentValues(Class<Model> valueType, Class<G1> genericArg1, Class<G2> genericArg2,
			ContentValues cv, String key, Model val) throws Exception {
		byte[] arr = PersistUtils.toBytes(val);
		cv.put(key, arr);
	}

	@Override
	public <G1, G2> Model readFromCursor(Class<Model> valType, Class<G1> genericArg1, Class<G2> genericArg2,
			Cursor cursor, int columnIndex) throws Exception {
		Model model = null;
		byte[] arr = cursor.getBlob(columnIndex);
		if (arr != null) {
			model = (Model) PersistUtils.fromBytes(arr);
		}
		return model;
	}

	@Override
	public <G1, G2> void putToJSON(Class<Model> valType, Class<G1> genericArg1, Class<G2> genericArg2, JSONObject obj,
			String key, Model val) throws Exception {
		@SuppressWarnings("unchecked")
		Class<Model> cls = (Class<Model>) val.getClass();
		JSONObject valStr = new JSONSerializer<Model>(cls, null).serialize(val);
		obj.put(key, valStr);
	}

	@Override
	public <G1, G2> Model readFromJSON(Class<Model> valType, Class<G1> genericArg1, Class<G2> genericArg2,
			JSONObject obj, String key) throws Exception {
		return new JSONSerializer<Model>(valType, null).deserialize(obj.getJSONObject(key));
	}

	@Override
	public <G1, G2> Model readFromXML(Class<Model> valType, Class<G1> genericArg1, Class<G2> genericArg2, Node node,
			String nodeListItemTagHint) throws Exception {
		return new XMLSerializer<Model>(valType, null).deserialize(node);
	}

	@Override
	protected <G1, G2> Model parseFromString(Class<Model> valType, Class<G1> genericArg1, Class<G2> genericArg2,
			String str) throws Exception {
		return new JSONSerializer<Model>(valType, null).deserialize(new JSONObject(str));
	}

}
