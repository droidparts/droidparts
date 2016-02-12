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
package org.droidparts.test.testcase;

import org.droidparts.bus.EventBus;
import org.droidparts.bus.EventReceiver;

import android.test.AndroidTestCase;

public class EventBusTestCase extends AndroidTestCase {

	private final String NAME = "name";
	private final String DATA = "data";

	private boolean calledBack;

	EventReceiver<Object> er = new EventReceiver<Object>() {

		@Override
		public void onEvent(String name, Object data) {
			calledBack = true;
			assertEquals(NAME, name);
			assertEquals(DATA, data);
		}
	};

	protected void tearDown() throws Exception {
		super.tearDown();
		EventBus.unregisterReceiver(er);
	}

	public void testEvent() {
		EventBus.registerReceiver(er);
		EventBus.postEvent(NAME, DATA);
		sleep();
		assertTrue(calledBack);
	}

	public void testStikyEvent() {
		EventBus.postEventSticky(NAME, DATA);
		sleep();
		EventBus.registerReceiver(er);
		sleep();
		assertTrue(calledBack);
	}

	private void sleep() {
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}
