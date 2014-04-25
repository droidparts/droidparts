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
package org.droidparts.activity.legacy;

import org.droidparts.Injector;
import org.droidparts.bus.EventBus;
import org.droidparts.contract.Injectable;

import android.os.Bundle;

public abstract class PreferenceActivity extends
		android.preference.PreferenceActivity implements Injectable {

	@Override
	public void onPreInject() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		onPreInject();
		Injector.inject(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		EventBus.registerAnnotatedReceiver(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		EventBus.unregisterAnnotatedReceiver(this);
	}

}
