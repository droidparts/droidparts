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

import java.util.UUID;

import org.droidparts.inner.TypeHelper;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;

public class UUIDConverter extends Converter<UUID> {

	@Override
	public boolean canHandle(Class<?> cls) {
		return TypeHelper.isUUID(cls);
	}

	@Override
	public String getDBColumnType() {
		return TEXT;
	}

	@Override
	public <V> void putToJSON(Class<UUID> valType, Class<V> componentType,
			JSONObject obj, String key, UUID val) throws JSONException {
		obj.put(key, val.toString());
	}

	@Override
	public <V> UUID readFromJSON(Class<UUID> valType, Class<V> componentType,
			JSONObject obj, String key) throws JSONException {
		return parseFromString(valType, componentType, obj.getString(key));
	}

	@Override
	protected <V> UUID parseFromString(Class<UUID> valType,
			Class<V> componentType, String str) {
		return UUID.fromString(str);
	}

	@Override
	public <V> void putToContentValues(Class<UUID> valueType,
			Class<V> componentType, ContentValues cv, String key, UUID val) {
		cv.put(key, val.toString());
	}

	@Override
	public <V> UUID readFromCursor(Class<UUID> valType, Class<V> componentType,
			Cursor cursor, int columnIndex) {
		return UUID.fromString(cursor.getString(columnIndex));
	}

}
