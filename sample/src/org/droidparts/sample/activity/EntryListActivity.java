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
package org.droidparts.sample.activity;

import java.util.ArrayList;

import org.droidparts.activity.ListActivity;
import org.droidparts.annotation.inject.InjectDependency;
import org.droidparts.annotation.inject.InjectView;
import org.droidparts.sample.R;
import org.droidparts.sample.adapter.EntryListAdapter;
import org.droidparts.sample.json.EntrySerializer;
import org.droidparts.sample.model.Entry;
import org.droidparts.util.DialogFactory;
import org.droidparts.util.L;
import org.json.JSONArray;
import org.json.JSONException;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

public class EntryListActivity extends ListActivity implements OnClickListener {

	private EntryListAdapter adapter;

	@InjectView(R.id.button_add)
	private Button addButton;
	@InjectView
	private Button toJsonButton;

	@InjectDependency
	private EntrySerializer entrySerializer;
	@InjectDependency
	private DialogFactory dialogFactory;

	@Override
	public void onPreInject() {
		setContentView(R.layout.activity_entry_list);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new EntryListAdapter(this);
		setListAdapter(adapter);
		addButton.setOnClickListener(this);
		toJsonButton.setOnClickListener(this);
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
		entry.name = "Entry #" + adapter.getCount();
		adapter.create(entry);
	}

	private void showEntryListJSON() {
		ArrayList<Entry> list = new ArrayList<Entry>();
		for (int i = 0; i < adapter.getCount(); i++) {
			Entry entry = adapter.read(i);
			list.add(entry);
		}
		String msg;
		try {
			JSONArray arr = entrySerializer.serializeList(list);
			msg = arr.toString();
		} catch (JSONException e) {
			L.e(e);
			msg = "o_O";
		}
		dialogFactory.showToast(msg);
	}
}