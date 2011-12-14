/**
 * Copyright 2011 Alex Yanchenko
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
package org.droidparts.reflection.processor;

import static org.droidparts.reflection.util.TypeHelper.isBoolean;
import static org.droidparts.reflection.util.TypeHelper.isByte;
import static org.droidparts.reflection.util.TypeHelper.isDBModel;
import static org.droidparts.reflection.util.TypeHelper.isFloat;
import static org.droidparts.reflection.util.TypeHelper.isInteger;
import static org.droidparts.reflection.util.TypeHelper.isLong;
import static org.droidparts.reflection.util.TypeHelper.isShort;
import static org.droidparts.util.Strings.isEmpty;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.droidparts.annotation.sql.Column;
import org.droidparts.annotation.sql.Table;
import org.droidparts.model.DBModel;
import org.droidparts.reflection.model.DBField;
import org.droidparts.util.L;

public class DBAnnotationProcessor extends AbstractAnnotationProcessor {

	private static final String ID_SUFFIX = "_id";

	public DBAnnotationProcessor(Class<? extends DBModel> cls) {
		super(cls);
	}

	public String getTableName() {
		Table ann = cls.getAnnotation(Table.class);
		if (ann != null) {
			return ann.value();
		} else {
			return cls.getSimpleName();
		}
	}

	public DBField[] getFields() {
		ArrayList<DBField> list = new ArrayList<DBField>();
		for (Field field : getClassHierarchyFields()) {
			Column columnAnn = field.getAnnotation(Column.class);
			if (columnAnn != null) {
				DBField dbField = new DBField();
				dbField.fieldName = field.getName();
				dbField.fieldType = field.getType();
				dbField.columnName = getColumnName(columnAnn, field);
				dbField.columnNullable = columnAnn.nullable();
				dbField.columnUnique = columnAnn.unique();
				list.add(dbField);
			}
		}
		sanitizeFields(list);
		return list.toArray(new DBField[list.size()]);
	}

	private String getColumnName(Column ann, Field field) {
		String name = ann.name();
		if (isEmpty(name)) {
			name = field.getName();
		}
		if (isDBModel(field.getType()) && !name.endsWith(ID_SUFFIX)) {
			name += ID_SUFFIX;
		}
		return name;
	}

	private void sanitizeFields(ArrayList<DBField> fields) {
		for (DBField field : fields) {
			if (field.columnNullable) {
				Class<?> cls = field.fieldType;
				if (isBoolean(cls) || isByte(cls) || isFloat(cls)
						|| isInteger(cls) || isLong(cls) || isShort(cls)) {
					L.e(field.fieldType.getSimpleName() + " can't be null.");
					field.columnNullable = false;
				}
			}
		}
	}

}
