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

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
import static android.content.pm.PackageManager.DONT_KILL_APP;
import static android.content.pm.PackageManager.GET_META_DATA;
import static android.provider.Settings.Secure.ANDROID_ID;
import static org.droidparts.contract.Constants.BUFFER_SIZE;
import static org.droidparts.util.IOUtils.silentlyClose;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.provider.Settings.Secure;

public class AppUtils {

	protected final Context ctx;

	public AppUtils(Context ctx) {
		this.ctx = ctx;
	}

	public String getDeviceId() {
		return Secure.getString(ctx.getContentResolver(), ANDROID_ID);
	}

	public String getVersionName() {
		String verName = "?";
		try {
			verName = ctx.getPackageManager().getPackageInfo(
					ctx.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			L.w(e);
		}
		return verName;
	}

	public boolean gotActivityForIntent(Intent intent) {
		return ctx.getPackageManager().resolveActivity(intent, 0) != null;
	}

	public boolean isInstalled(String pkgName) {
		try {
			ctx.getPackageManager().getApplicationInfo(pkgName, GET_META_DATA);
			return true;
		} catch (NameNotFoundException e) {
			return false;
		}
	}

	public void setComponentEnabled(Class<? extends Context> component,
			boolean visible) {
		PackageManager pm = ctx.getPackageManager();
		ComponentName componentName = new ComponentName(ctx, component);
		int state = visible ? COMPONENT_ENABLED_STATE_ENABLED
				: COMPONENT_ENABLED_STATE_DISABLED;
		pm.setComponentEnabledSetting(componentName, state, DONT_KILL_APP);
	}

	public String readStringResource(int resId) throws IOException {
		InputStream is = null;
		BufferedReader reader = null;
		try {
			is = ctx.getResources().openRawResource(resId);
			reader = new BufferedReader(new InputStreamReader(is), BUFFER_SIZE);
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			return sb.toString();
		} finally {
			silentlyClose(reader, is);
		}
	}

}
