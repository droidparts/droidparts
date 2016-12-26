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

import android.os.Bundle;

import org.droidparts.test.activity.TestActivity;

public class ActivitySaveInstanceStateTest extends TestActivityTest {

	public void testActivitySaveInstanceState() {
		final TestActivity activity = getActivity();
		final String data = "data";
		final Bundle b = new Bundle();
		Runnable test = new Runnable() {

			@Override
			public void run() {
				activity.data = data;
				activity.onSaveInstanceState(b);
				activity.data = null;
				activity.onCreate(b);
				assertEquals(data, activity.data);
			}
		};
		activity.runOnUiThread(test);
	}

}
