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
package org.droidparts.persist;

import static android.content.Context.MODE_PRIVATE;
import static org.droidparts.util.Strings.isEmpty;

import org.droidparts.inject.Injector;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

public abstract class AbstractPrefsManager {

	private static final String VERSION = "__prefs_version__";

	protected final Context ctx;
	protected final Resources res;
	protected final SharedPreferences prefs;

	public AbstractPrefsManager(Context ctx, int version) {
		this(ctx, null, version);
	}

	public AbstractPrefsManager(Context ctx, String prefsName, int version) {
		Injector.get().inject(ctx, this);
		this.ctx = ctx.getApplicationContext();
		res = ctx.getResources();
		if (isEmpty(prefsName)) {
			prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		} else {
			prefs = ctx.getSharedPreferences(prefsName, MODE_PRIVATE);
		}
		init(version);
	}

	public SharedPreferences getPrefs() {
		return prefs;
	}

	private void init(int newVersion) {
		int oldVersion = prefs.getInt(VERSION, -1);
		if (oldVersion != newVersion) {
			onUpgrade(prefs, oldVersion, newVersion);
			saveInt(VERSION, newVersion);
		}
	}

	protected void onUpgrade(SharedPreferences prefs, int oldVersion,
			int newVersion) {
		prefs.edit().clear().commit();
	}

	// shortcuts

	protected boolean readBoolean(int keyResId, int defValueResId) {
		return prefs.getBoolean(res.getString(keyResId),
				res.getBoolean(defValueResId));
	}

	protected int readInt(int keyResId, int defValueResId) {
		return prefs.getInt(res.getString(keyResId),
				res.getInteger(defValueResId));
	}

	protected String readString(int keyResId, int defValueResId) {
		return prefs.getString(ctx.getString(keyResId),
				ctx.getString(defValueResId));
	}

	protected boolean saveBoolean(String key, boolean val) {
		return prefs.edit().putBoolean(key, val).commit();
	}

	protected boolean saveInt(String key, int val) {
		return prefs.edit().putInt(key, val).commit();
	}

	protected boolean saveLong(String key, long val) {
		return prefs.edit().putLong(key, val).commit();
	}

	protected boolean saveString(String key, String val) {
		return prefs.edit().putString(key, val).commit();
	}

}
