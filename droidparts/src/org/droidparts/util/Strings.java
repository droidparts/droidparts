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
package org.droidparts.util;

import java.util.Collection;

public class Strings {

	public static boolean isNotEmpty(CharSequence str) {
		return !isEmpty(str);
	}

	public static boolean isEmpty(CharSequence str) {
		return str == null || str.length() == 0;
	}

	public static <T> String join(Collection<T> coll, String separator,
			String terminator) {
		return join(coll.toArray(new Object[coll.size()]), separator,
				terminator);
	}

	public static String join(Object[] arr, String separator, String terminator) {
		StringBuilder sb = new StringBuilder(arr.length * 2);
		for (int i = 0; i < arr.length; i++) {
			sb.append(arr[i]);
			if (i < arr.length - 1) {
				sb.append(separator);
			} else if (terminator != null && arr.length > 0) {
				sb.append(terminator);
			}
		}
		return sb.toString();
	}

}
