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
package org.droidparts.inner.reader;

import static org.droidparts.inner.ManifestMetaData.DEPENDENCY_PROVIDER;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.droidparts.AbstractDependencyProvider;
import org.droidparts.inner.ManifestMetaData;
import org.droidparts.inner.ann.Ann;
import org.droidparts.inner.ann.MethodSpec;
import org.droidparts.util.L;

import android.content.Context;

public class DependencyReader {

	private static volatile boolean inited = false;
	private static AbstractDependencyProvider dependencyProvider;
	private static HashMap<Class<?>, MethodSpec<?>> methodRegistry = new HashMap<Class<?>, MethodSpec<?>>();

	public static void init(Context ctx) {
		if (!inited) {
			synchronized (DependencyReader.class) {
				if (!inited) {
					dependencyProvider = getDependencyProvider(ctx);
					if (dependencyProvider != null) {
						Method[] methods = dependencyProvider.getClass()
								.getMethods();
						for (Method method : methods) {
							methodRegistry.put(method.getReturnType(),
									new MethodSpec<Ann<Annotation>>(method,
											null));
						}
					}
					inited = true;
				}
			}
		}
	}

	public static void tearDown() {
		if (dependencyProvider != null) {
			dependencyProvider.getDB().close();
		}
		dependencyProvider = null;
	}

	@SuppressWarnings("unchecked")
	public static <T> T readVal(Context ctx, Class<T> valType)
			throws RuntimeException {
		init(ctx);
		T val = null;
		if (dependencyProvider != null) {
			MethodSpec<?> spec = methodRegistry.get(valType);
			try {
				int paramCount = spec.paramTypes.length;
				if (paramCount == 0) {
					val = (T) spec.method.invoke(dependencyProvider);
				} else {
					val = (T) spec.method.invoke(dependencyProvider, ctx);
				}
			} catch (Exception e) {
				throw new RuntimeException(
						"No valid DependencyProvider method for "
								+ valType.getName() + ".", e);
			}
		}
		return val;
	}

	private static AbstractDependencyProvider getDependencyProvider(Context ctx) {
		String className;
		try {
			className = ManifestMetaData.get(ctx, DEPENDENCY_PROVIDER);
		} catch (Exception e) {
			L.d(e);
			L.e("No <meta-data android:name=\"%s\" android:value=\"...\"/> in AndroidManifest.xml.",
					ManifestMetaData.DEPENDENCY_PROVIDER);
			return null;
		}
		if (className.startsWith(".")) {
			className = ctx.getPackageName() + className;
		}
		try {
			Class<?> cls = Class.forName(className);
			Constructor<?> constr = cls.getConstructor(Context.class);
			AbstractDependencyProvider adp = (AbstractDependencyProvider) constr
					.newInstance(ctx.getApplicationContext());
			return adp;
		} catch (Exception e) {
			L.e("Not a valid DroidParts dependency provider: %s.", className);
			L.d(e);
			return null;
		}
	}

}
