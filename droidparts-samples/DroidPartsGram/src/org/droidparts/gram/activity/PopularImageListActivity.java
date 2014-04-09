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
package org.droidparts.gram.activity;

import org.droidparts.activity.SingleFragmentActivity;
import org.droidparts.annotation.inject.InjectDependency;
import org.droidparts.concurrent.service.MainThreadResultReceiver;
import org.droidparts.gram.R;
import org.droidparts.gram.adapter.ImageListAdapter;
import org.droidparts.gram.fragment.ImageDetailFragment;
import org.droidparts.gram.fragment.ImageListFragment;
import org.droidparts.gram.model.Image;
import org.droidparts.gram.persist.ImageEntityManager;
import org.droidparts.gram.service.ImageIntentService;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class PopularImageListActivity extends
		SingleFragmentActivity<ImageListFragment> implements
		ImageListFragment.Listener {

	@InjectDependency
	private ImageEntityManager imageEntityManager;

	private ImageListAdapter adapter;

	@Override
	protected ImageListFragment onCreateFragment() {
		return new ImageListFragment();
	}

	@Override
	protected void onStart() {
		super.onStart();
		setAdapter();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.options_image_list, menu);
		setActionBarReloadMenuItem(menu.findItem(R.id.menu_refresh));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			setActionBarLoadingIndicatorVisible(true);
			intent = ImageIntentService.getUpdatePicsIntent(this,
					refreshResultReceiver);
			startService(intent);
			return true;
		case R.id.menu_settings:
			intent = SettingsActivity.getIntent(this);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void doShowImageDetail(int position) {
		Image img = adapter.read(position);
		ImageDetailFragment.newInstance(img).show(this);
	}

	public void setAdapter() {
		adapter = new ImageListAdapter(this, imageEntityManager.select());
		getFragment().setContent(adapter);
	}

	private final MainThreadResultReceiver refreshResultReceiver = new MainThreadResultReceiver() {

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			setActionBarLoadingIndicatorVisible(false);
			setAdapter();
		}

	};

}
