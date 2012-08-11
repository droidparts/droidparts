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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.UUID;

import org.droidparts.model.Entity;
import org.droidparts.model.Model;
import org.droidparts.util.Arrays2;
import org.droidparts.util.L;
import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public final class TypeHelper {

	private TypeHelper() {
	}

	public static boolean isBoolean(Class<?> cls) {
		return cls == Boolean.class || cls == boolean.class;
	}

	public static boolean isByte(Class<?> cls) {
		return cls == Byte.class || cls == byte.class;
	}

	public static boolean isDouble(Class<?> cls) {
		return cls == Double.class || cls == double.class;
	}

	public static boolean isFloat(Class<?> cls) {
		return cls == Float.class || cls == float.class;
	}

	public static boolean isInteger(Class<?> cls) {
		return cls == Integer.class || cls == int.class;
	}

	public static boolean isLong(Class<?> cls) {
		return cls == Long.class || cls == long.class;
	}

	public static boolean isShort(Class<?> cls) {
		return cls == Short.class || cls == short.class;
	}

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

	public static Object[] toObjectArr(Object primitiveArr) {
		// as autoboxing won't work for Arrays.asList(int[] value)
		Class<?> arrCls = primitiveArr.getClass();
		Object[] arr;
		if (arrCls == boolean[].class) {
			arr = Arrays2.toObject((boolean[]) primitiveArr);
		} else if (arrCls == double[].class) {
			arr = Arrays2.toObject((double[]) primitiveArr);
		} else if (arrCls == float[].class) {
			arr = Arrays2.toObject((float[]) primitiveArr);
		} else if (arrCls == int[].class) {
			arr = Arrays2.toObject((int[]) primitiveArr);
		} else if (arrCls == long[].class) {
			arr = Arrays2.toObject((long[]) primitiveArr);
		} else if (arrCls == short[].class) {
			arr = Arrays2.toObject((short[]) primitiveArr);
		} else {
			// XXX
			arr = (Object[]) primitiveArr;
		}
		return arr;
	}

	public static Object toTypeArr(Class<?> arrCls, String[] arr) {
		if (arrCls == boolean[].class) {
			boolean[] tArr = new boolean[arr.length];
			for (int i = 0; i < arr.length; i++) {
				tArr[i] = Boolean.valueOf(arr[i]);
			}
			return tArr;
		} else if (arrCls == double[].class) {
			double[] tArr = new double[arr.length];
			for (int i = 0; i < arr.length; i++) {
				tArr[i] = Double.valueOf(arr[i]);
			}
			return tArr;
		} else if (arrCls == float[].class) {
			float[] tArr = new float[arr.length];
			for (int i = 0; i < arr.length; i++) {
				tArr[i] = Float.valueOf(arr[i]);
			}
			return tArr;
		} else if (arrCls == int[].class) {
			int[] tArr = new int[arr.length];
			for (int i = 0; i < arr.length; i++) {
				tArr[i] = Integer.valueOf(arr[i]);
			}
			return tArr;
		} else if (arrCls == long[].class) {
			long[] tArr = new long[arr.length];
			for (int i = 0; i < arr.length; i++) {
				tArr[i] = Long.valueOf(arr[i]);
			}
			return tArr;
		} else if (arrCls == short[].class) {
			short[] tArr = new short[arr.length];
			for (int i = 0; i < arr.length; i++) {
				tArr[i] = Short.valueOf(arr[i]);
			}
			return tArr;
		} else {
			// XXX
			return arr;
		}
	}

	public static Class<?>[] getGenericArgs(Field field) {
		Type genericType = field.getGenericType();
		if (genericType instanceof ParameterizedType) {
			Type[] typeArr = ((ParameterizedType) genericType)
					.getActualTypeArguments();
			Class<?>[] classArr = new Class<?>[typeArr.length];
			for (int i = 0; i < typeArr.length; i++) {
				String[] parts = typeArr[i].toString().split(" ");
				String className = parts[parts.length - 1];
				try {
					classArr[i] = Class.forName(className);
				} catch (ClassNotFoundException e) {
					L.w(e);
				}
			}
			return classArr;
		} else {
			return new Class<?>[0];
		}
	}

	public static Class<?> getArrayType(Class<?> arrCls) throws Exception {
		String name = arrCls.getName();
		name = name.substring(2, name.length() - 1);
		return Class.forName(name);
	}

}
