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

import org.droidparts.inner.ann.Ann;
import org.droidparts.inner.ann.inject.InjectBundleExtraAnn;
import org.droidparts.inner.ann.inject.InjectDependencyAnn;
import org.droidparts.inner.ann.inject.InjectFragmentAnn;
import org.droidparts.inner.ann.inject.InjectParentActivityAnn;
import org.droidparts.inner.ann.inject.InjectResourceAnn;
import org.droidparts.inner.ann.inject.InjectSystemServiceAnn;
import org.droidparts.inner.ann.inject.InjectViewAnn;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

public class ValueReader {

	public static Object getVal(Context ctx, View root, Object target,
			Ann<?> ann, Field field) throws Exception {
		Class<?> annType = ann.getClass();
		Object val = null;
		if (annType == InjectDependencyAnn.class) {
			val = DependencyReader.getVal(ctx, field.getType());
		} else if (annType == InjectBundleExtraAnn.class) {
			Bundle data = getIntentExtras(target);
			val = BundleExtraReader.getVal((InjectBundleExtraAnn) ann, data);
		} else if (annType == InjectResourceAnn.class) {
			val = ResourceReader.getVal(ctx, (InjectResourceAnn) ann, field);
		} else if (annType == InjectSystemServiceAnn.class) {
			val = SystemServiceReader.getVal(ctx, (InjectSystemServiceAnn) ann,
					field);
		} else if (annType == InjectViewAnn.class) {
			if (root == null) {
				throw new IllegalArgumentException("Null View.");
			}
			val = ViewAndPreferenceReader.getVal(ctx, root,
					(InjectViewAnn) ann, target, field);
		} else if (annType == InjectFragmentAnn.class) {
			if (useSupport()) {
				val = SupportFragmentReader.getVal(target,
						(InjectFragmentAnn) ann, field);
			} else if (nativeAvailable()) {
				val = NativeFragmentReader.getVal(target,
						(InjectFragmentAnn) ann, field);
			}
		} else if (annType == InjectParentActivityAnn.class) {
			if (useSupport()) {
				val = SupportParentActivityReader.getVal(target);
			} else if (nativeAvailable()) {
				val = NativeParentActivityReader.getVal(target);
			}
		}
		return val;
	}

	private static Bundle getIntentExtras(Object obj) {
		Bundle data = null;
		if (obj instanceof Activity) {
			data = ((Activity) obj).getIntent().getExtras();
		} else if (useSupport()) {
			data = SupportFragmentReader.getIntentExtras(obj);
		} else if (nativeAvailable()) {
			data = NativeFragmentReader.getIntentExtras(obj);
		}
		return data;
	}

	private static boolean useSupport() {
		if (_useSupport == null) {
			try {
				Class.forName("android.support.v4.app.Fragment");
				_useSupport = true;
			} catch (Exception e) {
				_useSupport = false;
			}
		}
		return _useSupport;
	}

	private static boolean nativeAvailable() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	private static Boolean _useSupport;

}
