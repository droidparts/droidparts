package org.droidparts.serializer;

import org.droidparts.model.Phone;
import org.droidparts.serializer.json.JSONSerializer;

public class PhoneSerializer extends JSONSerializer<Phone> {

	public PhoneSerializer() {
		super(Phone.class);
	}

}
