/**
 * Copyright 2012 Alex Yanchenko
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
package org.droidparts.reflect.model.inject.ann;

import org.droidparts.annotation.inject.InjectView;

public final class InjectViewAnn extends InjectAnn<InjectView> {

	public final int value;

	public InjectViewAnn(InjectView annotation) {
		super(InjectView.class);
		value = annotation.value();
	}
}
