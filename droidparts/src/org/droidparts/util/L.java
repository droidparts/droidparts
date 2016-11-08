/**
 * Copyright 2016 Alex Yanchenko
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

import static org.droidparts.inner.ManifestMetaData.LOG_LEVEL;
import static org.droidparts.inner.TypeHelper.isArray;
import static org.droidparts.inner.TypeHelper.isString;
import static org.droidparts.inner.TypeHelper.isThrowable;
import static org.droidparts.util.Strings.isEmpty;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import org.droidparts.Injector;
import org.droidparts.inner.ManifestMetaData;
import org.droidparts.inner.ManifestMetaData.LogLevel;

import android.content.Context;
import android.util.Log;

public class L {

	public interface Listener {
		void onMessageLogged(int priority, String tag, String msg);
	}

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
		return (level >= getLogLevel());
	}

	public static final int VERBOSE = Log.VERBOSE;
	public static final int DEBUG = Log.DEBUG;
	public static final int INFO = Log.INFO;
	public static final int WARN = Log.WARN;
	public static final int ERROR = Log.ERROR;
	public static final int ASSERT = Log.ASSERT;
	private static final int DISABLE = 1024;

	public static void setListener(Listener listener) {
		L.listener = listener;
	}

	private static Listener listener;

	private static final String DEFAULT_TAG = "DroidParts";

	private static void log(int priority, Object obj) {
		String msg = "null";
		if (obj != null) {
			Class<?> cls = obj.getClass();
			if (isString(cls)) {
				msg = (String) obj;
				if (isEmpty(msg)) {
					msg = "\"\"";
				}
			} else if (isThrowable(cls)) {
				StringWriter sw = new StringWriter();
				((Throwable) obj).printStackTrace(new PrintWriter(sw));
				msg = sw.toString();
			} else if (isArray(cls)) {
				msg = Arrays.toString(Arrays2.toObjectArray(obj));
			} else {
				msg = obj.toString();
			}
		}
		log(msg, priority);
	}

	private static void log(int priority, String format, Object... args) {
		String msg;
		try {
			msg = String.format(format, args);
		} catch (Exception e) {
			e(e);
			return;
		}
		log(msg, priority);
	}

	private static void log(String msg, int priority) {
		String tag = getTag(isDebug());
		if (listener != null) {
			listener.onMessageLogged(priority, tag, msg);
		}
		Log.println(priority, tag, msg);
	}

	private static boolean isDebug() {
		if (_debug == 0) {
			Context ctx = Injector.getApplicationContext();
			if (ctx != null) {
				_debug = AppUtils.isDebuggable(ctx) ? 1 : -1;
			} else {
				return true;
			}
		}
		return (_debug == 1);
	}

	private static int getLogLevel() {
		if (_logLevel == 0) {
			Context ctx = Injector.getApplicationContext();
			if (ctx != null) {
				String logLevelStr = ManifestMetaData.get(ctx, LOG_LEVEL);
				if (LogLevel.VERBOSE.equalsIgnoreCase(logLevelStr)) {
					_logLevel = VERBOSE;
				} else if (LogLevel.DEBUG.equalsIgnoreCase(logLevelStr)) {
					_logLevel = DEBUG;
				} else if (LogLevel.INFO.equalsIgnoreCase(logLevelStr)) {
					_logLevel = INFO;
				} else if (LogLevel.WARN.equalsIgnoreCase(logLevelStr)) {
					_logLevel = WARN;
				} else if (LogLevel.ERROR.equalsIgnoreCase(logLevelStr)) {
					_logLevel = ERROR;
				} else if (LogLevel.ASSERT.equalsIgnoreCase(logLevelStr)) {
					_logLevel = ASSERT;
				} else if (LogLevel.DISABLE.equalsIgnoreCase(logLevelStr)) {
					_logLevel = DISABLE;
				} else {
					_logLevel = VERBOSE;
					Log.i(DEFAULT_TAG, "No valid <meta-data android:name=\"" + ManifestMetaData.LOG_LEVEL
							+ "\" android:value=\"...\"/> in AndroidManifest.xml.");
				}
			}
		}
		return (_logLevel != 0) ? _logLevel : VERBOSE;
	}

	private static String getTag(boolean debug) {
		if (debug) {
			StackTraceElement caller = Thread.currentThread().getStackTrace()[6];
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
			return (_tag != null) ? _tag : DEFAULT_TAG;
		}
	}

	private static int _debug;
	private static int _logLevel;
	private static String _tag;

	protected L() {
	}

}