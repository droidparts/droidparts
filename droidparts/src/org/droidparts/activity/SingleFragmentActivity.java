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
package org.droidparts.activity;

import org.droidparts.inner.fragments.SecretFragmentsStockUtil;

import android.app.Fragment;
import android.os.Bundle;

public abstract class SingleFragmentActivity<F extends Fragment> extends
		Activity {

	private F fragment;

	@Override
	public void onPreInject() {
		SecretFragmentsStockUtil.singleFragmentActivitySetContentView(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fragment = onCreateFragment();
		SecretFragmentsStockUtil
				.singleFragmentActivityAddFragmentToContentView(this, fragment);
	}

	protected F getFragment() {
		return fragment;
	}

	protected abstract F onCreateFragment();

}
