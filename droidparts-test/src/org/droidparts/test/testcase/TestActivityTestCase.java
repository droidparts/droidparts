/**
 * Copyright 2014 Alex Yanchenko
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.droidparts.test.testcase;

import org.droidparts.Injector;
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
		SQLiteDatabase dependency = Injector.getDependency(getActivity(), cls);
		assertNotNull(dependency);
		assertTrue(cls.isAssignableFrom(dependency.getClass()));
	}

}
