package org.droidparts.test.testcase;

import org.droidparts.inject.Injector;
import org.droidparts.test.R;
import org.droidparts.test.activity.TestActivity;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.test.ActivityInstrumentationTestCase2;

public class TestActivityTestCase extends
		ActivityInstrumentationTestCase2<TestActivity> {

	public TestActivityTestCase() {
		super(TestActivity.class);
	}

	public void testInjection() throws Exception {
		Context ctx = getInstrumentation().getTargetContext();
		TestActivity activity = getActivity();
		String testString = ctx.getString(R.string.test_string);

		assertEquals(testString, activity.testString);
		assertNotNull(activity.textView);
		assertEquals(testString, activity.textView.getText());
	}

	public void testInjection2() {
		Class<SQLiteDatabase> cls = SQLiteDatabase.class;
		SQLiteDatabase dependency = Injector.get().getDependency(getActivity(),
				cls);
		assertNotNull(dependency);
		assertTrue(cls.isAssignableFrom(dependency.getClass()));
	}

}
