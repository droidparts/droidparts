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
package org.droidparts.inner.delegate;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import org.droidparts.inner.ReflectionUtils;

public class SupportDelegate extends BaseDelegate {

	public static <T extends Fragment> T newInstance(Class<T> cls, Bundle bundle) {
		T fragment = ReflectionUtils.newInstance(cls);
		fragment.setArguments(bundle);
		return fragment;
	}

	public static void activitySetFragmentVisible(FragmentActivity fragmentActivity, boolean visible,
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

	public static void singleFragmentActivityAddFragmentToContentView(FragmentActivity fragmentActivity,
	                                                                  Fragment fragment) {
		FragmentManager fm = fragmentActivity.getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.add(CONTENT_VIEW_ID, fragment);
		ft.commit();
	}

	public static void showDialogFragment(FragmentActivity activity, DialogFragment df) {
		FragmentManager fm = activity.getSupportFragmentManager();
		String tag = df.getClass().getName();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment f = fm.findFragmentByTag(tag);
		if (f != null) {
			ft.remove(f);
		}
		df.show(ft, tag);
	}

}
