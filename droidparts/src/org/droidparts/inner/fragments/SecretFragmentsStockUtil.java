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
package org.droidparts.inner.fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

public class SecretFragmentsStockUtil extends SecretFragmentsUtil {

	public static void fragmentActivitySetFragmentVisible(
			Activity fragmentActivity, boolean visible, Fragment... fragments) {
		FragmentManager fm = fragmentActivity.getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		for (Fragment fragment : fragments) {
			if (visible) {
				ft.show(fragment);
			} else {
				ft.hide(fragment);
			}
		}
		ft.commit();
	}

	public static void singleFragmentActivityAddFragmentToContentView(
			Activity fragmentActivity, Fragment fragment) {
		FragmentManager fm = fragmentActivity.getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.add(CONTENT_VIEW_ID, fragment);
		ft.commit();
	}

	public static void dialogFragmentShowDialogFragment(
			Activity fragmentActivity, DialogFragment dialogFragment) {
		FragmentManager fm = fragmentActivity.getFragmentManager();
		String tag = dialogFragment.getClass().getName();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment f = fm.findFragmentByTag(tag);
		if (f != null) {
			ft.remove(f);
		}
		dialogFragment.show(ft, tag);
	}

}
