package org.droidparts.sample.json;

import org.droidparts.persist.json.JSONSerializer;
import org.droidparts.sample.model.Entry;

import android.content.Context;

public class EntrySerializer extends JSONSerializer<Entry> {

	public EntrySerializer(Context ctx) {
		super(ctx, Entry.class);
	}

}
