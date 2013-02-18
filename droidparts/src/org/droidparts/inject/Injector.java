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
package org.droidparts.inject;

import static org.droidparts.reflect.FieldSpecBuilder.getInjectSpecs;
import static org.droidparts.reflect.util.ReflectionUtils.setFieldVal;

import java.lang.reflect.Field;

import org.droidparts.inject.reader.BundleExtraReader;
import org.droidparts.inject.reader.DependencyReader;
import org.droidparts.inject.reader.NativeFragmentReader;
import org.droidparts.inject.reader.NativeParentActivityReader;
import org.droidparts.inject.reader.ResourceReader;
import org.droidparts.inject.reader.SupportFragmentReader;
import org.droidparts.inject.reader.SupportParentActivityReader;
import org.droidparts.inject.reader.SystemServiceReader;
import org.droidparts.inject.reader.ViewAndPreferenceReader;
import org.droidparts.reflect.ann.Ann;
import org.droidparts.reflect.ann.FieldSpec;
import org.droidparts.reflect.ann.inject.InjectAnn;
import org.droidparts.reflect.ann.inject.InjectBundleExtraAnn;
import org.droidparts.reflect.ann.inject.InjectDependencyAnn;
import org.droidparts.reflect.ann.inject.InjectFragmentAnn;
import org.droidparts.reflect.ann.inject.InjectParentActivityAnn;
import org.droidparts.reflect.ann.inject.InjectResourceAnn;
import org.droidparts.reflect.ann.inject.InjectSystemServiceAnn;
import org.droidparts.reflect.ann.inject.InjectViewAnn;
import org.droidparts.util.L;

import android.app.Activity;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

/**
 * <meta-data android:name="droidparts_dependency_provider"
 * android:value="com.yanchenko.android.sample.DependencyProvider" />
 */
public class Injector {

	public static Context getApplicationContext() {
		return appCtx;
	}

	public static Injector get() {
		return Holder.INJECTOR;
	}

	public void setUp(Context ctx) {
		setContext(ctx);
		DependencyReader.init(ctx);
	}

	public void tearDown() {
		DependencyReader.tearDown();
		appCtx = null;
	}

	public <T> T getDependency(Context ctx, Class<T> cls)
			throws RuntimeException {
		setContext(ctx);
		return DependencyReader.getVal(ctx, cls);
	}

	public void inject(Activity act) {
		setContext(act);
		View root = act.findViewById(android.R.id.content).getRootView();
		inject(act, root, act);
	}

	public void inject(Service serv) {
		setContext(serv);
		inject(serv, null, serv);
	}

	public void inject(Context ctx, Object target) {
		setContext(ctx);
		inject(ctx, null, target);
	}

	public void inject(Dialog dialog, Object target) {
		View root = dialog.findViewById(android.R.id.content).getRootView();
		inject(root, target);
	}

	public void inject(View view, Object target) {
		Context ctx = view.getContext();
		setContext(ctx);
		inject(ctx, view, target);
	}

	//

	private static volatile Context appCtx;

	static class Holder {
		static final Injector INJECTOR = new Injector();
	}

	private Injector() {
	}

	private static void setContext(Context ctx) {
		if (appCtx == null) {
			appCtx = ctx.getApplicationContext();
		}
	}

	//

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
				L.w("Failed to inject %s#%s: %s.", cls.getSimpleName(),
						spec.field.getName(), e.getMessage());
				L.d(e);
			}
		}
		L.i("Injected into %s in %d ms.", cls.getSimpleName(),
				(System.currentTimeMillis() - start));
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

	protected Bundle getIntentExtras(Object obj) {
		Bundle data = null;
		if (obj instanceof Activity) {
			data = ((Activity) obj).getIntent().getExtras();
		} else if (obj instanceof Service) {
			// TODO
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
				Class.forName("com.actionbarsherlock.ActionBarSherlock");
				_useSupport = true;
			} catch (Exception e) {
				_useSupport = !nativeAvailable();
			}
		}
		return true;
	}

	private static boolean nativeAvailable() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	private static Boolean _useSupport;

}
