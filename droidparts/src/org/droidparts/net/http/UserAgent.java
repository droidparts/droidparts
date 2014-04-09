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
package org.droidparts.net.http;

import android.os.Build;

public class UserAgent {

	public static String getDefault() {
		return get(null);
	}

	public static String get(String nameHint) {
		return ((nameHint != null) ? nameHint : " DroidParts.org")
				+ " (Android " + Build.VERSION.RELEASE + "; " + Build.MODEL
				+ " Build/" + Build.ID + ")";
	}

	private UserAgent() {
	}

}
