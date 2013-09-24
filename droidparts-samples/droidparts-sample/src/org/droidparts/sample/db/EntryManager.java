package org.droidparts.sample.db;

import java.util.Date;

import org.droidparts.persist.sql.EntityManager;
import org.droidparts.sample.model.Entry;

import android.content.Context;

public class EntryManager extends EntityManager<Entry> {

	public EntryManager(Context ctx) {
		super(Entry.class, ctx);
	}

	@Override
	public boolean create(Entry item) {
		item.created = new Date();
		return super.create(item);
	}

}
