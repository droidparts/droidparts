/**
 * Copyright 2011 Alex Yanchenko
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
package org.droidparts.fragment;

import org.droidparts.inject.FragmentsInjector;

import android.support.v4.app.SupportActivity;
import android.view.View;

public class DialogFragment extends android.support.v4.app.DialogFragment {

	protected View contentView;

	@Override
	public void onAttach(SupportActivity activity) {
		super.onAttach(activity);
		int contentViewId = getContentViewId();
		if (contentViewId != 0) {
			contentView = getActivity().getLayoutInflater().inflate(
					contentViewId, null);
			FragmentsInjector.get().inject(contentView, this);
		}
	}

	protected int getContentViewId() {
		return 0;
	}

}
