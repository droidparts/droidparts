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
package org.droidparts.inject.injector;

import static org.droidparts.reflect.util.ReflectionUtils.setFieldVal;
import static org.droidparts.util.Strings.isEmpty;

import java.lang.reflect.Field;
import java.util.HashMap;

import org.droidparts.annotation.inject.InjectSystemService;
import org.droidparts.util.L;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.text.ClipboardManager;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

public class SystemServiceInjector {

	static boolean inject(Context ctx, InjectSystemService ann, Object target,
			Field field) {
		String serviceName = ann.value();
		String name = isEmpty(serviceName) ? serviceRegistry.get(field
				.getType()) : serviceName;
		Object serv = ctx.getSystemService(name);
		try {
			setFieldVal(target, field, serv);
			return true;
		} catch (IllegalArgumentException e) {
			// swallow
		}
		return false;
	}

	private static final HashMap<Class<?>, String> serviceRegistry = new HashMap<Class<?>, String>();

	static {
		serviceRegistry.put(AccessibilityService.class,
				Context.ACCESSIBILITY_SERVICE);
		serviceRegistry.put(ActivityManager.class, Context.ACTIVITY_SERVICE);
		serviceRegistry.put(AlarmManager.class, Context.ALARM_SERVICE);
		serviceRegistry.put(AudioManager.class, Context.AUDIO_SERVICE);
		serviceRegistry.put(ClipboardManager.class, Context.CLIPBOARD_SERVICE);
		serviceRegistry.put(ConnectivityManager.class,
				Context.CONNECTIVITY_SERVICE);
		serviceRegistry.put(InputMethodManager.class,
				Context.INPUT_METHOD_SERVICE);
		serviceRegistry.put(KeyguardManager.class, Context.KEYGUARD_SERVICE);
		serviceRegistry.put(LayoutInflater.class,
				Context.LAYOUT_INFLATER_SERVICE);
		serviceRegistry.put(LocationManager.class, Context.LOCATION_SERVICE);
		serviceRegistry.put(NotificationManager.class,
				Context.NOTIFICATION_SERVICE);
		serviceRegistry.put(PowerManager.class, Context.POWER_SERVICE);
		serviceRegistry.put(SearchManager.class, Context.SEARCH_SERVICE);
		serviceRegistry.put(SensorManager.class, Context.SENSOR_SERVICE);
		serviceRegistry.put(TelephonyManager.class, Context.TELEPHONY_SERVICE);
		serviceRegistry.put(Vibrator.class, Context.VIBRATOR_SERVICE);
		serviceRegistry.put(WallpaperManager.class, Context.WALLPAPER_SERVICE);
		serviceRegistry.put(WifiManager.class, Context.WIFI_SERVICE);
		serviceRegistry.put(WindowManager.class, Context.WINDOW_SERVICE);

		HashMap<String, String> postAPI7 = new HashMap<String, String>();
		postAPI7.put("android.accounts.AccountManager", "account");
		postAPI7.put("android.app.admin.DevicePolicyManager", "device_policy");
		postAPI7.put("android.app.DownloadManager", "download");
		postAPI7.put("android.os.DropBoxManager", "dropbox");
		postAPI7.put("android.nfc.NfcManager", "nfc");
		postAPI7.put("android.os.storage.StorageManager", "storage");
		postAPI7.put("android.view.textservice.TextServicesManager",
				"textservices");
		postAPI7.put("android.app.UiModeManager", "uimode");
		postAPI7.put("android.hardware.usb.UsbManager", "usb");
		postAPI7.put("android.net.wifi.p2p.WifiP2pManager", "wifip2p");

		for (String clsName : postAPI7.keySet()) {
			try {
				Class<?> cls = Class.forName(clsName);
				serviceRegistry.put(cls, postAPI7.get(clsName));
			} catch (ClassNotFoundException e) {
				L.d(clsName + " not present.");
			}
		}

	}

}
