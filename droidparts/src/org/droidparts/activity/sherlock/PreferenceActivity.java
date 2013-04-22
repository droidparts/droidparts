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
package org.droidparts.activity.sherlock;

import org.droidparts.Injector;
import org.droidparts.contract.Injectable;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

public class PreferenceActivity extends
		com.actionbarsherlock.app.SherlockPreferenceActivity implements
		Injectable {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		onPreInject();
		Injector.inject(this);
	}

	@Override
	public void onPreInject() {
	}

	// This Activity exists for the reason that the 'modern fragment-based
	// PreferenceActivity' is not available pre-Honeycomb. So un@Depracating
	// the methods below.

	@Override
	public PreferenceManager getPreferenceManager() {
		return super.getPreferenceManager();
	}

	@Override
	public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
		super.setPreferenceScreen(preferenceScreen);
	}

	@Override
	public PreferenceScreen getPreferenceScreen() {
		return super.getPreferenceScreen();
	}

	@Override
	public void addPreferencesFromIntent(Intent intent) {
		super.addPreferencesFromIntent(intent);
	}

	@Override
	public void addPreferencesFromResource(int preferencesResId) {
		super.addPreferencesFromResource(preferencesResId);
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	@Override
	public Preference findPreference(CharSequence key) {
		return super.findPreference(key);
	}

}
