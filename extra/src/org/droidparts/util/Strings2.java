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
package org.droidparts.util;

import java.util.Collection;

public class Strings2 extends Strings {

	public static String toEnumeration(Collection<CharSequence> coll,
			boolean terminateWithDot) {
		return toEnumeration(coll.toArray(new CharSequence[coll.size()]),
				terminateWithDot);
	}

	public static String toEnumeration(CharSequence[] arr,
			boolean terminateWithDot) {
		String end = terminateWithDot ? "." : null;
		return toEnumeration(arr, ", ", end);
	}

	public static String toEnumeration(Collection<CharSequence> coll,
			String separator, String terminator) {
		return toEnumeration(coll.toArray(new CharSequence[coll.size()]),
				separator, terminator);
	}

	protected Strings2() {
	}

}
