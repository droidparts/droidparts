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
package org.droidparts.fragment;

import org.droidparts.Injector;
import org.droidparts.inner.fragments.SecretFragmentsStockUtil;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DialogFragment extends android.app.DialogFragment {

	private boolean injected;

	@Override
	public final View onCreateView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		View view = onCreateView(savedInstanceState, inflater, container);
		if (view != null) {
			Injector.inject(view, this);
			injected = true;
		} else {
			Injector.inject(getDialog(), this);
		}
		return view;
	}

	public View onCreateView(Bundle savedInstanceState,
			LayoutInflater inflater, ViewGroup container) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	public final boolean isInjected() {
		return injected;
	}

	public void show(Activity activity) {
		SecretFragmentsStockUtil.dialogFragmentShowDialogFragment(activity,
				this);
	}

}
