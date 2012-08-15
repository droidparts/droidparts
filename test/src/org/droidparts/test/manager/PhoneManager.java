package org.droidparts.test.manager;

import org.droidparts.manager.sql.AnnotatedEntityManager;
import org.droidparts.test.model.Phone;

import android.content.Context;

public class PhoneManager extends AnnotatedEntityManager<Phone> {

	public PhoneManager(Context ctx) {
		super(ctx, Phone.class);
	}

}
