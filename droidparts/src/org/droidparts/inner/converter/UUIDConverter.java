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

import java.util.UUID;

import android.content.ContentValues;
import android.database.Cursor;

import org.json.JSONObject;

import org.droidparts.inner.TypeHelper;

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
	public <G1, G2> void putToJSON(Class<UUID> valType, Class<G1> genericArg1, Class<G2> genericArg2, JSONObject obj,
	                               String key, UUID val) throws Exception {
		obj.put(key, val.toString());
	}

	@Override
	protected <G1, G2> UUID parseFromString(Class<UUID> valType, Class<G1> genericArg1, Class<G2> genericArg2,
	                                        String str) {
		return UUID.fromString(str);
	}

	@Override
	public <G1, G2> void putToContentValues(Class<UUID> valueType, Class<G1> genericArg1, Class<G2> genericArg2,
	                                        ContentValues cv, String key, UUID val) {
		cv.put(key, val.toString());
	}

	@Override
	public <G1, G2> UUID readFromCursor(Class<UUID> valType, Class<G1> genericArg1, Class<G2> genericArg2,
	                                    Cursor cursor, int columnIndex) {
		return UUID.fromString(cursor.getString(columnIndex));
	}

}
