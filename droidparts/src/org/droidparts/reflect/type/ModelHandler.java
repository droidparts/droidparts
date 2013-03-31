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
package org.droidparts.reflect.type;

import org.droidparts.model.Model;
import org.droidparts.persist.json.JSONSerializer;
import org.droidparts.reflect.util.TypeHelper;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;

public class ModelHandler extends TypeHandler<Model> {

	@Override
	public boolean canHandle(Class<?> cls) {
		return TypeHelper.isModel(cls) && !TypeHelper.isEntity(cls);
	}

	@Override
	public String getDBColumnType() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void putToContentValues(ContentValues cv, String key, Model val)
			throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Model readFromCursor(Class<Model> cls, Cursor cursor, int columnIndex)
			throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object convertForJSON(Model val) {
		@SuppressWarnings("unchecked")
		Class<Model> cls = (Class<Model>) val.getClass();
		try {
			return new JSONSerializer<Model>(cls, null).serialize(val);
		} catch (JSONException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public Model convertFromJSON(Class<Model> cls, Object val) {
		if (val instanceof JSONObject) {
			try {
				return new JSONSerializer<Model>(cls, null)
						.deserialize((JSONObject) val);
			} catch (JSONException e) {
				throw new IllegalArgumentException(e);
			}
		} else {
			return super.convertFromJSON(cls, val);
		}
	}

	@Override
	protected Model parseFromString(Class<Model> cls, String str) {
		try {
			return convertFromJSON(cls, new JSONObject(str));
		} catch (JSONException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public Object parseTypeArr(Class<Model> arrValType, String[] arr) {
		throw new UnsupportedOperationException();
	}

}
