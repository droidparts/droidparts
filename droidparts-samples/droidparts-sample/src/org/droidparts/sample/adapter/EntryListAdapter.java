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
		super(Entry.class, ctx, new EntryManager(ctx), select);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View v = layoutInflater.inflate(android.R.layout.simple_list_item_2,
				null);
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
