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
package org.droidparts.inner.converter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
	public <V> void putToContentValues(Class<Model> valueType,
			Class<V> componentType, ContentValues cv, String key, Model val)
			throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(val);
		byte[] arr = baos.toByteArray();
		cv.put(key, arr);
	}

	@Override
	public <V> Model readFromCursor(Class<Model> valType,
			Class<V> componentType, Cursor cursor, int columnIndex)
			throws Exception {
		Model model = null;
		byte[] arr = cursor.getBlob(columnIndex);
		if (arr != null) {
			ByteArrayInputStream bais = new ByteArrayInputStream(arr);
			ObjectInputStream ois = new ObjectInputStream(bais);
			model = (Model) ois.readObject();
		}
		return model;
	}

	@Override
	public <V> void putToJSON(Class<Model> valType, Class<V> componentType,
			JSONObject obj, String key, Model val) throws Exception {
		@SuppressWarnings("unchecked")
		Class<Model> cls = (Class<Model>) val.getClass();
		JSONObject valStr = new JSONSerializer<Model>(cls, null).serialize(val);
		obj.put(key, valStr);
	}

	@Override
	public <V> Model readFromJSON(Class<Model> valType, Class<V> componentType,
			JSONObject obj, String key) throws Exception {
		return new JSONSerializer<Model>(valType, null).deserialize(obj
				.getJSONObject(key));
	}

	@Override
	public <V> Model readFromXML(Class<Model> valType, Class<V> componentType,
			Node node, String nodeListItemTagHint) throws Exception {
		return new XMLSerializer<Model>(valType, null).deserialize(node);
	}

	@Override
	protected <V> Model parseFromString(Class<Model> valType,
			Class<V> componentType, String str) throws Exception {
		return new JSONSerializer<Model>(valType, null)
				.deserialize(new JSONObject(str));
	}

}
