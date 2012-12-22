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
package org.droidparts.util.fragments;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class SupportUtil extends BaseUtil {

	public static void fragmentActivitySetFragmentVisible(
			FragmentActivity fragmentActivity, int fragmentId, boolean visible) {
		FragmentManager fm = fragmentActivity.getSupportFragmentManager();
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

	public static void singleFragmentActivityAddFragmentToContentView(
			FragmentActivity fragmentActivity, Fragment fragment) {
		FragmentManager fm = fragmentActivity.getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.add(CONTENT_VIEW_ID, fragment);
		ft.commit();
	}

	public static void dialogFragmentShowDialogFragment(
			FragmentActivity fragmentActivity, DialogFragment dialogFragment) {
		FragmentManager fm = fragmentActivity.getSupportFragmentManager();
		String tag = dialogFragment.getClass().getName();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment f = fm.findFragmentByTag(tag);
		if (f != null) {
			ft.remove(f);
		}
		dialogFragment.show(ft, tag);
	}

}
