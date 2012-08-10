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

import java.util.Arrays;
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

	public static boolean isArray(Class<?> cls) {
		return cls.isArray();
	}

	public static boolean isByteArray(Class<?> cls) {
		return cls == byte[].class;
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

	public static Object[] toObjectArr(Class<?> valueCls, Object value) {
		// as autoboxing won't work for Arrays.asList(int[] value)
		Object[] arr;
		if (valueCls == boolean[].class) {
			boolean[] tArr = (boolean[]) value;
			arr = new Object[tArr.length];
			for (int i = 0; i < arr.length; i++) {
				arr[i] = tArr[i];
			}
		} else if (valueCls == byte[].class) {
			byte[] tArr = (byte[]) value;
			arr = new Object[tArr.length];
			for (int i = 0; i < arr.length; i++) {
				arr[i] = tArr[i];
			}
		} else if (valueCls == double[].class) {
			Arrays.asList(value);
			double[] tArr = (double[]) value;
			arr = new Object[tArr.length];
			for (int i = 0; i < arr.length; i++) {
				arr[i] = tArr[i];
			}
		} else if (valueCls == float[].class) {
			float[] tArr = (float[]) value;
			arr = new Object[tArr.length];
			for (int i = 0; i < arr.length; i++) {
				arr[i] = tArr[i];
			}
		} else if (valueCls == int[].class) {
			int[] tArr = (int[]) value;
			arr = new Object[tArr.length];
			for (int i = 0; i < arr.length; i++) {
				arr[i] = tArr[i];
			}
		} else if (valueCls == long[].class) {
			long[] tArr = (long[]) value;
			arr = new Object[tArr.length];
			for (int i = 0; i < arr.length; i++) {
				arr[i] = tArr[i];
			}
		} else if (valueCls == short[].class) {
			short[] tArr = (short[]) value;
			arr = new Object[tArr.length];
			for (int i = 0; i < arr.length; i++) {
				arr[i] = tArr[i];
			}
		} else {
			// XXX
			arr = (Object[]) value;
		}
		return arr;
	}

	public static Object toTypeArr(Class<?> valueCls, String[] arr) {
		if (valueCls == boolean[].class) {
			boolean[] tArr = new boolean[arr.length];
			for (int i = 0; i < arr.length; i++) {
				tArr[i] = Boolean.valueOf(arr[i]);
			}
			return tArr;
		} else if (valueCls == byte[].class) {
			byte[] tArr = new byte[arr.length];
			for (int i = 0; i < arr.length; i++) {
				tArr[i] = Byte.valueOf(arr[i]);
			}
			return tArr;
		} else if (valueCls == double[].class) {
			double[] tArr = new double[arr.length];
			for (int i = 0; i < arr.length; i++) {
				tArr[i] = Double.valueOf(arr[i]);
			}
			return tArr;
		} else if (valueCls == float[].class) {
			float[] tArr = new float[arr.length];
			for (int i = 0; i < arr.length; i++) {
				tArr[i] = Float.valueOf(arr[i]);
			}
			return tArr;
		} else if (valueCls == int[].class) {
			int[] tArr = new int[arr.length];
			for (int i = 0; i < arr.length; i++) {
				tArr[i] = Integer.valueOf(arr[i]);
			}
			return tArr;
		} else if (valueCls == long[].class) {
			long[] tArr = new long[arr.length];
			for (int i = 0; i < arr.length; i++) {
				tArr[i] = Long.valueOf(arr[i]);
			}
			return tArr;
		} else if (valueCls == short[].class) {
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

}
