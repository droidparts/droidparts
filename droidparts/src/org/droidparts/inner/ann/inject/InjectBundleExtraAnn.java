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

import org.droidparts.annotation.inject.InjectBundleExtra;

public final class InjectBundleExtraAnn extends InjectAnn<InjectBundleExtra> {

	public final String key;
	public final boolean optional;

	public InjectBundleExtraAnn(InjectBundleExtra annotation) {
		super(annotation);
		if (hackSuccess()) {
			key = (String) getElement(KEY);
			optional = (Boolean) getElement(OPTIONAL);
			cleanup();
		} else {
			key = annotation.key();
			optional = annotation.optional();
		}
	}

}
