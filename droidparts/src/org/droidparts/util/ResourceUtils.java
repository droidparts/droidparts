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
package org.droidparts.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import android.content.Context;
import android.util.TypedValue;

public final class ResourceUtils {

	public static int dpToPx(Context ctx, int val) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				val, ctx.getResources().getDisplayMetrics());
	}

	public static String valueForKey(Context ctx, int keysArrId,
			int valuesArrId, String key) {
		String[] keysArr = ctx.getResources().getStringArray(keysArrId);
		String[] valuesArr = ctx.getResources().getStringArray(valuesArrId);
		int idx = Arrays.asList(keysArr).indexOf(key);
		return (idx != -1) ? valuesArr[idx] : null;
	}

	public static String readRawResource(Context ctx, int resId)
			throws IllegalArgumentException {
		InputStream is = null;
		try {
			is = ctx.getResources().openRawResource(resId);
			return IOUtils.readToString(is);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
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
