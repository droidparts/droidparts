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

import static org.droidparts.util.Strings.isEmpty;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.droidparts.Injector;
import org.droidparts.contract.Constants.ManifestMeta;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

public class L {

	public static void v(Object obj) {
		if (isLoggable(VERBOSE)) {
			log(VERBOSE, obj);
		}
	}

	public static void v(String format, Object... args) {
		if (isLoggable(VERBOSE)) {
			log(VERBOSE, format, args);
		}
	}

	public static void d(Object obj) {
		if (isLoggable(DEBUG)) {
			log(DEBUG, obj);
		}
	}

	public static void d(String format, Object... args) {
		if (isLoggable(DEBUG)) {
			log(DEBUG, format, args);
		}
	}

	public static void i(Object obj) {
		if (isLoggable(INFO)) {
			log(INFO, obj);
		}
	}

	public static void i(String format, Object... args) {
		if (isLoggable(INFO)) {
			log(INFO, format, args);
		}
	}

	public static void w(Object obj) {
		if (isLoggable(WARN)) {
			log(WARN, obj);
		}
	}

	public static void w(String format, Object... args) {
		if (isLoggable(WARN)) {
			log(WARN, format, args);
		}
	}

	public static void e(Object obj) {
		if (isLoggable(ERROR)) {
			log(ERROR, obj);
		}
	}

	public static void e(String format, Object... args) {
		if (isLoggable(ERROR)) {
			log(ERROR, format, args);
		}
	}

	public static void wtf(Object obj) {
		if (isLoggable(ASSERT)) {
			log(ASSERT, obj);
		}
	}

	public static void wtf(String format, Object... args) {
		if (isLoggable(ASSERT)) {
			log(ASSERT, format, args);
		}
	}

	public static void wtf() {
		if (isLoggable(ASSERT)) {
			log(ASSERT, "WTF");
		}
	}

	public static boolean isLoggable(int level) {
		boolean debug = isDebug();
		boolean log = debug || (!debug && level >= getLogLevel());
		return log;
	}

	public static final int VERBOSE = Log.VERBOSE;
	public static final int DEBUG = Log.DEBUG;
	public static final int INFO = Log.INFO;
	public static final int WARN = Log.WARN;
	public static final int ERROR = Log.ERROR;
	public static final int ASSERT = Log.ASSERT;
	private static final int DISABLE = 1024;

	private static final String TAG = "DroidParts";

	private static void log(int priority, Object obj) {
		String msg;
		if (obj instanceof Throwable) {
			StringWriter sw = new StringWriter();
			((Throwable) obj).printStackTrace(new PrintWriter(sw));
			msg = sw.toString();
		} else {
			msg = String.valueOf(obj);
			if (isEmpty(msg)) {
				msg = "\"\"";
			}
		}
		Log.println(priority, getTag(isDebug()), msg);
	}

	private static void log(int priority, String format, Object... args) {
		try {
			String msg = String.format(format, args);
			Log.println(priority, getTag(isDebug()), msg);
		} catch (Exception e) {
			e(e);
		}
	}

	private static boolean isDebug() {
		if (_debug == null) {
			Context ctx = Injector.getApplicationContext();
			if (ctx != null) {
				_debug = (ctx.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
			}
		}
		return (_debug != null) ? _debug : true;
	}

	private static int getLogLevel() {
		if (_logLevel == 0) {
			Context ctx = Injector.getApplicationContext();
			if (ctx != null) {
				String logLevelStr = null;
				try {
					Bundle metaData = ctx.getPackageManager()
							.getApplicationInfo(ctx.getPackageName(),
									PackageManager.GET_META_DATA).metaData;
					logLevelStr = metaData.getString(ManifestMeta.LOG_LEVEL)
							.toLowerCase().trim();
				} catch (Exception e) {
					// pass
				}
				if (ManifestMeta.VERBOSE.equals(logLevelStr)) {
					_logLevel = VERBOSE;
				} else if (ManifestMeta.DEBUG.equals(logLevelStr)) {
					_logLevel = DEBUG;
				} else if (ManifestMeta.INFO.equals(logLevelStr)) {
					_logLevel = INFO;
				} else if (ManifestMeta.WARN.equals(logLevelStr)) {
					_logLevel = WARN;
				} else if (ManifestMeta.ERROR.equals(logLevelStr)) {
					_logLevel = ERROR;
				} else if (ManifestMeta.ASSERT.equals(logLevelStr)) {
					_logLevel = ASSERT;
				} else if (ManifestMeta.DISABLE.equals(logLevelStr)) {
					_logLevel = DISABLE;
				} else {
					_logLevel = DISABLE;
					Log.i(TAG,
							"No valid <meta-data android:name=\""
									+ ManifestMeta.LOG_LEVEL
									+ "\" android:value=\"...\"/> in AndroidManifest.xml. Logging disabled.");
				}
			}
		}
		return (_logLevel != 0) ? _logLevel : DISABLE;
	}

	private static String getTag(boolean debug) {
		if (debug) {
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
			if (_tag == null) {
				Context ctx = Injector.getApplicationContext();
				if (ctx != null) {
					_tag = ctx.getPackageName();
				}
			}
			return (_tag != null) ? _tag : TAG;
		}
	}

	private static Boolean _debug;
	private static int _logLevel;
	private static String _tag;

	protected L() {
	}

}