/**
 * Copyright 2016 Alex Yanchenko
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
package org.droidparts.test.testcase.activity;

import org.droidparts.Injector;
import org.droidparts.persist.sql.AbstractDBOpenHelper;
import org.droidparts.test.R;
import org.droidparts.test.activity.TestActivity;

public class ActivityInjectTest extends TestActivityTest {

	public void testInjection() throws Exception {
		TestActivity activity = getActivity();
		String testString = activity.getString(R.string.test_string);

		assertEquals(testString, activity.testString);
		assertNotNull(activity.textView);
		assertEquals(testString, activity.textView.getText());

		assertNotNull(activity.testFragment);
	}

	public void testInjection2() {
		Class<AbstractDBOpenHelper> cls = AbstractDBOpenHelper.class;
		AbstractDBOpenHelper dependency = Injector.getDependency(getActivity(), cls);
		assertNotNull(dependency);
		assertTrue(cls.isAssignableFrom(dependency.getClass()));
	}

}
