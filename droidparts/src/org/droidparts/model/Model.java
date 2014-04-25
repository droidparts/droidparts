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
package org.droidparts.model;

import static org.droidparts.inner.ClassSpecRegistry.getJsonKeySpecs;
import static org.droidparts.inner.ClassSpecRegistry.getTableColumnSpecs;
import static org.droidparts.inner.ReflectionUtils.getFieldVal;
import static org.droidparts.util.Strings.join;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import org.droidparts.inner.ann.FieldSpec;
import org.droidparts.inner.ann.json.KeyAnn;
import org.droidparts.inner.ann.sql.ColumnAnn;

public abstract class Model implements Serializable {
	private static final long serialVersionUID = 1L;

	public Model() {
		// Subclasses must have a no-argument constructor
		// and should override hashCode() and equals().
	}

	@Override
	public boolean equals(Object o) {
		boolean eq = false;
		if (this == o) {
			eq = true;
		} else if (o != null && getClass() == o.getClass()) {
			eq = (hashCode() == o.hashCode());
		}
		return eq;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public String toString() {
		LinkedHashSet<Field> fields = new LinkedHashSet<Field>();
		for (FieldSpec<KeyAnn> spec : getJsonKeySpecs(getClass())) {
			fields.add(spec.field);
		}
		if (this instanceof Entity) {
			for (FieldSpec<ColumnAnn> spec : getTableColumnSpecs((Class<? extends Entity>) getClass())) {
				fields.add(spec.field);
			}
		}
		StringBuilder sb = new StringBuilder();
		ArrayList<String> fieldVals = new ArrayList<String>();
		for (Field field : fields) {
			sb.append(field.getName()).append(": ");
			try {
				sb.append(getFieldVal(this, field));
			} catch (Exception e) {
				sb.append("n/a");
			}
			fieldVals.add(sb.toString());
			sb.setLength(0);
		}
		sb.append(getClass().getSimpleName());
		sb.append(" [");
		sb.append(join(fieldVals, ", "));
		sb.append("]");
		return sb.toString();
	}

}
