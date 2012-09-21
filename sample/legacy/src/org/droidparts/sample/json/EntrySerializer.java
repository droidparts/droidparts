package org.droidparts.sample.json;

import org.droidparts.persist.json.JSONSerializer;
import org.droidparts.sample.model.Entry;

public class EntrySerializer extends JSONSerializer<Entry> {

	public EntrySerializer() {
		super(Entry.class);
	}

}
