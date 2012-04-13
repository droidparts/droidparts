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

import static android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE;
import static android.util.Log.DEBUG;
import static android.util.Log.ERROR;
import static android.util.Log.INFO;
import static android.util.Log.VERBOSE;
import static android.util.Log.WARN;

import org.droidparts.inject.Injector;

import android.content.Context;
import android.content.pm.ApplicationInfo;
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

	public static void wtf(Object msg) {
		log(ERROR, "WTF: " + msg);
	}

	private static void log(int priority, Object msg) {
		Log.println(priority, getTag(), String.valueOf(msg));
	}

	private static String getTag() {
		if (debug == null) {
			Context ctx = Injector.get().getApplicationContext();
			if (ctx != null) {
				ApplicationInfo appInfo = ctx.getApplicationInfo();
				debug = (appInfo.flags &= FLAG_DEBUGGABLE) != 0;
			}
		}
		StackTraceElement caller = Thread.currentThread().getStackTrace()[5];
		String className = caller.getFileName().substring(0,
				caller.getFileName().length() - 5);
		if (debug != null && debug) {
			StringBuilder sb = new StringBuilder(5);
			sb.append(className);
			sb.append(".");
			sb.append(caller.getMethodName());
			sb.append("():");
			sb.append(caller.getLineNumber());
			return sb.toString();
		} else {
			return className;
		}
	}

	private static Boolean debug;

	private L() {
	}

}