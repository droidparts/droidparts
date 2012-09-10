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
import static org.droidparts.reflect.util.TypeHelper.isEntity;
import static org.droidparts.reflect.util.TypeHelper.isFloat;
import static org.droidparts.reflect.util.TypeHelper.isInteger;
import static org.droidparts.reflect.util.TypeHelper.isLong;
import static org.droidparts.reflect.util.TypeHelper.isShort;
import static org.droidparts.util.Strings.isEmpty;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.droidparts.annotation.sql.Column;
import org.droidparts.annotation.sql.Table;
import org.droidparts.model.Entity;
import org.droidparts.reflect.model.EntityField;
import org.droidparts.util.L;

public class EntityAnnotationProcessor extends
		AbstractAnnotationProcessor<EntityField> {

	private static final String ID_SUFFIX = "_id";

	public static String toPKColumnName(String name) {
		if (!name.endsWith(ID_SUFFIX)) {
			name += ID_SUFFIX;
		}
		return name;
	}

	public EntityAnnotationProcessor(Class<? extends Entity> cls) {
		super(cls);
	}

	@Override
	protected String modelClassName() {
		Table ann = cls.getAnnotation(Table.class);
		if (ann != null) {
			return ann.value();
		} else {
			return cls.getSimpleName();
		}
	}

	@Override
	protected EntityField[] modelClassFields() {
		ArrayList<EntityField> list = new ArrayList<EntityField>();
		for (Field field : getClassHierarchyFields()) {
			Column columnAnn = field.getAnnotation(Column.class);
			if (columnAnn != null) {
				EntityField dbField = new EntityField();
				fillField(field, dbField);
				dbField.columnName = getColumnName(columnAnn, field);
				dbField.columnNullable = columnAnn.nullable();
				dbField.columnUnique = columnAnn.unique();
				dbField.columnEager = columnAnn.eager();
				list.add(dbField);
			}
		}
		sanitizeFields(list);
		return list.toArray(new EntityField[list.size()]);
	}

	private String getColumnName(Column ann, Field field) {
		String name = ann.name();
		if (isEmpty(name)) {
			name = field.getName();
		}
		if (isEntity(field.getType())) {
			name = toPKColumnName(name);
		}
		return name;
	}

	private void sanitizeFields(ArrayList<EntityField> fields) {
		for (EntityField field : fields) {
			if (field.columnNullable) {
				Class<?> fieldType = field.fieldType;
				if (isBoolean(fieldType) || isByte(fieldType)
						|| isFloat(fieldType) || isInteger(fieldType)
						|| isLong(fieldType) || isShort(fieldType)) {
					L.e(fieldType.getSimpleName() + " can't be null.");
					field.columnNullable = false;
				}
			}
		}
	}

}
