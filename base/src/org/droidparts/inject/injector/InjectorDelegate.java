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

import static org.droidparts.reflect.FieldSpecBuilder.getInjectSpecs;
import static org.droidparts.reflect.util.ReflectionUtils.setFieldVal;

import java.lang.reflect.Field;

import org.droidparts.reflect.ann.Ann;
import org.droidparts.reflect.ann.FieldSpec;
import org.droidparts.reflect.ann.inject.InjectAnn;
import org.droidparts.reflect.ann.inject.InjectBundleExtraAnn;
import org.droidparts.reflect.ann.inject.InjectDependencyAnn;
import org.droidparts.reflect.ann.inject.InjectResourceAnn;
import org.droidparts.reflect.ann.inject.InjectSystemServiceAnn;
import org.droidparts.reflect.ann.inject.InjectViewAnn;
import org.droidparts.util.L;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

public class InjectorDelegate {

	public static void setUp(Context ctx) {
		DependencyReader.init(ctx);
	}

	public static void tearDown() {
		DependencyReader.tearDown();
	}

	public final void inject(Context ctx, View root, Object target) {
		long start = System.currentTimeMillis();
		final Class<?> cls = target.getClass();
		for (FieldSpec<InjectAnn<?>> spec : getInjectSpecs(cls)) {
			try {
				Object val = getVal(ctx, root, target, spec.ann, spec.field);
				if (val != null) {
					setFieldVal(target, spec.field, val);
				}
			} catch (Throwable e) {
				L.e("Failed to inject field '" + spec.field.getName() + "' in "
						+ cls.getSimpleName() + ", cause: " + e.getMessage()
						+ ".");
				L.d(e);
			}
		}
		L.d(String.format("Injected on %s in %d ms.", cls.getSimpleName(),
				(System.currentTimeMillis() - start)));
	}

	protected Object getVal(Context ctx, View root, Object target, Ann<?> ann,
			Field field) throws Exception {
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
			val = SystemServiceReader.getVal(ctx,
					(InjectSystemServiceAnn) ann, field);
		} else if (annType == InjectViewAnn.class) {
			if (root == null) {
				throw new IllegalArgumentException("Null View.");
			}
			val = ViewAndPreferenceReader.getVal(ctx, root,
					(InjectViewAnn) ann, target, field);
		}
		return val;
	}

	protected Bundle getIntentExtras(Object obj) {
		Bundle data = null;
		if (obj instanceof Activity) {
			data = ((Activity) obj).getIntent().getExtras();
		} else if (obj instanceof Service) {
			// TODO
		}
		return data;
	}

}
