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

import java.lang.reflect.Field;

import org.droidparts.annotation.inject.InjectBundleExtra;

import android.content.Context;
import android.os.Bundle;

public class BundleExtraInjector {

	public static boolean inject(Context ctx, Bundle data,
			InjectBundleExtra ann, Object target, Field field) {
		String key = ann.value();
		boolean optional = ann.optional();
		Object val = data.get(key);
		if (val == null && !optional) {
			throw new IllegalArgumentException("Bundle contains no key: " + key);
		}
		if (val != null) {
			setFieldVal(field, target, val);
			return true;
		} else {
			return false;
		}
	}

}
