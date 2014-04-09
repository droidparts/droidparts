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
package org.droidparts.sample.activity;

import static org.droidparts.util.Strings.isNotEmpty;
import static org.droidparts.util.ui.ViewUtils.setKeyboardVisible;

import java.util.ArrayList;

import org.droidparts.activity.legacy.ListActivity;
import org.droidparts.annotation.inject.InjectView;
import org.droidparts.sample.R;
import org.droidparts.sample.adapter.EntryListAdapter;
import org.droidparts.sample.db.EntryManager;
import org.droidparts.sample.model.Entry;
import org.droidparts.util.L;
import org.droidparts.widget.ClearableEditText;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

public class EntryListActivity extends ListActivity implements OnClickListener {

	private EntryListAdapter adapter;

	@InjectView(id = R.id.view_name)
	private ClearableEditText nameView;

	@InjectView(id = R.id.button_add, click = true)
	private Button addButton;
	@InjectView(click = true)
	private Button toJsonButton;

	@Override
	public void onPreInject() {
		setContentView(R.layout.activity_entry_list);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new EntryListAdapter(this, new EntryManager(this).select());
		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		adapter.delete(position);
	}

	@Override
	public void onClick(View v) {
		if (v == addButton) {
			addEntry();
		} else if (v == toJsonButton) {
			showEntryListJSON();
		}
	}

	private void addEntry() {
		L.i("Adding an Entry.");
		Entry entry = new Entry();
		String name = nameView.getText().toString().trim();
		if (isNotEmpty(name)) {
			nameView.setText("");
			setKeyboardVisible(nameView, false);
		} else {
			name = "Entry";
		}
		entry.name = name + " #" + adapter.getCount();
		adapter.create(entry);
	}

	private void showEntryListJSON() {
		ArrayList<Entry> list = new ArrayList<Entry>();
		for (int i = 0; i < adapter.getCount(); i++) {
			Entry entry = adapter.read(i);
			list.add(entry);
		}
		Intent intent = JsonViewActivity.getIntent(this, list);
		startActivity(intent);
	}
}