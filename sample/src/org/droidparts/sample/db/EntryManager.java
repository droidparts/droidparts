package org.droidparts.sample.db;

import org.droidparts.manager.sql.AnnotatedEntityManager;
import org.droidparts.sample.model.Entry;

import android.content.Context;

public class EntryManager extends AnnotatedEntityManager<Entry> {

	public EntryManager(Context ctx) {
		super(ctx, Entry.class);
	}

	@Override
	public boolean create(Entry item) {
		item.created = System.currentTimeMillis();
		return super.create(item);
	}

}
