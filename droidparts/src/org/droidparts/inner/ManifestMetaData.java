/**
 * Copyright 2017 Alex Yanchenko
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
package org.droidparts.inner;

import android.content.Context;

import static android.content.pm.PackageManager.GET_META_DATA;

public class ManifestMetaData {

	public static final String DEPENDENCY_PROVIDER = "droidparts_dependency_provider";
	public static final String LOG_LEVEL = "droidparts_log_level";

	public interface LogLevel {

		String DISABLE = "disable";
		String VERBOSE = "verbose";
		String DEBUG = "debug";
		String INFO = "info";
		String WARN = "warn";
		String ERROR = "error";
		String ASSERT = "assert";

	}

	public static String get(Context ctx, String key) {
		try {
			return ctx.getPackageManager().getApplicationInfo(ctx.getPackageName(), GET_META_DATA).metaData
					.getString(key);
		} catch (Exception e) {
			return null;
		}
	}

	private ManifestMetaData() {
	}

}