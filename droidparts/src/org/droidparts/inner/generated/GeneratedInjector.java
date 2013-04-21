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
package org.droidparts.inner.generated;

import java.lang.reflect.Constructor;

import android.content.Context;
import android.view.View;

public abstract class GeneratedInjector {

	private static final String SUFFIX = "$$DroidPartsInjector";

	public static GeneratedInjector getInstance(Object enclosingInstance) {
		Class<?> enclosingClass = enclosingInstance.getClass();
		String clsName = enclosingClass.getName() + SUFFIX;
		try {
			Constructor<?> constr = Class.forName(clsName)
					.getConstructor(enclosingClass);
			return (GeneratedInjector) constr
					.newInstance(enclosingInstance);
		} catch (Exception e) {
			return null;
		}
	}

	public abstract void inject(Context ctx, View root, Object target);

}
