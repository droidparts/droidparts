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
package org.droidparts.inner.ann.bus;

import static org.droidparts.util.Strings.isEmpty;

import org.droidparts.annotation.bus.ReceiveEvents;
import org.droidparts.inner.ann.Ann;

public final class ReceiveEventsAnn extends Ann<ReceiveEvents> {

	public final String[] names;

	public ReceiveEventsAnn(ReceiveEvents annotation) {
		super(annotation);
		String[] names;
		if (hackSuccess()) {
			names = (String[]) getElement(NAME);
			cleanup();
		} else {
			names = annotation.name();
		}
		boolean none = (names.length == 1) && isEmpty(names[0]);
		if (none) {
			this.names = new String[0];
		} else {
			this.names = names;
		}
	}

}
