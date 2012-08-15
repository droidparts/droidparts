package org.droidparts.test.testcase;

import org.droidparts.test.manager.PhoneManager;
import org.droidparts.test.model.Phone;

import android.test.AndroidTestCase;

public class EntityTestCase extends AndroidTestCase {

	private PhoneManager phoneManager;

	@Override
	protected void setUp() throws Exception {
		phoneManager = new PhoneManager(getContext());
	}

	public void testCRUD() throws Exception {
		Phone phone = new Phone();
		phone.name = "Galaxy Nexus";
		phone.version = 4.1f;
		phoneManager.create(phone);
		assertFalse(phone.id == 0);
		Phone phone2 = phoneManager.read(phone.id);
		assertEquals(phone.name, phone2.name);
		phone2.name = "iPhone";
		phoneManager.update(phone2);
		Phone phone3 = phoneManager.read(phone2.id);
		assertEquals(phone2.name, phone3.name);
		assertEquals(phone.id, phone3.id);
		phoneManager.delete(phone.id);
		assertNull(phoneManager.read(phone.id));
	}

}
