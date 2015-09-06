/**
 * Copyright 2015 Alex Yanchenko
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
package org.droidparts.test.activity;

import org.droidparts.annotation.inject.InjectBundleExtra;
import org.droidparts.annotation.serialize.SaveInstanceState;
import org.droidparts.fragment.Fragment;
import org.droidparts.test.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TestFragment extends Fragment {

	public static final String EXTRA_STR = "str";

	@InjectBundleExtra(key = EXTRA_STR)
	@SaveInstanceState
	public String str;

	@Override
	protected View onCreateView(Bundle savedInstanceState, LayoutInflater inflater, ViewGroup container) {
		return inflater.inflate(R.layout.fragment_test, null);
	}

}
