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
package org.droidparts.util;

import java.lang.reflect.Array;

public class Arrays2 {

	public static Object[] toObjectArray(Object someArr) {
		Class<?> arrCls = someArr.getClass();
		if (arrCls == byte[].class) {
			return toObject((byte[]) someArr);
		} else if (arrCls == short[].class) {
			return toObject((short[]) someArr);
		} else if (arrCls == int[].class) {
			return toObject((int[]) someArr);
		} else if (arrCls == long[].class) {
			return toObject((long[]) someArr);
		} else if (arrCls == float[].class) {
			return toObject((float[]) someArr);
		} else if (arrCls == double[].class) {
			return toObject((double[]) someArr);
		} else if (arrCls == boolean[].class) {
			return toObject((boolean[]) someArr);
		} else if (arrCls == char[].class) {
			return toObject((char[]) someArr);
		} else {
			return (Object[]) someArr;
		}
	}

	public static byte[] toPrimitive(Byte[] arr) {
		return (byte[]) convertArray(byte.class, arr.length, arr);
	}

	public static Byte[] toObject(byte[] arr) {
		return (Byte[]) convertArray(Byte.class, arr.length, arr);
	}

	public static short[] toPrimitive(Short[] arr) {
		return (short[]) convertArray(short.class, arr.length, arr);
	}

	public static Short[] toObject(short[] arr) {
		return (Short[]) convertArray(Short.class, arr.length, arr);
	}

	public static int[] toPrimitive(Integer[] arr) {
		return (int[]) convertArray(int.class, arr.length, arr);
	}

	public static Integer[] toObject(int[] arr) {
		return (Integer[]) convertArray(Integer.class, arr.length, arr);
	}

	public static long[] toPrimitive(Long[] arr) {
		return (long[]) convertArray(long.class, arr.length, arr);
	}

	public static Long[] toObject(long[] arr) {
		return (Long[]) convertArray(Long.class, arr.length, arr);
	}

	public static float[] toPrimitive(Float[] arr) {
		return (float[]) convertArray(float.class, arr.length, arr);
	}

	public static Float[] toObject(float[] arr) {
		return (Float[]) convertArray(Float.class, arr.length, arr);
	}

	public static double[] toPrimitive(Double[] arr) {
		return (double[]) convertArray(double.class, arr.length, arr);
	}

	public static Double[] toObject(double[] arr) {
		return (Double[]) convertArray(Double.class, arr.length, arr);
	}

	public static boolean[] toPrimitive(Boolean[] arr) {
		return (boolean[]) convertArray(boolean.class, arr.length, arr);
	}

	public static Boolean[] toObject(boolean[] arr) {
		return (Boolean[]) convertArray(Boolean.class, arr.length, arr);
	}

	public static char[] toPrimitive(Character[] arr) {
		return (char[]) convertArray(char.class, arr.length, arr);
	}

	public static Character[] toObject(char[] arr) {
		return (Character[]) convertArray(Character.class, arr.length, arr);
	}

	private static Object convertArray(Class<?> componentType, int size,
			Object arr) {
		Object arr2 = Array.newInstance(componentType, size);
		for (int i = 0; i < size; i++) {
			Object val = Array.get(arr, i);
			Array.set(arr2, i, val);
		}
		return arr2;
	}

}
