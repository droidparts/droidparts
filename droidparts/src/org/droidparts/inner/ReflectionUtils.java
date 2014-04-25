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
package org.droidparts.inner;

import static org.droidparts.inner.TypeHelper.isArray;
import static org.droidparts.inner.TypeHelper.isBoolean;
import static org.droidparts.inner.TypeHelper.isByte;
import static org.droidparts.inner.TypeHelper.isCharacter;
import static org.droidparts.inner.TypeHelper.isDouble;
import static org.droidparts.inner.TypeHelper.isFloat;
import static org.droidparts.inner.TypeHelper.isInteger;
import static org.droidparts.inner.TypeHelper.isLong;
import static org.droidparts.inner.TypeHelper.isShort;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;

import org.droidparts.util.Arrays2;
import org.droidparts.util.L;

public final class ReflectionUtils {

	@SuppressWarnings("unchecked")
	public static <T> T getFieldVal(Object obj, Field field)
			throws IllegalArgumentException {
		Class<?> ft = field.getType();
		Object val;
		try {
			if (isBoolean(ft, false)) {
				val = Boolean.valueOf(field.getBoolean(obj));
			} else if (isInteger(ft, false)) {
				val = Integer.valueOf(field.getInt(obj));
			} else if (isLong(ft, false)) {
				val = Long.valueOf(field.getLong(obj));
			} else if (isFloat(ft, false)) {
				val = Float.valueOf(field.getFloat(obj));
			} else if (isDouble(ft, false)) {
				val = Double.valueOf(field.getDouble(obj));
			} else if (isByte(ft, false)) {
				val = Byte.valueOf(field.getByte(obj));
			} else if (isShort(ft, false)) {
				val = Short.valueOf(field.getShort(obj));
			} else if (isCharacter(ft, false)) {
				val = Character.valueOf(field.getChar(obj));
			} else {
				val = field.get(obj);
			}
			return (T) val;
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static void setFieldVal(Object obj, Field field, Object val)
			throws IllegalArgumentException {
		Class<?> ft = field.getType();
		try {
			if (isBoolean(ft, false)) {
				field.setBoolean(obj, (Boolean) val);
			} else if (isInteger(ft, false)) {
				field.setInt(obj, (Integer) val);
			} else if (isLong(ft, false)) {
				field.setLong(obj, (Long) val);
			} else if (isFloat(ft, false)) {
				field.setFloat(obj, (Float) val);
			} else if (isDouble(ft, false)) {
				field.setDouble(obj, (Double) val);
			} else if (isByte(ft, false)) {
				field.setByte(obj, (Byte) val);
			} else if (isShort(ft, false)) {
				field.setShort(obj, (Short) val);
			} else if (isCharacter(ft, false)) {
				field.setChar(obj, (Character) val);
			} else {
				field.set(obj, val);
			}
		} catch (Exception e) {
			String valClsName = (val != null) ? val.getClass().getSimpleName()
					: "?";
			L.w("Error assigning <%s> %s to (%s) field %s#%s: %s.", valClsName,
					val, field.getType().getSimpleName(), obj.getClass()
							.getSimpleName(), field.getName(), e.getMessage());
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

	public static <T> T newInstance(Class<T> cls)
			throws IllegalArgumentException {
		try {
			return cls.newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static Enum<?> newEnum(Class<?> enumClass, String enumStr) {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		Enum en = Enum.valueOf(enumClass.asSubclass(Enum.class), enumStr);
		return en;
	}

	public static ArrayList<Class<?>> buildClassHierarchy(Class<?> cls) {
		ArrayList<Class<?>> hierarhy = new ArrayList<Class<?>>();
		boolean enteredDroidParts = false;
		do {
			hierarhy.add(0, cls);
			boolean inDroidParts = cls.getName().startsWith("org.droidparts");
			if (enteredDroidParts && !inDroidParts) {
				break;
			} else {
				enteredDroidParts = inDroidParts;
				cls = cls.getSuperclass();
			}
		} while (cls != null);
		return hierarhy;
	}

	public static Class<?> getArrayComponentType(Class<?> arrCls) {
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
					varArgs = Arrays2.toObjectArray(firstArg);
				}
			}
		}
		return varArgs;
	}

}
