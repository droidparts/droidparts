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
package org.droidparts.reflection.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.droidparts.util.L;

public final class ReflectionUtils {

	public static boolean canAssign(Field to, Object obj) {
		return to.getType().isAssignableFrom(obj.getClass());
	}

	public static Field getField(Class<?> cls, String fieldName)
			throws IllegalArgumentException {
		try {
			return cls.getField(fieldName);
		} catch (Exception e) {
			L.e(cls.getSimpleName() + " has no field " + fieldName + ".");
			throw new IllegalArgumentException(e);
		}

	}

	public static <Type> Type getTypedFieldVal(Field field, Object obj)
			throws IllegalArgumentException {
		try {
			field.setAccessible(true);
			@SuppressWarnings("unchecked")
			Type val = (Type) field.get(obj);
			return val;
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static void setFieldVal(Field field, Object obj, Object val)
			throws IllegalArgumentException {
		try {
			field.setAccessible(true);
			field.set(obj, val);
		} catch (Exception e) {
			L.e("Error assigning (" + val.getClass().getSimpleName() + ")"
					+ val + " to (" + field.getType().getSimpleName()
					+ ") field " + obj.getClass().getSimpleName() + "#"
					+ field.getName() + ".");
			throw new IllegalArgumentException(e);
		}
	}

	public static <Type> Type instantiate(Class<?> cls)
			throws IllegalArgumentException {
		try {
			@SuppressWarnings("unchecked")
			Type instance = (Type) cls.newInstance();
			return instance;
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static Enum<?> instantiateEnum(Class<?> fieldCls, String str) {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		Enum en = Enum.valueOf(fieldCls.asSubclass(Enum.class), str);
		return en;
	}

	public static List<Field> listAnnotatedFields(Class<?> cls) {
		ArrayList<Class<?>> clsTree = new ArrayList<Class<?>>();
		boolean enteredDroidParts = false;
		do {
			clsTree.add(0, cls);
			boolean inDroidParts = cls.getCanonicalName().startsWith(
					"org.droidparts");
			if (enteredDroidParts && !inDroidParts) {
				break;
			} else {
				enteredDroidParts = inDroidParts;
				cls = cls.getSuperclass();
			}
		} while (cls != null);
		ArrayList<Field> fields = new ArrayList<Field>();
		for (Class<?> c : clsTree) {
			for (Field f : c.getDeclaredFields()) {
				if (f.getAnnotations().length > 0) {
					fields.add(f);
				}
			}
		}
		return fields;
	}

	private ReflectionUtils() {
	}

}
