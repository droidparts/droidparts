/**
 * Copyright 2014 Alex Yanchenko
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
package org.droidparts.inner.reader;

import static org.droidparts.util.Strings.isEmpty;

import java.util.HashMap;

import org.droidparts.util.L;

import android.content.Context;

public class SystemServiceReader {

	static Object readVal(Context ctx, String serviceName, Class<?> valType)
			throws Exception {
		String name = isEmpty(serviceName) ? serviceRegistry.get(valType)
				: serviceName;
		if (name == null) {
			throw new Exception("Unknown service: " + name);
		} else {
			return ctx.getSystemService(name);
		}
	}

	private static final HashMap<Class<?>, String> serviceRegistry = new HashMap<Class<?>, String>();

	static {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("power", "android.os.PowerManager");
		map.put("window", "android.view.WindowManager");
		map.put("layout_inflater", "android.view.LayoutInflater");
		map.put("account", "android.accounts.AccountManager");
		map.put("activity", "android.app.ActivityManager");
		map.put("alarm", "android.app.AlarmManager");
		map.put("notification", "android.app.NotificationManager");
		map.put("accessibility",
				"android.view.accessibility.AccessibilityManager");
		map.put("keyguard", "android.app.KeyguardManager");
		map.put("location", "android.location.LocationManager");
		map.put("country_detector", "android.location.CountryDetector");
		map.put("search", "android.app.SearchManager");
		map.put("sensor", "android.hardware.SensorManager");
		map.put("storage", "android.os.storage.StorageManager");
		map.put("wallpaper", "android.app.WallpaperManager");
		map.put("vibrator", "android.os.Vibrator");
		map.put("statusbar", "android.app.StatusBarManager");
		map.put("connectivity", "android.net.ConnectivityManager");
		map.put("throttle", "android.net.ThrottleManager");
		map.put("updatelock", "android.os.IUpdateLock");
		map.put("wifi", "android.net.wifi.WifiManager");
		map.put("wifip2p", "android.net.wifi.p2p.WifiP2pManager");
		map.put("servicediscovery", "android.net.nsd.NsdManager");
		map.put("audio", "android.media.AudioManager");
		map.put("media_router", "android.media.MediaRouter");
		map.put("phone", "android.telephony.TelephonyManager");
		map.put("clipboard", "android.text.ClipboardManager");
		map.put("input_method", "android.view.inputmethod.InputMethodManager");
		map.put("textservices", "android.view.textservice.TextServicesManager");
		map.put("appwidget", "android.appwidget.AppWidgetManager");
		map.put("backup", "android.app.backup.IBackupManager");
		map.put("dropbox", "android.os.DropBoxManager");
		map.put("device_policy", "android.app.admin.DevicePolicyManager");
		map.put("uimode", "android.app.UiModeManager");
		map.put("download", "android.app.DownloadManager");
		map.put("nfc", "android.nfc.NfcManager");
		map.put("bluetooth", "android.bluetooth.BluetoothAdapter");
		map.put("sip", "android.net.sip.SipManager");
		map.put("usb", "android.hardware.usb.UsbManager");
		map.put("serial", "android.hardware.SerialManager");
		map.put("input", "android.hardware.input.InputManager");
		map.put("display", "android.hardware.display.DisplayManager");
		map.put("scheduling_policy", "android.os.SchedulingPolicyService");
		map.put("user", "android.os.UserManager");

		for (String serviceName : map.keySet()) {
			String clsName = map.get(serviceName);
			try {
				Class<?> cls = Class.forName(clsName);
				serviceRegistry.put(cls, serviceName);
			} catch (ClassNotFoundException e) {
				L.i("%s service not available.", clsName);
			}
		}
	}

}
