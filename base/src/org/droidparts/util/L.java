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

import org.droidparts.contract.Constants.ManifestMeta;
import org.droidparts.inject.Injector;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
		log(ASSERT, obj);
	}

	public static void wtf() {
		log(ASSERT, "WTF");
	}

	private static final String TAG = "DroidParts";

	private static final int VERBOSE = Log.VERBOSE;
	private static final int DEBUG = Log.DEBUG;
	private static final int INFO = Log.INFO;
	private static final int WARN = Log.WARN;
	private static final int ERROR = Log.ERROR;
	private static final int ASSERT = Log.ASSERT;
	private static final int DISABLE = 1024;

	private static final int DEFAULT = DISABLE;

	private static void log(int priority, Object obj) {
		boolean debug = isDebug();
		if (debug || (!debug && priority >= getLogLevel())) {
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
			Log.println(priority, getTag(debug), msg);
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
					_logLevel = DEFAULT;
					Log.i(TAG,
							"No valid <meta-data android:name=\""
									+ ManifestMeta.LOG_LEVEL
									+ "\" android:value=\"...\"/> in AndroidManifest.xml. Logging disabled.");
				}
			}
		}
		return (_logLevel != 0) ? _logLevel : DEFAULT;
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