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

import android.test.ActivityInstrumentationTestCase2;
import android.view.LayoutInflater;

import org.droidparts.test.activity.TestActivity;

public abstract class TestActivityTest extends ActivityInstrumentationTestCase2<TestActivity> {

	public TestActivityTest() {
		super(TestActivity.class);
	}

	protected final LayoutInflater getLayoutInflater() {
		return LayoutInflater.from(getActivity());
	}

}
