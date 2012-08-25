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

import static org.droidparts.reflect.util.ReflectionUtils.setFieldVal;

import java.lang.reflect.Field;

import org.droidparts.annotation.inject.InjectView;
import org.droidparts.util.inner.ResourceUtils;

import android.content.Context;
import android.preference.PreferenceActivity;
import android.view.View;

public class ViewOrPreferenceInjector {

	static boolean inject(Context ctx, View root, InjectView ann,
			Object target, Field field) {
		boolean isView = View.class.isAssignableFrom(field.getType());
		int viewOrPrefId = ann.value();
		if (viewOrPrefId == 0) {
			String fieldName = field.getName();
			if (isView) {
				viewOrPrefId = ResourceUtils.getResourceId(ctx, fieldName);
			} else {
				viewOrPrefId = ResourceUtils.getStringId(ctx, fieldName);
			}
		}
		if (viewOrPrefId != 0) {
			Object val;
			if (isView) {
				val = root.findViewById(viewOrPrefId);
			} else {
				// XXX
				val = ((PreferenceActivity) ctx).findPreference(ctx
						.getText(viewOrPrefId));
			}
			try {
				setFieldVal(field, target, val);
				return true;
			} catch (IllegalArgumentException e) {
				// swallow
			}
		}
		return false;
	}
}
