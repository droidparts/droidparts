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
package org.droidparts.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.TypedValue;

public final class ResourceUtils {

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

	public static void dumpDBToCacheDir(Context ctx, SQLiteDatabase db) {
		String dbFilePath = db.getPath();
		String dbFileName = dbFilePath.substring(dbFilePath.lastIndexOf('/',
				dbFilePath.length()));
		File fileTo = new File(ctx.getExternalCacheDir(), dbFileName);
		try {
			IOUtils.copy(new File(dbFilePath), fileTo);
			L.i("Copied DB file to '%s'.", fileTo.getAbsolutePath());
		} catch (IOException e) {
			L.w(e);
		}
	}

	public static String valueForKey(Context ctx, int keysArrId,
			int valuesArrId, String key) {
		String[] keysArr = ctx.getResources().getStringArray(keysArrId);
		String[] valuesArr = ctx.getResources().getStringArray(valuesArrId);
		int idx = Arrays.asList(keysArr).indexOf(key);
		return (idx != -1) ? valuesArr[idx] : null;
	}

	public static int dpToPx(Context ctx, int val) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				val, ctx.getResources().getDisplayMetrics());
	}

	public static String readStringResource(Context ctx, int resId)
			throws IOException {
		InputStream is = null;
		try {
			is = ctx.getResources().openRawResource(resId);
			return IOUtils.readToString(is);
		} finally {
			IOUtils.silentlyClose(is);
		}
	}

	public static int getResourceId(Context ctx, String resourceName) {
		return getId(ctx, "id", resourceName);
	}

	public static int getStringId(Context ctx, String stringName) {
		return getId(ctx, "string", stringName);
	}

	private static int getId(Context ctx, String type, String name) {
		return ctx.getResources().getIdentifier(name, type,
				ctx.getPackageName());
	}

}
