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
package org.droidparts.inner.reader;

import java.lang.reflect.Field;

import org.droidparts.inner.ReflectionUtils;
import org.droidparts.inner.ann.inject.InjectViewAnn;
import org.droidparts.util.L;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.View;

public class ViewAndPreferenceReader {

	public static Object getVal(Context ctx, View rootView, InjectViewAnn ann,
			Object target, Field field) throws Exception {
		boolean isView = View.class.isAssignableFrom(field.getType());
		boolean isPreference = Preference.class.isAssignableFrom(field
				.getType());
		if (!isView && !isPreference) {
			throw new Exception("Not a View or Preference '"
					+ field.getType().getName() + "'.");
		}
		int viewOrPrefId = ann.id;
		if (viewOrPrefId == 0) {
			String fieldName = field.getName();
			if (isView) {
				viewOrPrefId = ReflectionUtils.getResourceId(ctx, fieldName);
			} else {
				viewOrPrefId = ReflectionUtils.getStringId(ctx, fieldName);
			}
		}
		Object val;
		if (isView) {
			val = rootView.findViewById(viewOrPrefId);
		} else {
			val = ((PreferenceActivity) ctx).findPreference(ctx
					.getText(viewOrPrefId));
		}
		if (val != null) {
			if (ann.click) {
				if (isView) {
					if (target instanceof View.OnClickListener) {
						((View) val)
								.setOnClickListener((View.OnClickListener) target);
					} else {
						L.w("Failed to set OnClickListener");
					}
				} else {
					boolean done = false;
					Preference pref = (Preference) val;
					if (target instanceof Preference.OnPreferenceClickListener) {
						pref.setOnPreferenceClickListener((Preference.OnPreferenceClickListener) target);
						done = true;
					}
					if (target instanceof Preference.OnPreferenceChangeListener) {
						pref.setOnPreferenceChangeListener((Preference.OnPreferenceChangeListener) target);
						done = true;
					}
					if (!done) {
						L.w("Failed to set OnPreferenceClickListener or OnPreferenceChangeListener.");
					}
				}
			}
			return val;
		} else {
			throw new Exception("View or Preference not found for id.");
		}
	}
}
