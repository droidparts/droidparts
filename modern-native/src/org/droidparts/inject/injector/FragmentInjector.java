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

import org.droidparts.reflect.ann.inject.InjectFragmentAnn;
import org.droidparts.util.inner.ResourceUtils;

import android.app.Activity;
import android.app.Fragment;

public class FragmentInjector {

	static boolean inject(Activity activity, InjectFragmentAnn ann, Field field) {
		int fragmentId = ann.value;
		if (fragmentId == 0) {
			fragmentId = ResourceUtils.getResourceId(activity, field.getName());
		}
		if (fragmentId != 0) {
			Fragment fragment = activity.getFragmentManager().findFragmentById(
					fragmentId);
			try {
				setFieldVal(activity, field, fragment);
				return true;
			} catch (IllegalArgumentException e) {
				// swallow
			}
		}
		return false;
	}

}
