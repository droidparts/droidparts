/**
 * Copyright 2017 Alex Yanchenko
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
package org.droidparts.inner;

import java.io.Serializable;
import java.lang.reflect.Field;

import android.os.Bundle;
import android.os.Parcelable;

import org.droidparts.inner.ann.FieldSpec;
import org.droidparts.inner.ann.serialize.SaveInstanceStateAnn;

public class InstanceStateSaver {

	public static void onCreate(Object obj, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			FieldSpec<SaveInstanceStateAnn>[] specs = ClassSpecRegistry.getSaveInstanceSpecs(obj.getClass());
			for (FieldSpec<SaveInstanceStateAnn> spec : specs) {
				String key = getKey(spec.field);
				if (savedInstanceState.containsKey(key)) {
					Object val = savedInstanceState.get(key);
					ReflectionUtils.setFieldVal(obj, spec.field, val);
				}
			}
		}
	}

	public static void onSaveInstanceState(Object obj, Bundle outState) {
		FieldSpec<SaveInstanceStateAnn>[] specs = ClassSpecRegistry.getSaveInstanceSpecs(obj.getClass());
		for (FieldSpec<SaveInstanceStateAnn> spec : specs) {
			String key = getKey(spec.field);
			Object val = ReflectionUtils.getFieldVal(obj, spec.field);
			if (val instanceof Parcelable) {
				outState.putParcelable(key, (Parcelable) val);
			} else {
				outState.putSerializable(key, (Serializable) val);
			}
		}
	}

	private static String getKey(Field f) {
		return "__" + f.getName();
	}

}
