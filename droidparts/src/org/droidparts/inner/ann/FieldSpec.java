/**
 * Copyright 2017 Alex Yanchenko
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
package org.droidparts.inner.ann;

import java.lang.reflect.Field;

import android.util.Pair;

import static org.droidparts.inner.ReflectionUtils.getArrayComponentType;
import static org.droidparts.inner.ReflectionUtils.getFieldGenericArgs;
import static org.droidparts.inner.TypeHelper.isArray;
import static org.droidparts.inner.TypeHelper.isCollection;
import static org.droidparts.inner.TypeHelper.isMap;

public class FieldSpec<AnnType extends Ann<?>> {

	public final Field field;
	public final Class<?> genericArg1;
	public final Class<?> genericArg2;

	public final AnnType ann;

	public FieldSpec(Field field, AnnType ann) {
		this.field = field;
		this.ann = ann;
		field.setAccessible(true);
		Pair<Class<?>, Class<?>> genericArgs = getGenericArgs(field);
		this.genericArg1 = genericArgs.first;
		this.genericArg2 = genericArgs.second;
	}

	private static Pair<Class<?>, Class<?>> getGenericArgs(Field field) {
		Class<?> genericArg1 = null;
		Class<?> genericArg2 = null;
		Class<?> fieldType = field.getType();
		if (isArray(fieldType)) {
			genericArg1 = getArrayComponentType(fieldType);
		} else if (isCollection(fieldType) || isMap(fieldType)) {
			Class<?>[] genericArgs = getFieldGenericArgs(field);
			genericArg1 = (genericArgs.length > 0) ? genericArgs[0] : Object.class;
			genericArg2 = (genericArgs.length > 1) ? genericArgs[1] : Object.class;
		}
		return new Pair<Class<?>, Class<?>>(genericArg1, genericArg2);
	}

}