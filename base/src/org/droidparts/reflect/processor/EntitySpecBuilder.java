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
package org.droidparts.reflect.processor;

import static org.droidparts.reflect.util.TypeHelper.isBoolean;
import static org.droidparts.reflect.util.TypeHelper.isByte;
import static org.droidparts.reflect.util.TypeHelper.isFloat;
import static org.droidparts.reflect.util.TypeHelper.isInteger;
import static org.droidparts.reflect.util.TypeHelper.isLong;
import static org.droidparts.reflect.util.TypeHelper.isShort;
import static org.droidparts.util.Strings.isEmpty;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.droidparts.model.Entity;
import org.droidparts.reflect.model.sql.EntitySpec;
import org.droidparts.reflect.model.sql.ann.ColumnAnn;
import org.droidparts.reflect.model.sql.ann.TableAnn;
import org.droidparts.reflect.util.AnnUtil;
import org.droidparts.util.L;

public class EntityAnnotationProcessor extends
		AbstractAnnotationProcessor<EntitySpec> {

	public EntityAnnotationProcessor(Class<? extends Entity> cls) {
		super(cls);
	}

	@Override
	protected String modelClassName() {
		String name = null;
		TableAnn ann = (TableAnn) AnnUtil.getClassAnn(cls, TableAnn.class);
		if (ann != null) {
			name = ann.name;
		}
		if (isEmpty(name)) {
			name = cls.getSimpleName();
		}
		return name;
	}

	@Override
	protected EntitySpec[] modelClassFields() {
		ArrayList<EntitySpec> list = new ArrayList<EntitySpec>();
		for (Field field : getClassHierarchyFields()) {
			ColumnAnn columnAnn = (ColumnAnn) AnnUtil.getFieldAnn(cls, field,
					ColumnAnn.class);
			if (columnAnn != null) {
				EntitySpec spec = new EntitySpec();
				spec.field = field;
				spec.multiFieldArgType = getMultiFieldArgType(field);
				spec.column.name = getColumnName(columnAnn, field);
				spec.column.nullable = columnAnn.nullable;
				spec.column.unique = columnAnn.unique;
				spec.column.eager = columnAnn.eager;
				list.add(spec);
			}
		}
		sanitizeFields(list);
		return list.toArray(new EntitySpec[list.size()]);
	}

	private String getColumnName(ColumnAnn ann, Field field) {
		String name = ann.name;
		if (isEmpty(name)) {
			name = field.getName();
			if (!name.endsWith(ID_SUFFIX)) {
				name += ID_SUFFIX;
			}
		}
		return name;
	}

	private static final String ID_SUFFIX = "_id";

	private void sanitizeFields(ArrayList<EntitySpec> entitySpecs) {
		for (EntitySpec spec : entitySpecs) {
			if (spec.column.nullable) {
				Class<?> fieldType = spec.field.getType();
				if (isBoolean(fieldType) || isByte(fieldType)
						|| isFloat(fieldType) || isInteger(fieldType)
						|| isLong(fieldType) || isShort(fieldType)) {
					L.e(fieldType.getSimpleName() + " can't be null.");
					spec.column.nullable = false;
				}
			}
		}
	}

}
