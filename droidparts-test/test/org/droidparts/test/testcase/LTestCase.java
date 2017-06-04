/**
 * Copyright 2017 Alex Yanchenko
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

import java.util.Arrays;
import java.util.List;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.droidparts.util.L;

import static org.junit.Assert.assertEquals;


@RunWith(AndroidJUnit4.class)
public class LTestCase implements L.Listener {

	private int priority;
	private String tag;
	private String msg;


	@Override
	public void onMessageLogged(int priority, String tag, String msg) {
		this.priority = priority;
		this.tag = tag;
		this.msg = msg;
	}

	@Before
	public void setUp() throws Exception {
		L.setListener(this);
		priority = -1;
		tag = msg = null;
	}

	private static final String MSG = "MSG";
	private static final String MSG_FORMAT_1 = "MSG %s";

	@Test
	public void testLog() {
		L.v(MSG);
		assertEquals(MSG, msg);
		L.v(MSG_FORMAT_1);
		assertEquals(MSG_FORMAT_1, msg);
		L.v(MSG_FORMAT_1, 5);
		assertEquals(String.format(MSG_FORMAT_1, 5), msg);
	}

	@Test
	public void testArrayCollectionMessage() {
		Integer[] arr = new Integer[]{100, 500};
		List<Integer> list = Arrays.asList(arr);
		L.v(arr);
		String tmpMsg = msg;
		msg = null;
		L.v(list);
		assertEquals(tmpMsg, msg);
	}

	@Test
	public void testTag() {
		L.v(MSG_FORMAT_1, 1);
		String prevTag = tag;
		tag = null;
		L.v(new Object());
		assertEquals(prevTag.substring(0, prevTag.indexOf(':')), tag.substring(0, tag.indexOf(':')));
	}

}
