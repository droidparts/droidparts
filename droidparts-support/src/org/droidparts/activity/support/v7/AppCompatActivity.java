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
package org.droidparts.activity.support.v7;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;
import android.view.View;

import org.droidparts.inner.delegate.SupportDelegate;

public class AppCompatActivity extends android.support.v7.app.AppCompatActivity {

	private MenuItem reloadMenuItem;
	private View loadingIndicator;

	private boolean isLoading;

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

	public final void setActionBarLoadingIndicatorVisible(boolean visible) {
		isLoading = visible;
		if (reloadMenuItem != null) {
			reloadMenuItem.setActionView(visible ? loadingIndicator : null);
		}
	}

	public final void setActionBarReloadMenuItem(MenuItem menuItem) {
		this.reloadMenuItem = menuItem;
		if (menuItem != null && loadingIndicator == null) {
			loadingIndicator = SupportDelegate.activityBuildLoadingIndicator(this);
		}
		setActionBarLoadingIndicatorVisible(isLoading);
	}

	public void setFragmentVisible(boolean visible, Fragment... fragments) {
		SupportDelegate.activitySetFragmentVisible(this, visible, fragments);
	}

}
