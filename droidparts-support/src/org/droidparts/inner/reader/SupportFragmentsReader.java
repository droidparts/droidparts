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
package org.droidparts.inner.reader;

import org.droidparts.inner.reader.LegacyReader.ISupportFragmentsReader;
import org.droidparts.util.ResourceUtils;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

public class SupportFragmentsReader implements ISupportFragmentsReader {

	@Override
	public boolean isSupportObject(Object obj) {
		return (obj instanceof FragmentActivity) || (obj instanceof Fragment);
	}

	@Override
	public Activity getParentActivity(Object fragmentObj) {
		Fragment fragment = (Fragment) fragmentObj;
		return fragment.getActivity();
	}

	@Override
	public Fragment getFragment(Object fragmentActivityObj, int fragmentId,
			String valName) {
		FragmentActivity fragmentActivity = (FragmentActivity) fragmentActivityObj;
		if (fragmentId == 0) {
			fragmentId = ResourceUtils.getResourceId(fragmentActivity, valName);
		}
		return fragmentActivity.getSupportFragmentManager().findFragmentById(
				fragmentId);
	}

	@Override
	public Bundle getFragmentArguments(Object fragmentObj) {
		return ((Fragment) fragmentObj).getArguments();
	}

}
