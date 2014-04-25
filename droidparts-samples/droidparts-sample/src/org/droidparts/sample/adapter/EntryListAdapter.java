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
package org.droidparts.sample.adapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.droidparts.adapter.cursor.EntityCursorAdapter;
import org.droidparts.adapter.holder.Text2Holder;
import org.droidparts.persist.sql.stmt.Select;
import org.droidparts.sample.db.EntryManager;
import org.droidparts.sample.model.Entry;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;

public class EntryListAdapter extends EntityCursorAdapter<Entry> {

	public EntryListAdapter(Context ctx, Select<Entry> select) {
		super(ctx, new EntryManager(ctx), select);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View v = getLayoutInflater().inflate(
				android.R.layout.simple_list_item_2, null);
		Text2Holder holder = new Text2Holder(v);
		v.setTag(holder);
		return v;
	}

	@Override
	public void bindView(Context context, View view, Entry item) {
		Text2Holder holder = (Text2Holder) view.getTag();
		holder.text1.setText(item.name);
		holder.text2.setText(DF.format(item.created));
	}

	private static final DateFormat DF = SimpleDateFormat.getTimeInstance();

}
