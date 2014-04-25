/**
 * Copyright 2014 Alex Yanchenko
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
package org.droidparts;

import static org.droidparts.inner.ReflectionUtils.setFieldVal;

import org.droidparts.inner.ClassSpecRegistry;
import org.droidparts.inner.ann.FieldSpec;
import org.droidparts.inner.ann.inject.InjectAnn;
import org.droidparts.inner.reader.DependencyReader;
import org.droidparts.inner.reader.ValueReader;
import org.droidparts.util.L;

import android.app.Activity;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.view.View;

/**
 * <meta-data android:name="droidparts_dependency_provider"
 * android:value="com.yanchenko.android.sample.DependencyProvider" />
 */
public class Injector {

	public static void inject(Activity act) {
		setContext(act);
		View root = act.findViewById(android.R.id.content).getRootView();
		inject(act, root, act);
	}

	public static void inject(Service serv) {
		setContext(serv);
		inject(serv, null, serv);
	}

	public static void inject(Context ctx, Object target) {
		setContext(ctx);
		inject(ctx, null, target);
	}

	public static void inject(Dialog dialog, Object target) {
		View root = dialog.findViewById(android.R.id.content).getRootView();
		inject(root, target);
	}

	public static void inject(View view, Object target) {
		Context ctx = view.getContext();
		setContext(ctx);
		inject(ctx, view, target);
	}

	public static <T> T getDependency(Context ctx, Class<T> cls)
			throws RuntimeException {
		setContext(ctx);
		return DependencyReader.readVal(ctx, cls);
	}

	public static Context getApplicationContext() {
		return appCtx;
	}

	public static void setUp(Context ctx) {
		setContext(ctx);
		DependencyReader.init(ctx);
	}

	public static void tearDown() {
		DependencyReader.tearDown();
		appCtx = null;
	}

	private static void setContext(Context ctx) {
		if (appCtx == null) {
			appCtx = ctx.getApplicationContext();
		}
	}

	private static volatile Context appCtx;

	private static void inject(Context ctx, View root, Object target) {
		long start = System.currentTimeMillis();
		Class<?> cls = target.getClass();
		FieldSpec<InjectAnn<?>>[] specs = ClassSpecRegistry.getInjectSpecs(cls);
		for (FieldSpec<InjectAnn<?>> spec : specs) {
			try {
				Object val = ValueReader.getVal(ctx, root, target, spec);
				if (val != null) {
					setFieldVal(target, spec.field, val);
				}
			} catch (Throwable e) {
				L.w("Failed to inject %s#%s: %s.", cls.getSimpleName(),
						spec.field.getName(), e.getMessage());
				L.d(e);
			}
		}
		L.i("Injected into %s in %d ms.", cls.getSimpleName(),
				(System.currentTimeMillis() - start));
	}

	private Injector() {
	}

}
