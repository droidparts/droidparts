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

import static com.actionbarsherlock.app.ActionBar.NAVIGATION_MODE_TABS;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;

public abstract class TabbedFragmentActivity extends FragmentActivity {

	private final ArrayList<int[]> tabSpecs = new ArrayList<int[]>();

	private final TabListener tabListener = new TabListener() {

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			TabbedFragmentActivity.this.onTabSelected(tab, ft);
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			// NA
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			// NA
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setNavigationMode(NAVIGATION_MODE_TABS);
	}

	public int getSelectedTabPos() {
		return getSupportActionBar().getSelectedTab().getPosition();
	}

	protected void onTabSelected(int pos) {

	}

	protected final void addTab(ActionBar.Tab tab, int[] fragmentIds) {
		tab.setTabListener(tabListener);
		getSupportActionBar().addTab(tab);
		tabSpecs.add(fragmentIds);
	}

	// TODO make private
	protected void onTabSelected(Tab tab, FragmentTransaction ft) {
		int pos = tab.getPosition();
		FragmentManager fm = getSupportFragmentManager();
		for (int i = 0; i < tabSpecs.size(); i++) {
			int[] tabSpec = tabSpecs.get(i);
			for (int fragmentId : tabSpec) {
				Fragment f = fm.findFragmentById(fragmentId);
				if (f != null) {
					if (i == pos) {
						ft.show(f);
					} else {
						ft.hide(f);
					}
				}
			}
		}
		onTabSelected(pos);
	}
}
