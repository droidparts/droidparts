/**
 * Copyright 2011 Alex Yanchenko
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

import org.droidparts.activity.FragmentActivity;
import org.droidparts.annotation.inject.InjectFragment;
import org.droidparts.reflection.util.ReflectionUtils;
import org.droidparts.util.L;
import org.droidparts.util.inner.ResourceUtils;

import android.support.v4.app.Fragment;

public class FragmentInjector {

	public static boolean inject(FragmentActivity activity, InjectFragment ann,
			Field field) {
		int fragmenId = ann.value();
		if (fragmenId == 0) {
			String fieldName = field.getName();
			fragmenId = ResourceUtils.getResourceId(activity, fieldName);
		}
		if (fragmenId != 0) {
			Fragment fragment = activity.getSupportFragmentManager()
					.findFragmentById(fragmenId);
			if (field.getType() != fragment.getClass()) {
				// TODO
				L.e("Incompatible types.");
			}
			ReflectionUtils.setFieldVal(field, activity, fragment);
			return true;
		} else {
			L.e("Fragment not found.");
			return false;
		}
	}
}
