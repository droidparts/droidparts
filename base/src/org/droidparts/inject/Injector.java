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
package org.droidparts.inject;

import static org.droidparts.reflection.util.ReflectionUtils.getClassTreeFields;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import org.droidparts.annotation.inject.Inject;
import org.droidparts.annotation.inject.InjectIntentExtra;
import org.droidparts.annotation.inject.InjectResource;
import org.droidparts.annotation.inject.InjectSystemService;
import org.droidparts.annotation.inject.InjectView;
import org.droidparts.inject.injector.IntentExtraInjector;
import org.droidparts.inject.injector.ModuleInjector;
import org.droidparts.inject.injector.ResourceInjector;
import org.droidparts.inject.injector.SystemServiceInjector;
import org.droidparts.inject.injector.ViewInjector;
import org.droidparts.util.L;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

/**
 * <meta-data android:name="droidparts_module"
 * android:value="com.yanchenko.android.sample.Module" />
 */
public class Injector {

	private static Context ctx;

	public static void setUp(Context ctx) {
		Injector.ctx = ctx.getApplicationContext();
	}

	public static void tearDown() {
		AbstractModule module = ModuleInjector.getModule();
		if (module != null) {
			module.getDB().close();
		}
		ctx = null;
	}

	public static void inject(Activity act) {
		// XXX
		View root = act.findViewById(android.R.id.content).getRootView();
		inject(act, root, act);
	}

	public static void inject(Service serv) {
		inject(serv, null, serv);
	}

	public static void inject(Context ctx, Object target) {
		inject(ctx, null, target);
	}

	public static void inject(View view, Object target) {
		inject(view.getContext(), view, target);
	}

	public static void inject(Object target) {
		if (Injector.ctx != null) {
			inject(Injector.ctx, null, target);
		} else {
			throw new IllegalStateException("No context provided.");
		}
	}

	private static void inject(Context ctx, View root, Object target) {
		long start = System.currentTimeMillis();
		if (Injector.ctx == null) {
			Injector.ctx = ctx.getApplicationContext();
		}
		final Class<?> cls = target.getClass();
		List<Field> fields = getClassTreeFields(cls);
		for (Field field : fields) {
			for (Annotation ann : field.getAnnotations()) {
				Class<? extends Annotation> annType = ann.annotationType();
				boolean success = false;
				if (annType == Inject.class) {
					success = ModuleInjector.inject(ctx, target, field);
					if (!success) {
						// TODO try injecting resource?
					}
				} else if (annType == InjectIntentExtra.class) {
					Bundle data = getIntentExtras(ctx);
					success = IntentExtraInjector.inject(ctx, data,
							(InjectIntentExtra) ann, target, field);
				} else if (annType == InjectResource.class) {
					success = ResourceInjector.inject(ctx,
							(InjectResource) ann, target, field);
				} else if (annType == InjectSystemService.class) {
					success = SystemServiceInjector.inject(ctx,
							(InjectSystemService) ann, target, field);
				} else if (annType == InjectView.class) {
					if (root != null) {
						success = ViewInjector.inject(ctx, root,
								(InjectView) ann, target, field);
					}
				}
				if (success) {
					break;
				}
			}
		}
		long end = System.currentTimeMillis() - start;
		L.d(String.format("Injected on %s in %d ms.", cls.getSimpleName(), end));
	}

	private static Bundle getIntentExtras(Context ctx) {
		// FIXME or Service
		Activity act = (Activity) ctx;
		Bundle data = act.getIntent().getExtras();
		return data;
	}

	private Injector() {

	}

}
