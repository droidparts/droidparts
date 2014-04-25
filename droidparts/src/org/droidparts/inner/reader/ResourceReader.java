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
package org.droidparts.inner.reader;

import static org.droidparts.inner.TypeHelper.isArray;
import static org.droidparts.inner.TypeHelper.isBoolean;
import static org.droidparts.inner.TypeHelper.isDrawable;
import static org.droidparts.inner.TypeHelper.isInteger;
import static org.droidparts.inner.TypeHelper.isString;
import android.content.Context;
import android.content.res.Resources;

public class ResourceReader {

	static Object readVal(Context ctx, int resId, Class<?> valType)
			throws Exception {
		Resources res = ctx.getResources();
		Object val = null;
		if (isBoolean(valType, true)) {
			val = res.getBoolean(resId);
		} else if (isInteger(valType, true)) {
			val = res.getInteger(resId);
		} else if (isString(valType)) {
			val = res.getString(resId);
		} else if (isDrawable(valType)) {
			val = res.getDrawable(resId);
		} else if (isArray(valType)) {
			Class<?> type = valType.getComponentType();
			if (isInteger(type, false)) {
				val = res.getIntArray(resId);
			} else if (isString(type)) {
				val = res.getStringArray(resId);
			}
		}
		if (val == null) {
			throw new Exception("Unsupported resource type '"
					+ valType.getName() + "'.");
		} else {
			return val;
		}
	}
}
