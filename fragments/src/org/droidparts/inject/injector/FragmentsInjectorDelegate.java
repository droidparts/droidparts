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

import org.droidparts.reflect.ann.Ann;
import org.droidparts.reflect.ann.inject.InjectFragmentAnn;
import org.droidparts.reflect.ann.inject.InjectParentActivityAnn;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

public class FragmentsInjectorDelegate extends InjectorDelegate {

	@Override
	protected Object getVal(Context ctx, View root, Object target, Ann<?> ann,
			Field field) throws Exception {
		boolean handled = false;
		Class<?> annType = ann.getClass();
		Object val = null;
		if (annType == InjectFragmentAnn.class) {
			handled = true;
			if (useSupport()) {
				val = SupportFragmentReader.getVal(target,
						(InjectFragmentAnn) ann, field);
			} else {
				val = NativeFragmentReader.getVal(target,
						(InjectFragmentAnn) ann, field);
			}
		} else if (annType == InjectParentActivityAnn.class) {
			handled = true;
			if (useSupport()) {
				val = SupportParentActivityReader.getVal(target);
			} else {
				val = NativeParentActivityReader.getVal(target);
			}
		}
		return handled ? val : super.getVal(ctx, root, target, ann, field);
	}

	@Override
	protected Bundle getIntentExtras(Object obj) {
		Bundle data;
		if (useSupport()) {
			data = SupportFragmentReader.getIntentExtras(obj);
		} else {
			data = NativeFragmentReader.getIntentExtras(obj);
		}
		return (data != null) ? data : super.getIntentExtras(obj);
	}

	private static boolean useSupport() {
		if (_useSupport == null) {
			try {
				Class.forName("com.actionbarsherlock.ActionBarSherlock");
				_useSupport = true;
			} catch (Exception e) {
				_useSupport = Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB;
			}
		}
		return true;
	}

	private static Boolean _useSupport;
}