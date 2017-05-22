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
package org.droidparts.activity;

import android.os.Bundle;

import org.droidparts.inner.delegate.BaseDelegate;

public class PreferenceActivity extends android.preference.PreferenceActivity {

	protected void onPreInject() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		onPreInject();
		BaseDelegate.onActivityCreate(this, savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		BaseDelegate.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		BaseDelegate.onPause(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		BaseDelegate.onActivitySaveInstanceState(this, outState);
	}

}
