/**
 * Copyright 2012 Alex Yanchenko
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
package org.droidparts.util;

import java.util.Arrays;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.preference.Preference;

public class MiscUtils {

	public static void merge(JSONObject source, JSONObject target,
			boolean overwrite) throws JSONException {
		@SuppressWarnings("unchecked")
		Iterator<String> it = source.keys();
		while (it.hasNext()) {
			String key = it.next();
			if (!target.has(key) || overwrite) {
				target.put(key, source.get(key));
			}
		}
	}

	public static void setListPreferenceSummary(Context ctx, int valsArrId,
			int namesArrId, Preference preference, Object newValue) {
		String[] valsArr = ctx.getResources().getStringArray(valsArrId);
		String[] namesArr = ctx.getResources().getStringArray(namesArrId);
		int idx = Arrays.asList(valsArr).indexOf(newValue);
		String summary = (idx != -1) ? namesArr[idx] : "...";
		preference.setSummary(summary);
	}

	private MiscUtils() {
	}

}
