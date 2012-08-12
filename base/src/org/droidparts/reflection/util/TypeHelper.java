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

import static org.droidparts.util.Arrays2.toObject;
import static org.droidparts.util.Arrays2.toPrimitive;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.UUID;

import org.droidparts.model.Entity;
import org.droidparts.model.Model;
import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public final class TypeHelper {

	private TypeHelper() {
	}

	public static boolean isByte(Class<?> cls) {
		return (cls == byte.class || cls == Byte.class);
	}

	public static boolean isShort(Class<?> cls) {
		return (cls == short.class || cls == Short.class);
	}

	public static boolean isInteger(Class<?> cls) {
		return (cls == int.class || cls == Integer.class);
	}

	public static boolean isLong(Class<?> cls) {
		return (cls == long.class || cls == Long.class);
	}

	public static boolean isFloat(Class<?> cls) {
		return (cls == float.class || cls == Float.class);
	}

	public static boolean isDouble(Class<?> cls) {
		return (cls == double.class || cls == Double.class);
	}

	public static boolean isBoolean(Class<?> cls) {
		return (cls == boolean.class || cls == Boolean.class);
	}

	public static boolean isCharacter(Class<?> cls) {
		return (cls == char.class || cls == Character.class);
	}

	//

	public static boolean isString(Class<?> cls) {
		return cls == String.class;
	}

	public static boolean isEnum(Class<?> cls) {
		return cls.isEnum();
	}

	public static boolean isUUID(Class<?> cls) {
		return UUID.class.isAssignableFrom(cls);
	}

	//

	public static boolean isByteArray(Class<?> cls) {
		return cls == byte[].class;
	}

	public static boolean isArray(Class<?> cls) {
		return cls.isArray();
	}

	public static boolean isCollection(Class<?> cls) {
		return Collection.class.isAssignableFrom(cls);
	}

	//

	public static boolean isBitmap(Class<?> cls) {
		return Bitmap.class.isAssignableFrom(cls);
	}

	public static boolean isDrawable(Class<?> cls) {
		return Drawable.class.isAssignableFrom(cls);
	}

	public static boolean isJsonObject(Class<?> cls) {
		return JSONObject.class.isAssignableFrom(cls);
	}

	public static boolean isJsonArray(Class<?> cls) {
		return JSONArray.class.isAssignableFrom(cls);
	}

	//

	public static boolean isModel(Class<?> cls) {
		return Model.class.isAssignableFrom(cls);
	}

	public static boolean isEntity(Class<?> cls) {
		return Entity.class.isAssignableFrom(cls);
	}

	//

	public static Object[] toObjectArr(Class<?> arrCls, Object someArr) {
		// as autoboxing won't work for Arrays.asList(int[] value)
		Object[] arr;
		if (arrCls == byte[].class) {
			arr = toObject((byte[]) someArr);
		} else if (arrCls == short[].class) {
			arr = toObject((short[]) someArr);
		} else if (arrCls == int[].class) {
			arr = toObject((int[]) someArr);
		} else if (arrCls == long[].class) {
			arr = toObject((long[]) someArr);
		} else if (arrCls == float[].class) {
			arr = toObject((float[]) someArr);
		} else if (arrCls == double[].class) {
			arr = toObject((double[]) someArr);
		} else if (arrCls == boolean[].class) {
			arr = toObject((boolean[]) someArr);
		} else if (arrCls == char[].class) {
			arr = toObject((char[]) someArr);
		} else {
			// out of primitives
			arr = (Object[]) someArr;
		}
		return arr;
	}

	public static Object toTypeArr(Class<?> arrCls, String[] arr) {
		if (arrCls == byte[].class || arrCls == Byte[].class) {
			Byte[] tArr = new Byte[arr.length];
			for (int i = 0; i < arr.length; i++) {
				tArr[i] = Byte.valueOf(arr[i]);
			}
			return (arrCls == byte[].class) ? toPrimitive(tArr) : tArr;
		} else if (arrCls == short[].class || arrCls == Short[].class) {
			Short[] tArr = new Short[arr.length];
			for (int i = 0; i < arr.length; i++) {
				tArr[i] = Short.valueOf(arr[i]);
			}
			return (arrCls == short[].class) ? toPrimitive(tArr) : tArr;
		} else if (arrCls == int[].class || arrCls == Integer[].class) {
			Integer[] tArr = new Integer[arr.length];
			for (int i = 0; i < arr.length; i++) {
				tArr[i] = Integer.valueOf(arr[i]);
			}
			return (arrCls == int[].class) ? toPrimitive(tArr) : tArr;
		} else if (arrCls == long[].class || arrCls == Long[].class) {
			Long[] tArr = new Long[arr.length];
			for (int i = 0; i < arr.length; i++) {
				tArr[i] = Long.valueOf(arr[i]);
			}
			return (arrCls == long[].class) ? toPrimitive(tArr) : tArr;
		} else if (arrCls == float[].class || arrCls == Float[].class) {
			Float[] tArr = new Float[arr.length];
			for (int i = 0; i < arr.length; i++) {
				tArr[i] = Float.valueOf(arr[i]);
			}
			return (arrCls == float[].class) ? toPrimitive(tArr) : tArr;
		} else if (arrCls == double[].class || arrCls == Double[].class) {
			Double[] tArr = new Double[arr.length];
			for (int i = 0; i < arr.length; i++) {
				tArr[i] = Double.valueOf(arr[i]);
			}
			return (arrCls == double[].class) ? toPrimitive(tArr) : tArr;
		} else if (arrCls == boolean[].class || arrCls == Boolean[].class) {
			Boolean[] tArr = new Boolean[arr.length];
			for (int i = 0; i < arr.length; i++) {
				tArr[i] = Boolean.valueOf(arr[i]);
			}
			return (arrCls == boolean[].class) ? toPrimitive(tArr) : tArr;
		} else if (arrCls == char[].class || arrCls == Character[].class) {
			Character[] tArr = new Character[arr.length];
			for (int i = 0; i < arr.length; i++) {
				String str = arr[i];
				tArr[i] = (str.length() == 0) ? ' ' : str.charAt(0);
			}
			return (arrCls == char[].class) ? toPrimitive(tArr) : tArr;
		} else if (arrCls == String[].class) {
			return arr;
		} else {
			throw new IllegalArgumentException("Unable to convert to" + arrCls);
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

	public static Class<?> getArrayType(Class<?> arrCls) {
		String clsName = arrCls.getName();
		if (clsName.length() == 2) {
			// primitives - [Z
			clsName = clsName.substring(1);
			if ("B".equals(clsName)) {
				return byte.class;
			} else if ("S".equals(clsName)) {
				return short.class;
			} else if ("I".equals(clsName)) {
				return int.class;
			} else if ("J".equals(clsName)) {
				return long.class;
			} else if ("F".equals(clsName)) {
				return float.class;
			} else if ("D".equals(clsName)) {
				return double.class;
			} else if ("Z".equals(clsName)) {
				return boolean.class;
			} else if ("C".equals(clsName)) {
				return char.class;
			} else {
				throw new IllegalArgumentException("Alien primitive: "
						+ clsName);
			}
		} else {
			// objects - [Ljava.lang.String;
			clsName = clsName.substring(2, clsName.length() - 1);
			return classForName(clsName);
		}
	}

	private static Class<?> classForName(String clsName)
			throws IllegalArgumentException {
		try {
			return Class.forName(clsName);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
