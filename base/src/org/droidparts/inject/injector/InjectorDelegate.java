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

import static org.droidparts.reflection.util.ReflectionUtils.listAnnotatedFields;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import org.droidparts.annotation.inject.InjectBundleExtra;
import org.droidparts.annotation.inject.InjectDependency;
import org.droidparts.annotation.inject.InjectResource;
import org.droidparts.annotation.inject.InjectSystemService;
import org.droidparts.annotation.inject.InjectView;
import org.droidparts.util.L;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

public class InjectorDelegate {

	public static void setUp(Context ctx) {
		DependencyInjector.init(ctx);
	}

	public static void tearDown() {
		DependencyInjector.tearDown();
	}

	public void inject(Context ctx, View root, Object target) {
		long start = System.currentTimeMillis();
		final Class<?> cls = target.getClass();
		List<Field> fields = listAnnotatedFields(cls);
		for (Field field : fields) {
			for (Annotation ann : field.getAnnotations()) {
				boolean success = inject(ctx, root, target, ann, field);
				if (success) {
					break;
				}
				// TODO error logging
			}
		}
		long end = System.currentTimeMillis() - start;
		L.d(String.format("Injected on %s in %d ms.", cls.getSimpleName(), end));
	}

	protected boolean inject(Context ctx, View root, Object target,
			Annotation ann, Field field) {
		Class<? extends Annotation> annType = ann.annotationType();
		L.wtf(annType.getCanonicalName());
		boolean success = false;
		if (annType == InjectDependency.class) {
			success = DependencyInjector.inject(ctx, target, field);
		} else if (annType == InjectBundleExtra.class) {
			Bundle data = getIntentExtras(target);
			success = BundleExtraInjector.inject(ctx, data,
					(InjectBundleExtra) ann, target, field);
		} else if (annType == InjectResource.class) {
			success = ResourceInjector.inject(ctx, (InjectResource) ann,
					target, field);
		} else if (annType == InjectSystemService.class) {
			success = SystemServiceInjector.inject(ctx,
					(InjectSystemService) ann, target, field);
		} else if (annType == InjectView.class) {
			if (root != null) {
				success = ViewInjector.inject(ctx, root, (InjectView) ann,
						target, field);
			}
		}
		return success;
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
