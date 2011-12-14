/**
 * Copyright 2011 Alex Yanchenko
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

import static android.util.Log.DEBUG;
import static android.util.Log.ERROR;
import static android.util.Log.INFO;
import static android.util.Log.VERBOSE;
import static android.util.Log.WARN;
import android.util.Log;

public class L {

	public static void v(Object msg) {
		log(VERBOSE, msg);
	}

	public static void d(Object msg) {
		log(DEBUG, msg);
	}

	public static void i(Object msg) {
		log(INFO, msg);
	}

	public static void w(Object msg) {
		log(WARN, msg);
	}

	public static void e(Object msg) {
		log(ERROR, msg);
	}

	private static void log(int priority, Object msg) {
		Log.println(priority, getTag(), String.valueOf(msg));
	}

	private static String getTag() {
		// TODO
		return "";
	}

	private L() {
	};

}