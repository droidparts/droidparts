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

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
import static android.content.pm.PackageManager.DONT_KILL_APP;
import static android.content.pm.PackageManager.GET_META_DATA;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class AppUtils {

	protected final ContextWrapper ctx;

	public AppUtils(ContextWrapper ctx) {
		this.ctx = ctx;
	}

	public String getVersionName() {
		String verName = "?";
		try {
			verName = ctx.getPackageManager().getPackageInfo(
					ctx.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			L.e(e);
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

	public static void setComponentEnabled(Context ctx,
			Class<? extends Context> component, boolean visible) {
		PackageManager pm = ctx.getPackageManager();
		ComponentName componentName = new ComponentName(ctx, component);
		int state = visible ? COMPONENT_ENABLED_STATE_ENABLED
				: COMPONENT_ENABLED_STATE_DISABLED;
		pm.setComponentEnabledSetting(componentName, state, DONT_KILL_APP);
	}

}
