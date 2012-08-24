package org.droidparts.test.testcase;

import org.droidparts.contract.DB;
import org.droidparts.manager.sql.EntityManager;
import org.droidparts.manager.sql.stmt.Is;
import org.droidparts.test.manager.PhoneManager;
import org.droidparts.test.model.Phone;
import org.droidparts.test.model.TwoStrings;

import android.database.Cursor;
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

	public void testUniqueAndNull() throws Exception {
		EntityManager<TwoStrings> mngr = EntityManager.getInstance(
				getContext(), TwoStrings.class);
		TwoStrings str1 = new TwoStrings();
		str1.one = "one1";
		str1.two = "two1";
		boolean created = mngr.create(str1);
		assertTrue(created);

		TwoStrings str2 = new TwoStrings();
		created = mngr.create(str2);
		assertFalse(created);

		str2.one = str1.one;
		created = mngr.create(str2);
		assertFalse(created);

		str2.one = str1.one + "x";
		created = mngr.create(str2);
		assertTrue(created);

		Cursor cursor = mngr.select().where(DB.Column.ID, Is.EQUAL, str1.id)
				.execute();
		assertEquals(1, cursor.getCount());
		cursor.close();

		cursor = mngr.select().where("two", Is.NOT_NULL, null).execute();
		assertEquals(1, cursor.getCount());
		cursor.moveToFirst();
		TwoStrings str11 = mngr.readFromCursor(cursor);
		assertEquals(str1.one, str11.one);
		cursor.close();

		cursor = mngr.select().where("two", Is.NULL, null).execute();
		assertEquals(1, cursor.getCount());
		cursor.moveToFirst();
		TwoStrings str21 = mngr.readFromCursor(cursor);
		assertEquals(str2.one, str21.one);
		cursor.close();
	}

}
