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
package org.droidparts.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.droidparts.inner.delegate.BaseDelegate;
import org.droidparts.inner.delegate.FragmentDelegate;

public class ListFragment extends android.app.ListFragment {

	public static <T extends ListFragment> T newInstance(Class<T> cls, Bundle args){
		return FragmentDelegate.newInstance(cls, args);
	}

	private boolean injected;

	@Override
	public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = onCreateView(savedInstanceState, inflater, container);
		FragmentDelegate.onFragmentCreateView(this, view, null, savedInstanceState);
		injected = true;
		return view;
	}

	protected View onCreateView(Bundle savedInstanceState, LayoutInflater inflater, ViewGroup container) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		BaseDelegate.onResume(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		BaseDelegate.onPause(this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		FragmentDelegate.onFragmentSaveInstanceState(this, injected, outState);
	}

	public final boolean isInjected() {
		return injected;
	}

}
