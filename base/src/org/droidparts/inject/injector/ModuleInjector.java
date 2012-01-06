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
package org.droidparts.inject.injector;

import static android.content.pm.PackageManager.GET_META_DATA;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.droidparts.inject.AbstractModule;
import org.droidparts.reflection.util.ReflectionUtils;
import org.droidparts.util.L;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

public class ModuleInjector {

	private static final String META_KEY = "droidparts_module";

	private static volatile boolean inited = false;
	private static AbstractModule module;
	private static HashMap<Class<?>, Method> methodRegistry = new HashMap<Class<?>, Method>();

	public static boolean inject(Context ctx, Object target, Field field) {
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
						throw new IllegalArgumentException(e);
					}
				}
				if (val != null) {
					ReflectionUtils.setFieldVal(field, target, val);
				}
				return true;
			}
		}
		return false;
	}

	public static void tearDown() {
		if (module != null) {
			module.getDB().close();
		}
		module = null;
	}

	private static void init(Context ctx) {
		if (!inited) {
			synchronized (ModuleInjector.class) {
				if (!inited) {
					module = getModule(ctx);
					Method[] methods = module.getClass().getMethods();
					for (Method method : methods) {
						methodRegistry.put(method.getReturnType(), method);
					}
					inited = true;
				}
			}
		}
	}

	private static AbstractModule getModule(Context ctx) {
		PackageManager pm = ctx.getPackageManager();
		Bundle metaData;
		try {
			metaData = pm.getApplicationInfo(ctx.getPackageName(),
					GET_META_DATA).metaData;
		} catch (NameNotFoundException e) {
			L.d(e);
			return null;
		}
		if (metaData == null) {
			L.e("No <meta-data />.");
			return null;
		}
		String className = metaData.getString(META_KEY);
		try {
			Class<?> cls = Class.forName(className);
			Constructor<?> constr = cls.getConstructor(Context.class);
			AbstractModule module = (AbstractModule) constr.newInstance(ctx
					.getApplicationContext());
			return module;
		} catch (Exception e) {
			L.e("No such droidparts module: " + className);
			return null;
		}
	}

}
