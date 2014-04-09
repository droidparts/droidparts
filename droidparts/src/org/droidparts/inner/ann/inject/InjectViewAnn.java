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
package org.droidparts.inner.ann.inject;

import org.droidparts.annotation.inject.InjectView;

public final class InjectViewAnn extends InjectAnn<InjectView> {

	public final int id;
	public final boolean click;

	public InjectViewAnn(InjectView annotation) {
		super(annotation);
		if (hackSuccess()) {
			id = (Integer) getElement(ID);
			click = (Boolean) getElement(CLICK);
			cleanup();
		} else {
			id = annotation.id();
			click = annotation.click();
		}
	}

}
