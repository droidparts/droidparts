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

import org.droidparts.annotation.bus.ReceiveEvents;
import org.droidparts.bus.EventBus;
import org.droidparts.bus.EventReceiver;

import android.test.AndroidTestCase;

public class EventBusTestCase extends AndroidTestCase {

	private final String NAME = "name";
	private final String DATA = "data";

	private int calledBackTimes = 0;

	private final EventReceiver<Object> er = new EventReceiver<Object>() {

		@Override
		public void onEvent(String name, Object data) {
			calledBackTimes++;
			assertEquals(NAME, name);
			assertEquals(DATA, data);
		}
	};

	class AnnotatedParent {

		int parentCalledBackTimes = 0;

		@ReceiveEvents
		private void onEvent() {
			parentCalledBackTimes++;
		}
	}

	class AnnotatedChild extends AnnotatedParent {

		int childCalledBackTimes = 0;

		@ReceiveEvents(name = NAME)
		private void onEvent() {
			childCalledBackTimes++;
		}
	}

	private final AnnotatedChild aer = new AnnotatedChild();

	@Override
	protected void tearDown() throws Exception {
		EventBus.unregisterReceiver(er);
		EventBus.unregisterAnnotatedReceiver(aer);
	}

	public void testEvent() {
		EventBus.registerReceiver(er);
		EventBus.postEvent(NAME, DATA);
		sleep();
		assertEquals(1, calledBackTimes);
	}

	public void testStikyEvent() {
		EventBus.postEventSticky(NAME, DATA);
		sleep();
		EventBus.registerReceiver(er);
		sleep();
		assertEquals(1, calledBackTimes);
	}

	public void testAnnotatedReceiver() {
		for (int i = 0; i < 5; i++) {
			EventBus.registerAnnotatedReceiver(aer);
		}
		EventBus.postEvent("whatever");
		for (int i = 0; i < 2; i++) {
			EventBus.postEvent(NAME, DATA);
		}
		sleep();
		assertEquals(3, aer.parentCalledBackTimes);
		assertEquals(2, aer.childCalledBackTimes);
	}

	private void sleep() {
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}
