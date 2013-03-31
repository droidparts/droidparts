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
package org.droidparts.type.reader;

import java.lang.reflect.Field;

import org.droidparts.type.ann.inject.InjectFragmentAnn;
import org.droidparts.util.inner.ResourceUtils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

public class SupportFragmentReader {

	public static Object getVal(Object fragmentActivityObj,
			InjectFragmentAnn ann, Field field) {
		FragmentActivity fragmentActivity = (FragmentActivity) fragmentActivityObj;
		int fragmentId = ann.id;
		if (fragmentId == 0) {
			fragmentId = ResourceUtils.getResourceId(fragmentActivity,
					field.getName());
		}
		return fragmentActivity.getSupportFragmentManager().findFragmentById(
				fragmentId);
	}

	public static Bundle getIntentExtras(Object obj) {
		if (obj instanceof Fragment) {
			return ((Fragment) obj).getArguments();
		} else {
			return null;
		}
	}

}
