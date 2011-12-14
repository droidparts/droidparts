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
package org.droidparts.activity;

import org.droidparts.R;
import org.droidparts.inject.Injector;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost.TabSpec;

public class TabActivity extends android.app.TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.droidparts_tabbed_activity);
		Injector.inject(this);
	}

	protected void addTab(String tag, int textId, int imgId, Intent intent) {
		TabSpec tab = getTabHost().newTabSpec(tag);
		tab.setIndicator(getString(textId), getResources().getDrawable(imgId));
		tab.setContent(intent);
		getTabHost().addTab(tab);
	}

}
