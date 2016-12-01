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
import android.view.LayoutInflater;

import org.droidparts.test.activity.TestFragment;
import org.droidparts.test.activity.TestFragment.KV;

public class FragmentTest extends TestActivityTest {

	private Bundle args, state;
	private String str;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		args = new Bundle();
		state = new Bundle();
		str = "str";
		args.putString(TestFragment.EXTRA_STR, str);

	}

	public void testInjectArgument() {
		TestFragment tf = makeFragment();
		tf.onCreate(null);
		assertFalse(tf.isInjected());
		assertNull(tf.str);
		tf.onCreateView(getLayoutInflater(), null, null);
		assertTrue(tf.isInjected());
		assertEquals(str, tf.str);
	}

	public void testSaveInstanceNonInjected() {
		TestFragment tf = makeFragment();
		tf.onCreate(null);
		assertNull(tf.str);
		tf.onSaveInstanceState(state);
		tf.onCreateView(getLayoutInflater(), null, state);
		assertEquals("str", tf.str);
	}

	public void testSaveInstanceInjected() {
		TestFragment tf = makeFragment();
		tf.onCreateView(LayoutInflater.from(getActivity()), null, null);
		assertEquals(str, tf.str);
		tf.str = "changed";
		tf.onSaveInstanceState(state);
		tf.str = null;
		tf.onCreateView(getLayoutInflater(), null, state);
		assertEquals("changed", tf.str);
	}

	public void testSaveInstanceInjectedNested() {
		TestFragment tf = makeFragment();
		tf.onCreateView(LayoutInflater.from(getActivity()), null, null);
		tf.map.put(1, new KV<String, String>("k", "v"));
		tf.onSaveInstanceState(state);
		tf.map = null;
		tf.onCreateView(getLayoutInflater(), null, state);
		assertNotNull(tf.map);
		assertTrue(tf.map.containsKey(1));
		assertEquals("v", tf.map.get(1).v);
	}

	private TestFragment makeFragment() {
		TestFragment tf = new TestFragment();
		tf.setArguments(args);
		return tf;
	}

}
