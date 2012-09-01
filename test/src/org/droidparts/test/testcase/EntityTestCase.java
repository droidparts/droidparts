package org.droidparts.test.testcase;

import java.util.ArrayList;

import org.droidparts.contract.DB;
import org.droidparts.persist.sql.EntityManager;
import org.droidparts.persist.sql.stmt.Is;
import org.droidparts.test.manager.PhoneManager;
import org.droidparts.test.model.Phone;
import org.droidparts.test.model.TwoStrings;

import android.database.Cursor;
import android.test.AndroidTestCase;

public class EntityTestCase extends AndroidTestCase {

	private PhoneManager phoneManager;
	private EntityManager<TwoStrings> twoStringsManager;

	@Override
	protected void setUp() throws Exception {
		phoneManager = new PhoneManager(getContext());
		twoStringsManager = EntityManager.getInstance(getContext(),
				TwoStrings.class);
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
		TwoStrings str1 = new TwoStrings();
		str1.one = "one1";
		str1.two = "two1";
		boolean created = twoStringsManager.create(str1);
		assertTrue(created);

		TwoStrings str2 = new TwoStrings();
		created = twoStringsManager.create(str2);
		assertFalse(created);

		str2.one = str1.one;
		created = twoStringsManager.create(str2);
		assertFalse(created);

		str2.one = str1.one + "x";
		created = twoStringsManager.create(str2);
		assertTrue(created);

		Cursor cursor = twoStringsManager.select()
				.where(DB.Column.ID, Is.EQUAL, str1.id).execute();
		assertEquals(1, cursor.getCount());
		cursor.close();

		cursor = twoStringsManager.select().where("two", Is.NOT_NULL).execute();
		assertEquals(1, cursor.getCount());
		cursor.moveToFirst();
		TwoStrings str11 = twoStringsManager.readFromCursor(cursor);
		assertEquals(str1.one, str11.one);
		cursor.close();

		cursor = twoStringsManager.select().where("two", Is.NULL).execute();
		assertEquals(1, cursor.getCount());
		cursor.moveToFirst();
		TwoStrings str21 = twoStringsManager.readFromCursor(cursor);
		assertEquals(str2.one, str21.one);
		cursor.close();
		//
		deleteAllTwoStrings();
	}

	public void testInAndLike() throws Exception {
		ArrayList<TwoStrings> list = new ArrayList<TwoStrings>();
		for (String str : new String[] { "pc", "mac", "phone" }) {
			TwoStrings ts = new TwoStrings();
			ts.one = str;
			list.add(ts);
		}
		boolean success = twoStringsManager.create(list);
		assertTrue(success);
		//
		Cursor c = twoStringsManager.select().where(DB.Column.ID, Is.IN, 1, 2)
				.execute();
		assertEquals(2, c.getCount());
		c.close();
		//
		int[] arr = new int[] { 1, 2 };
		c = twoStringsManager.select().where(DB.Column.ID, Is.NOT_IN, arr)
				.execute();
		assertEquals(1, c.getCount());
		c.close();
		//
		c = twoStringsManager.select().where("one", Is.LIKE, "%%hon%%")
				.execute();
		assertEquals(1, c.getCount());
		c.close();
		//
		deleteAllTwoStrings();
	}

	private void deleteAllTwoStrings() {
		twoStringsManager.delete().execute();
	}

}
