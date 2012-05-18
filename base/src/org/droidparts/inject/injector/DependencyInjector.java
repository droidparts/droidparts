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

import static android.content.pm.PackageManager.GET_META_DATA;
import static org.droidparts.reflection.util.ReflectionUtils.setFieldVal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.droidparts.inject.AbstractDependencyProvider;
import org.droidparts.util.L;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

public class DependencyInjector {

	private static final String META_KEY = "droidparts_dependency_provider";

	private static volatile boolean inited = false;
	private static AbstractDependencyProvider module;
	private static HashMap<Class<?>, Method> methodRegistry = new HashMap<Class<?>, Method>();

	static void init(Context ctx) {
		if (!inited) {
			synchronized (DependencyInjector.class) {
				if (!inited) {
					module = getModule(ctx);
					if (module != null) {
						Method[] methods = module.getClass().getMethods();
						for (Method method : methods) {
							methodRegistry.put(method.getReturnType(), method);
						}
					}
					inited = true;
				}
			}
		}
	}

	static void tearDown() {
		if (module != null) {
			module.getDB().close();
		}
		module = null;
	}

	static boolean inject(Context ctx, Object target, Field field) {
		init(ctx);
		if (module != null) {
			Method method = methodRegistry.get(field.getType());
			if (method != null) {
				Object val = null;
				try {
					val = method.invoke(module);
				} catch (Exception e) {
					try {
						val = method.invoke(module, ctx);
					} catch (Exception ex) {
						L.e("No dependency provided for "
								+ field.getType().getCanonicalName());
						return false;
					}
				}
				if (val != null) {
					setFieldVal(field, target, val);
					return true;
				}
			}
		}
		return false;
	}

	private static AbstractDependencyProvider getModule(Context ctx) {
		PackageManager pm = ctx.getPackageManager();
		Bundle metaData = null;
		try {
			metaData = pm.getApplicationInfo(ctx.getPackageName(),
					GET_META_DATA).metaData;
		} catch (NameNotFoundException e) {
			L.d(e);
		}
		if (metaData == null) {
			L.e("No <meta-data />.");
			return null;
		}
		String className = metaData.getString(META_KEY);
		try {
			Class<?> cls = Class.forName(className);
			Constructor<?> constr = cls.getConstructor(Context.class);
			AbstractDependencyProvider module = (AbstractDependencyProvider) constr
					.newInstance(ctx.getApplicationContext());
			return module;
		} catch (Exception e) {
			L.d(e);
			L.e("No such droidparts dependency provider: " + className);
			return null;
		}
	}

}
