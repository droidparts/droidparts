package org.droidparts.test.manager;

import org.droidparts.persist.json.JSONSerializer;
import org.droidparts.test.model.Phone;

public class PhoneSerializer extends JSONSerializer<Phone> {

	public PhoneSerializer() {
		super(Phone.class);
	}

}
