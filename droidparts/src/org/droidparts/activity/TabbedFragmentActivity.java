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
package org.droidparts.activity;

import static android.app.ActionBar.NAVIGATION_MODE_TABS;

import java.util.ArrayList;
import java.util.HashSet;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;

public abstract class TabbedFragmentActivity extends Activity {

	private static final String CURR_TAB = "__curr_tab__";

	private final ArrayList<Fragment[]> fragmentsOnTab = new ArrayList<Fragment[]>();

	private final HashSet<Fragment> manuallyHiddenFragments = new HashSet<Fragment>();

	private final TabListener tabListener = new TabListener() {

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			showFragmentsForCurrentTab(ft);
			onTabChanged(getCurrentTab());
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

	private int enterAnimation, exitAnimation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setNavigationMode(NAVIGATION_MODE_TABS);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(CURR_TAB, getCurrentTab());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		setCurrentTab(savedInstanceState.getInt(CURR_TAB, 0));
	}

	public void addTab(ActionBar.Tab tab, Fragment... tabFragments) {
		addTab(fragmentsOnTab.size(), tab, tabFragments);
	}

	public void addTab(int position, ActionBar.Tab tab,
			Fragment... tabFragments) {
		tab.setTabListener(tabListener);
		getActionBar().addTab(tab, position);
		fragmentsOnTab.add(position, tabFragments);
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		showFragmentsForCurrentTab(ft);
		ft.commit();
	}

	public void setCustomAnimations(int enter, int exit) {
		this.enterAnimation = enter;
		this.exitAnimation = exit;
	}

	public void setCurrentTab(int position) {
		getActionBar().setSelectedNavigationItem(position);
	}

	public int getCurrentTab() {
		return getActionBar().getSelectedTab().getPosition();
	}

	protected void onTabChanged(int position) {

	}

	@Override
	public void setFragmentVisible(boolean visible, Fragment... fragments) {
		// set visible only if it's on current tab
		for (Fragment fragment : fragments) {
			if (visible) {
				manuallyHiddenFragments.remove(fragment);
				Fragment[] currTabFragments = fragmentsOnTab
						.get(getCurrentTab());
				for (Fragment currTabFragment : currTabFragments) {
					if (fragment == currTabFragment) {
						super.setFragmentVisible(true, fragment);
						break;
					}
				}
			} else {
				manuallyHiddenFragments.add(fragment);
				super.setFragmentVisible(false, fragment);
			}
		}
	}

	private void showFragmentsForCurrentTab(FragmentTransaction ft) {
		int currTabPos = getCurrentTab();
		if (enterAnimation != 0 && exitAnimation != 0) {
			ft.setCustomAnimations(enterAnimation, exitAnimation);
		}
		for (int tabPos = 0; tabPos < fragmentsOnTab.size(); tabPos++) {
			boolean isCurrTab = (tabPos == currTabPos);
			for (Fragment fragment : fragmentsOnTab.get(tabPos)) {
				if (isCurrTab) {
					if (!manuallyHiddenFragments.contains(fragment)) {
						ft.show(fragment);
					}
				} else {
					ft.hide(fragment);
				}
			}
		}
	}
}
