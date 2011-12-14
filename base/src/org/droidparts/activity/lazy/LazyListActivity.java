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
package org.droidparts.activity.lazy;

import static android.widget.Toast.LENGTH_LONG;

import org.droidparts.activity.ListActivity;

import android.view.Window;
import android.widget.Toast;

import org.droidparts.R;

public class LazyListActivity extends ListActivity implements LazyLoadable {

	@Override
	public void setLoadingState() {
		setProgressBarIndeterminateVisibility(true);
		emptyView.setText(R.string.loading);
	}

	@Override
	public void setDefaultState() {
		setProgressBarIndeterminateVisibility(false);
		emptyView.setText(R.string.empty);
	}

	@Override
	public void onException(Exception e) {
		setDefaultState();
		Toast.makeText(this, "" + e.getMessage(), LENGTH_LONG).show();
	}

	@Override
	protected int getWindowFeature() {
		return Window.FEATURE_INDETERMINATE_PROGRESS;
	}

}
