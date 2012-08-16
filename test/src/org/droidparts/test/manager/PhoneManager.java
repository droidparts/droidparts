package org.droidparts.test.manager;

import org.droidparts.manager.sql.EntityManager;
import org.droidparts.test.model.Phone;

import android.content.Context;

public class PhoneManager extends EntityManager<Phone> {

	public PhoneManager(Context ctx) {
		super(ctx, Phone.class);
	}

}
