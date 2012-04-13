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
package org.droidparts.inject.injector;

import static org.droidparts.reflection.util.ReflectionUtils.setFieldVal;
import static org.droidparts.reflection.util.TypeHelper.isString;

import java.lang.reflect.Field;

import org.droidparts.annotation.inject.InjectResource;
import org.droidparts.util.L;

import android.content.Context;
import android.content.res.Resources;

public class ResourceInjector {

	static boolean inject(Context ctx, InjectResource ann, Object target,
			Field field) {
		int resId = ann.value();
		if (resId != 0) {
			Resources res = ctx.getResources();
			Class<?> cls = field.getType();
			Object val = null;
			if (isString(cls)) {
				val = res.getString(resId);
			}
			// TODO more resource types
			if (val != null && cls.isAssignableFrom(val.getClass())) {
				setFieldVal(field, target, val);
				return true;
			} else {
				L.e("Null or incompatible: " + val);
				return false;
			}
		} else {
			L.e("0 resource id.");
			return false;
		}
	}

}
