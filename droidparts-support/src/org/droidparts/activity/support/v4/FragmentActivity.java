/**
 * Copyright 2016 Alex Yanchenko
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
package org.droidparts.activity.support.v4;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.droidparts.inner.delegate.SupportDelegate;

public class FragmentActivity extends android.support.v4.app.FragmentActivity {

	protected void onPreInject() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		onPreInject();
		SupportDelegate.onActivityCreate(this, savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		SupportDelegate.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		SupportDelegate.onPause(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		SupportDelegate.onActivitySaveInstanceState(this, outState);
	}

	public void setFragmentVisible(boolean visible, Fragment... fragments) {
		SupportDelegate.activitySetFragmentVisible(this, visible, fragments);
	}

}
