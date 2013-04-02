/**
 * Copyright 2013 Alex Yanchenko
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
package org.droidparts.model;

import static org.droidparts.inner.ReflectionUtils.getFieldVal;
import static org.droidparts.inner.ReflectionUtils.listAnnotatedFields;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

import org.droidparts.util.L;

public abstract class Model implements Serializable {
	private static final long serialVersionUID = 1L;

	public Model() {
		// subclasses must have a no-arg constructor
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append(" [");
		List<Field> fields = listAnnotatedFields(getClass());
		for (int i = 0; i < fields.size(); i++) {
			if (i > 0) {
				sb.append(", ");
			}
			Field field = fields.get(i);
			sb.append(field.getName());
			sb.append(": ");
			try {
				sb.append(getFieldVal(this, field));
			} catch (Exception e) {
				L.d(e);
				sb.append("n/a");
			}
		}
		sb.append("]");
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o != null && getClass() == o.getClass()) {
			return hashCode() == o.hashCode();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

}
