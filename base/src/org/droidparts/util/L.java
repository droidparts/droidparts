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

	public static void v(Object obj) {
		log(VERBOSE, obj);
	}

	public static void d(Object obj) {
		log(DEBUG, obj);
	}

	public static void i(Object obj) {
		log(INFO, obj);
	}

	public static void w(Object obj) {
		log(WARN, obj);
	}

	public static void e(Object obj) {
		log(ERROR, obj);
	}

	public static void wtf(Object obj) {
		log(ERROR, "WTF: " + obj);
	}

	private static void log(int priority, Object obj) {
		String msg = (obj instanceof Exception) ? Log
				.getStackTraceString((Exception) obj) : String.valueOf(obj);
		Log.println(priority, getTag(), msg);
	}

	private static String getTag() {
		if (debug == null) {
			Context ctx = Injector.getApplicationContext();
			if (ctx != null) {
				ApplicationInfo appInfo = ctx.getApplicationInfo();
				debug = (appInfo.flags &= FLAG_DEBUGGABLE) != 0;
			}
		}
		if (debug != null && debug) {
			StackTraceElement caller = Thread.currentThread().getStackTrace()[5];
			String c = caller.getClassName();
			String className = c.substring(c.lastIndexOf(".") + 1, c.length());
			StringBuilder sb = new StringBuilder(5);
			sb.append(className);
			sb.append(".");
			sb.append(caller.getMethodName());
			sb.append("():");
			sb.append(caller.getLineNumber());
			return sb.toString();
		} else {
			if (tag == null) {
				Context ctx = Injector.getApplicationContext();
				if (ctx != null) {
					tag = ctx.getPackageName();
				}
			}
			return (tag != null) ? tag : "";
		}
	}

	private static Boolean debug;
	private static String tag;

	private L() {
	}

}