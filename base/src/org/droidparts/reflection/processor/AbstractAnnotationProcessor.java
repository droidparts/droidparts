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
package org.droidparts.reflection.processor;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.droidparts.model.Model;
import org.droidparts.reflection.model.AbstractField;
import org.droidparts.reflection.util.ReflectionUtils;
import org.droidparts.util.L;

public abstract class AbstractAnnotationProcessor {

	protected final Class<? extends Model> cls;

	public AbstractAnnotationProcessor(Class<? extends Model> cls) {
		this.cls = cls;
	}

	protected List<Field> getClassHierarchyFields() {
		return ReflectionUtils.listAnnotatedFields(cls);
	}

	protected void fillField(Field source, AbstractField target) {
		target.fieldName = source.getName();
		target.fieldClass = source.getType();
		Type genericType = source.getGenericType();
		if (genericType instanceof ParameterizedType) {
			Type[] arr = ((ParameterizedType) genericType)
					.getActualTypeArguments();
			target.fieldClassGenericArgs = new Class<?>[arr.length];
			for (int i = 0; i < arr.length; i++) {
				String[] parts = arr[i].toString().split(" ");
				String className = parts[parts.length - 1];
				try {
					target.fieldClassGenericArgs[i] = Class
							.forName(className);
				} catch (ClassNotFoundException e) {
					L.e(e);
				}
			}
		}
	}
}
