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
package org.droidparts.activity;

import org.droidparts.R;
import org.droidparts.inject.Injectable;
import org.droidparts.inject.FragmentsInjector;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public abstract class FragmentActivity extends SherlockFragmentActivity
		implements Injectable {

	private MenuItem reloadMenuItem;
	private View loadingIndicator;

	private boolean isLoading;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		onPreInject();
		FragmentsInjector.get().inject(this);
	}

	@Override
	public void onPreInject() {
	}

	@Override
	public void setSupportProgressBarIndeterminateVisibility(boolean visible) {
		isLoading = visible;
		if (reloadMenuItem != null) {
			reloadMenuItem.setActionView(visible ? loadingIndicator : null);
		} else {
			super.setSupportProgressBarIndeterminateVisibility(visible);
		}
	}

	public void setReloadMenuItem(MenuItem menuItem) {
		this.reloadMenuItem = menuItem;
		if (menuItem != null && loadingIndicator == null) {
			loadingIndicator = LayoutInflater.from(this).inflate(
					R.layout.view_ab_loading_indicator, null);
		}
		setSupportProgressBarIndeterminateVisibility(isLoading);
	}

	public void setFragmentVisible(int fragmentId, boolean visible) {
		FragmentManager fm = getSupportFragmentManager();
		Fragment f = fm.findFragmentById(fragmentId);
		if (f != null) {
			FragmentTransaction ft = fm.beginTransaction();
			if (visible) {
				ft.show(f);
			} else {
				ft.hide(f);
			}
			ft.commit();
		}
	}

}
