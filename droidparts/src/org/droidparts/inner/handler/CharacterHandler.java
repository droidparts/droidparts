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
package org.droidparts.inner.handler;

import static org.droidparts.util.Arrays2.toPrimitive;

import java.util.ArrayList;

import org.droidparts.inner.TypeHelper;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;

public class CharacterHandler extends TypeHandler<Character> {

	@Override
	public boolean canHandle(Class<?> cls) {
		return TypeHelper.isCharacter(cls);
	}

	@Override
	public String getDBColumnType() {
		return INTEGER;
	}

	@Override
	public <V> Character readFromJSON(Class<Character> valType,
			Class<V> arrCollItemType, JSONObject obj, String key)
			throws JSONException {
		return parseFromString(valType, arrCollItemType, obj.getString(key));
	}

	@Override
	protected <V> Character parseFromString(Class<Character> valType,
			Class<V> arrCollItemType, String str) {
		return Character.valueOf((str.length() == 0) ? ' ' : str.charAt(0));
	}

	@Override
	public <V> void putToContentValues(Class<Character> valueType,
			Class<V> arrCollItemType, ContentValues cv, String key,
			Character val) {
		cv.put(key, String.valueOf(val));
	}

	@Override
	public <V> Character readFromCursor(Class<Character> valType,
			Class<V> arrCollItemType, Cursor cursor, int columnIndex) {
		return parseFromString(valType, null, cursor.getString(columnIndex));
	}

	@Override
	public Object parseTypeArr(Class<Character> valType, String[] arr) {
		ArrayList<Character> list = parseTypeColl(valType, arr);
		Character[] tArr = list.toArray(new Character[list.size()]);
		return (valType == char.class) ? toPrimitive(tArr) : tArr;
	}

}
