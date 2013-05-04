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

import static android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
import static android.content.pm.PackageManager.DONT_KILL_APP;
import static android.content.pm.PackageManager.GET_META_DATA;
import static android.content.pm.PackageManager.GET_SIGNATURES;
import static android.content.pm.PackageManager.SIGNATURE_MATCH;
import static android.provider.Settings.Secure.ANDROID_ID;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.provider.Settings.Secure;

public class AppUtils {

	protected final Context ctx;

	public AppUtils(Context ctx) {
		this.ctx = ctx.getApplicationContext();
	}

	public boolean isDebuggable() {
		ApplicationInfo appInfo = ctx.getApplicationInfo();
		boolean debug = (appInfo.flags & FLAG_DEBUGGABLE) != 0;
		return debug;
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

	//

	public boolean isInstalled(String pkgName) {
		try {
			ctx.getPackageManager().getApplicationInfo(pkgName, GET_META_DATA);
			return true;
		} catch (NameNotFoundException e) {
			return false;
		}
	}

	public void setComponentEnabled(Class<? extends Context> component,
			boolean enabled) {
		PackageManager pm = ctx.getPackageManager();
		ComponentName componentName = new ComponentName(ctx, component);
		int state = enabled ? COMPONENT_ENABLED_STATE_ENABLED
				: COMPONENT_ENABLED_STATE_DISABLED;
		pm.setComponentEnabledSetting(componentName, state, DONT_KILL_APP);
	}

	//

	public String getDeviceId() {
		return Secure.getString(ctx.getContentResolver(), ANDROID_ID);
	}

	public String getSignature(String pkgName) throws NameNotFoundException {
		PackageInfo pi = ctx.getPackageManager().getPackageInfo(pkgName,
				GET_SIGNATURES);
		String signature = pi.signatures[0].toCharsString();
		return signature;
	}

	public boolean doSignaturesMatch(String pkg1, String pkg2) {
		boolean match = ctx.getPackageManager().checkSignatures(pkg1, pkg2) == SIGNATURE_MATCH;
		return match;
	}

	public boolean canInstallNonMarketApps() {
		return Secure.getInt(ctx.getContentResolver(),
				Secure.INSTALL_NON_MARKET_APPS, 0) != 0;
	}

	public boolean isInstalledFromMarket(String pkgName)
			throws NameNotFoundException {
		String installerPkg = ctx.getPackageManager().getInstallerPackageName(
				pkgName);
		boolean installedFromMarket = "com.google.android.feedback"
				.equals(installerPkg);
		return installedFromMarket;
	}

	public long getClassesDexCrc() {
		ZipFile zf;
		try {
			zf = new ZipFile(ctx.getPackageCodePath());
		} catch (IOException e) {
			L.e(e);
			return -1;
		}
		ZipEntry ze = zf.getEntry("classes.dex");
		long crc = ze.getCrc();
		return crc;
	}

}
