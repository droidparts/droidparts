/**
 * Copyright 2013 Alex Yanchenko
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

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class SecretFragmentsSupportUtil extends SecretFragmentsUtil {

	public static void fragmentActivitySetFragmentVisible(
			FragmentActivity fragmentActivity, boolean visible,
			Fragment... fragments) {
		FragmentManager fm = fragmentActivity.getSupportFragmentManager();
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
