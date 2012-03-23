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
import java.util.HashSet;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;

public abstract class TabbedFragmentActivity extends FragmentActivity {

	private final ArrayList<int[]> tabSpecs = new ArrayList<int[]>();

	private final HashSet<Integer> hiddenFragments = new HashSet<Integer>();

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

	@Override
	public void setFragmentVisible(int fragmentId, boolean visible) {
		if (visible) {
			hiddenFragments.remove(fragmentId);
			int[] currTab = tabSpecs.get(getSelectedTabPos());
			for (int fId : currTab) {
				if (fId == fragmentId) {
					super.setFragmentVisible(fragmentId, true);
					break;
				}
			}
		} else {
			hiddenFragments.add(fragmentId);
			super.setFragmentVisible(fragmentId, false);
		}
	}

	private void onTabSelected(Tab tab, FragmentTransaction ft) {
		int pos = tab.getPosition();
		FragmentManager fm = getSupportFragmentManager();
		for (int tabPos = 0; tabPos < tabSpecs.size(); tabPos++) {
			int[] tabSpec = tabSpecs.get(tabPos);
			for (int fragmentId : tabSpec) {
				Fragment fragment = fm.findFragmentById(fragmentId);
				if (fragment != null) {
					boolean tabSelected = (tabPos == pos);
					if (tabSelected) {
						if (!hiddenFragments.contains(fragmentId)) {
							ft.show(fragment);
						}
					} else {
						if (fragment.isHidden()) {
							hiddenFragments.add(fragmentId);
						} else {
							hiddenFragments.remove(fragmentId);
							ft.hide(fragment);
						}
					}
				}
			}
		}
		onTabSelected(pos);
	}
}
