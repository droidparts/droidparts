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
package org.droidparts.reflect.util;

import static org.droidparts.reflect.util.TypeHelper.isArray;
import static org.droidparts.reflect.util.TypeHelper.toObjectArr;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.droidparts.util.L;

public class ReflectionUtils {

	public static Field getField(Class<?> cls, String fieldName)
			throws IllegalArgumentException {
		try {
			return cls.getField(fieldName);
		} catch (Exception e) {
			L.e(cls.getSimpleName() + " has no field " + fieldName + ".");
			throw new IllegalArgumentException(e);
		}

	}

	public static <FieldType> FieldType getTypedFieldVal(Field field, Object obj)
			throws IllegalArgumentException {
		try {
			field.setAccessible(true);
			@SuppressWarnings("unchecked")
			FieldType val = (FieldType) field.get(obj);
			return val;
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static void setFieldVal(Field field, Object obj, Object val)
			throws IllegalArgumentException {
		try {
			if (val == null) {
				throw new IllegalArgumentException("null val");
			}
			field.setAccessible(true);
			field.set(obj, val);
		} catch (Exception e) {
			String valClsName = (val != null) ? val.getClass().getSimpleName()
					: "";
			L.w("Error assigning (" + valClsName + ")" + val + " to ("
					+ field.getType().getSimpleName() + ") field "
					+ obj.getClass().getSimpleName() + "#" + field.getName()
					+ ": " + e.getMessage());
			throw new IllegalArgumentException(e);
		}
	}

	public static Class<?> classForName(String clsName)
			throws IllegalArgumentException {
		try {
			return Class.forName(clsName);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static <InstanceType> InstanceType instantiate(Class<?> cls)
			throws IllegalArgumentException {
		try {
			@SuppressWarnings("unchecked")
			InstanceType instance = (InstanceType) cls.newInstance();
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

	public static Class<?> getArrayType(Class<?> arrCls) {
		if (arrCls == byte[].class) {
			return byte.class;
		} else if (arrCls == short[].class) {
			return short.class;
		} else if (arrCls == int[].class) {
			return int.class;
		} else if (arrCls == long[].class) {
			return long.class;
		} else if (arrCls == float[].class) {
			return float.class;
		} else if (arrCls == double[].class) {
			return double.class;
		} else if (arrCls == boolean[].class) {
			return boolean.class;
		} else if (arrCls == char[].class) {
			return char.class;
		} else {
			// objects - [Ljava.lang.String;
			String clsName = arrCls.getName();
			clsName = clsName.substring(2, clsName.length() - 1);
			return classForName(clsName);
		}
	}

	public static Class<?>[] getFieldGenericArgs(Field field) {
		Type genericType = field.getGenericType();
		if (genericType instanceof ParameterizedType) {
			Type[] typeArr = ((ParameterizedType) genericType)
					.getActualTypeArguments();
			Class<?>[] argsArr = new Class<?>[typeArr.length];
			for (int i = 0; i < typeArr.length; i++) {
				// class java.lang.String
				String[] nameParts = typeArr[i].toString().split(" ");
				String clsName = nameParts[nameParts.length - 1];
				argsArr[i] = classForName(clsName);
			}
			return argsArr;
		} else {
			return new Class<?>[0];
		}
	}

	public static Object[] varArgsHack(Object[] varArgs) {
		if (varArgs != null && varArgs.length == 1) {
			Object firstArg = varArgs[0];
			if (firstArg != null) {
				Class<?> firstArgCls = firstArg.getClass();
				if (isArray(firstArgCls)) {
					varArgs = toObjectArr(firstArg);
				}
			}
		}
		return varArgs;
	}

	protected ReflectionUtils() {
	}

}
