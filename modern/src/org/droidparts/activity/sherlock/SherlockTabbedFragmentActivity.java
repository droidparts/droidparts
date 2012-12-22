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
package org.droidparts.activity.sherlock;

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

public abstract class SherlockTabbedFragmentActivity extends SherlockFragmentActivity {

	private static final String CURR_TAB = "_curr_tab";

	private final ArrayList<int[]> fragmentsOnTab = new ArrayList<int[]>();

	private final HashSet<Integer> manuallyHiddenFragments = new HashSet<Integer>();

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
		getSupportActionBar().setNavigationMode(NAVIGATION_MODE_TABS);
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

	public void addTab(ActionBar.Tab tab, int[] fragmentIds) {
		addTab(fragmentsOnTab.size(), tab, fragmentIds);
	}

	public void addTab(int position, ActionBar.Tab tab, int[] fragmentIds) {
		tab.setTabListener(tabListener);
		getSupportActionBar().addTab(tab, position);
		fragmentsOnTab.add(position, fragmentIds);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		showFragmentsForCurrentTab(ft);
		ft.commit();
	}

	public void setCustomAnimations(int enter, int exit) {
		this.enterAnimation = enter;
		this.exitAnimation = exit;
	}

	public void setCurrentTab(int position) {
		getSupportActionBar().setSelectedNavigationItem(position);
	}

	public int getCurrentTab() {
		return getSupportActionBar().getSelectedTab().getPosition();
	}

	protected void onTabChanged(int position) {

	}

	@Override
	public void setFragmentVisible(int fragmentId, boolean visible) {
		// set visible only if it's on current tab
		if (visible) {
			manuallyHiddenFragments.remove(fragmentId);
			int[] currTabFragments = fragmentsOnTab.get(getCurrentTab());
			for (int fId : currTabFragments) {
				if (fId == fragmentId) {
					super.setFragmentVisible(fragmentId, true);
					break;
				}
			}
		} else {
			manuallyHiddenFragments.add(fragmentId);
			super.setFragmentVisible(fragmentId, false);
		}
	}

	private void showFragmentsForCurrentTab(FragmentTransaction ft) {
		int currTabPos = getCurrentTab();
		FragmentManager fm = getSupportFragmentManager();
		if (enterAnimation != 0 && exitAnimation != 0) {
			ft.setCustomAnimations(enterAnimation, exitAnimation);
		}
		for (int tabPos = 0; tabPos < fragmentsOnTab.size(); tabPos++) {
			boolean isCurrTab = (tabPos == currTabPos);
			int[] tabFragments = fragmentsOnTab.get(tabPos);
			for (int fragmentId : tabFragments) {
				Fragment fragment = fm.findFragmentById(fragmentId);
				if (fragment != null) {
					if (isCurrTab) {
						if (!manuallyHiddenFragments.contains(fragmentId)) {
							ft.show(fragment);
						}
					} else {
						ft.hide(fragment);
					}
				}
			}
		}
	}
}
