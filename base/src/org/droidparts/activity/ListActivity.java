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
import org.droidparts.annotation.inject.InjectView;
import org.droidparts.inject.Injector;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

public class ListActivity extends android.app.ListActivity {

	@InjectView(android.R.id.empty)
	protected TextView emptyView;
	@InjectView(android.R.id.list)
	protected ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int windowFeature = getWindowFeature();
		if (windowFeature != -1) {
			requestWindowFeature(windowFeature);
		}
		setContentView(getContentViewId());
		Injector.inject(this);
	}

	protected int getWindowFeature() {
		return -1;
	}

	protected int getContentViewId() {
		return R.layout.droidparts_list_activity;
	}

}
