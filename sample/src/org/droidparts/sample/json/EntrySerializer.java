package org.droidparts.sample.json;

import org.droidparts.sample.model.Entry;
import org.droidparts.serializer.json.JSONSerializer;

public class EntrySerializer extends JSONSerializer<Entry> {

	public EntrySerializer() {
		super(Entry.class);
	}

}
