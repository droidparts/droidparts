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

import java.lang.reflect.Field;

import org.droidparts.annotation.inject.InjectView;
import org.droidparts.reflection.util.ReflectionUtils;
import org.droidparts.util.L;
import org.droidparts.util.inner.ResourceUtils;

import android.content.Context;
import android.view.View;

public class InjectViewInjector {

	public static boolean inject(Context ctx, View root, InjectView ann,
			Object target, Field field) {
		int viewId = ann.value();
		if (viewId == 0) {
			String fieldName = field.getName();
			viewId = ResourceUtils.getResourceId(ctx, fieldName);
		}
		if (viewId != 0) {
			View view = root.findViewById(viewId);
			if (field.getType() != view.getClass()) {
				// TODO
				L.e("Incompatible types.");
			}
			ReflectionUtils.setFieldVal(field, target, view);
			return true;
		} else {
			L.e("View not found.");
			return false;
		}
	}
}
