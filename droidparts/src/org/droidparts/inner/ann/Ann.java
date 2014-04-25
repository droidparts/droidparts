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
package org.droidparts.inner.ann;

import java.lang.annotation.Annotation;
import java.util.HashMap;

import org.droidparts.inner.AnnotationElementsReader;
import org.droidparts.util.L;

public abstract class Ann<T extends Annotation> {

	protected static final String NAME = "name";
	protected static final String VALUE = "value";
	protected static final String ID = "id";
	protected static final String CLICK = "click";
	protected static final String KEY = "key";
	protected static final String OPTIONAL = "optional";
	protected static final String NULLABLE = "nullable";
	protected static final String UNIQUE = "unique";
	protected static final String EAGER = "eager";

	private static boolean hackSuccess = true;

	private HashMap<String, Object> elements;

	public Ann(T annotation) {
		if (hackSuccess) {
			try {
				elements = AnnotationElementsReader.getElements(annotation);
			} catch (Exception e) {
				L.w(e);
				hackSuccess = false;
			}
		}
	}

	protected final boolean hackSuccess() {
		return hackSuccess;
	}

	protected final Object getElement(String name) {
		return elements.get(name);
	}

	protected final void cleanup() {
		elements = null;
	}

}