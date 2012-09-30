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
package org.droidparts.reflect.model;

import java.lang.annotation.Annotation;

public class Ann<T extends Annotation> {

	public final Class<? extends Annotation> cls;

	public Ann(Class<T> cls) {
		this.cls = cls;
	}

	@Override
	public int hashCode() {
		return cls.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (o instanceof Ann) {
			return cls.equals(((Ann<?>) o).cls);
		}
		return false;
	}

	@Override
	public String toString() {
		return cls.toString();
	}
}