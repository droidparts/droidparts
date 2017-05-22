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

import org.droidparts.contract.SQL;
import org.droidparts.inner.PersistUtils;

public abstract class Converter<T> implements SQL.DDL {

	public abstract boolean canHandle(Class<?> cls);

	public <G1, G2> void putToJSON(Class<T> valType, Class<G1> genericArg1, Class<G2> genericArg2, JSONObject obj,
	                               String key, T val) throws Exception {
		obj.put(key, val);
	}

	public <G1, G2> T readFromJSON(Class<T> valType, Class<G1> genericArg1, Class<G2> genericArg2, JSONObject obj,
	                               String key) throws Exception {
		return parseFromString(valType, genericArg1, genericArg2, obj.getString(key));
	}

	public <G1, G2> T readFromXML(Class<T> valType, Class<G1> genericArg1, Class<G2> genericArg2, Node node,
	                              String nodeListItemTagHint) throws Exception {
		return parseFromString(valType, genericArg1, genericArg2, PersistUtils.getNodeText(node));
	}

	protected <G1, G2> T parseFromString(Class<T> valType, Class<G1> genericArg1, Class<G2> genericArg2, String str)
			throws Exception {
		throw new UnsupportedOperationException();
	}

	public String getDBColumnType() {
		throw new UnsupportedOperationException();
	}

	public <G1, G2> void putToContentValues(Class<T> valueType, Class<G1> genericArg1, Class<G2> genericArg2,
	                                        ContentValues cv, String key, T val) throws Exception {
		throw new UnsupportedOperationException();
	}

	public <G1, G2> T readFromCursor(Class<T> valType, Class<G1> genericArg1, Class<G2> genericArg2, Cursor cursor,
	                                 int columnIndex) throws Exception {
		throw new UnsupportedOperationException();
	}

}
