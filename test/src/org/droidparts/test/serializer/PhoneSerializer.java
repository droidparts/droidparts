package org.droidparts.test.serializer;

import org.droidparts.serializer.json.JSONSerializer;
import org.droidparts.test.model.Phone;

public class PhoneSerializer extends JSONSerializer<Phone> {

	public PhoneSerializer() {
		super(Phone.class);
	}

}
