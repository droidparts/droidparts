/**
 * Copyright 2012 Alex Yanchenko
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

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.view.Window.FEATURE_INDETERMINATE_PROGRESS;
import static android.widget.Toast.LENGTH_LONG;

import org.droidparts.R;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class AsyncListActivity extends ListActivity {

	// @InjectView(R.id.progressContainer)
	private View progressContainer;
	// @InjectView(R.id.listContainer)
	private View listContainer;

	@Override
	public void onPreInject() {
		requestWindowFeature(FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_list);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		progressContainer = findViewById(R.id.progressContainer);
		listContainer = findViewById(R.id.listContainer);
	}

	public void setLoading(boolean loading) {
		if (loading) {
			boolean emptyList = getListView().getCount() == 0;
			if (emptyList) {
				progressContainer.setVisibility(VISIBLE);
				listContainer.setVisibility(GONE);
			} else {
				setProgressBarIndeterminate(loading);
			}
		} else {
			setProgressBarIndeterminate(false);
			progressContainer.setVisibility(GONE);
			listContainer.setVisibility(VISIBLE);
		}
	}

	public void handleException(Exception e) {
		setLoading(false);
		Toast.makeText(this, String.valueOf(e.getMessage()), LENGTH_LONG)
				.show();
	}

}
