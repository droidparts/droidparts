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
import android.widget.TextView;

public abstract class ListActivity extends android.app.ListActivity implements
		Injected {

	// @InjectView(android.R.id.list)
	// private ListView listView;
	@InjectView(android.R.id.empty)
	private TextView emptyView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		onPreInject();
		Injector.get().inject(this);
	}

	@Override
	public void onPreInject() {
		setContentView(R.layout.activity_list);
	}

	public void setEmptyText(CharSequence text) {
		emptyView.setText(text);
	}

}
