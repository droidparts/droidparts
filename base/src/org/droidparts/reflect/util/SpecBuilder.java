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
package org.droidparts.reflect.util;

import static org.droidparts.reflect.util.AnnUtil.getFieldAnns;
import static org.droidparts.reflect.util.ReflectionUtils.getArrayType;
import static org.droidparts.reflect.util.ReflectionUtils.getFieldGenericArgs;
import static org.droidparts.reflect.util.TypeHelper.isArray;
import static org.droidparts.reflect.util.TypeHelper.isBoolean;
import static org.droidparts.reflect.util.TypeHelper.isByte;
import static org.droidparts.reflect.util.TypeHelper.isCollection;
import static org.droidparts.reflect.util.TypeHelper.isFloat;
import static org.droidparts.reflect.util.TypeHelper.isInteger;
import static org.droidparts.reflect.util.TypeHelper.isLong;
import static org.droidparts.reflect.util.TypeHelper.isShort;
import static org.droidparts.util.Strings.isEmpty;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.droidparts.model.Entity;
import org.droidparts.model.Model;
import org.droidparts.reflect.model.Ann;
import org.droidparts.reflect.model.inject.InjectSpec;
import org.droidparts.reflect.model.inject.ann.InjectAnn;
import org.droidparts.reflect.model.json.KeySpec;
import org.droidparts.reflect.model.json.ann.KeyAnn;
import org.droidparts.reflect.model.json.ann.ObjectAnn;
import org.droidparts.reflect.model.sql.ColumnSpec;
import org.droidparts.reflect.model.sql.ann.ColumnAnn;
import org.droidparts.reflect.model.sql.ann.TableAnn;
import org.droidparts.util.L;

public final class SpecBuilder {

	public static InjectSpec[] getInjectSpecs(Class<?> cls) {
		ArrayList<InjectSpec> list = new ArrayList<InjectSpec>();
		List<Field> fields = AnnUtil.listAnnotatedFields(cls);
		for (Field field : fields) {
			for (Ann<?> ann : getFieldAnns(cls, field)) {
				if (ann instanceof InjectAnn) {
					InjectSpec spec = new InjectSpec();
					spec.field = field;
					spec.injectAnn = (InjectAnn<?>) ann;
					list.add(spec);
					break;
				}
			}
		}
		return list.toArray(new InjectSpec[list.size()]);
	}

	public static String getTableName(Class<? extends Entity> cls) {
		String name = null;
		TableAnn ann = (TableAnn) AnnUtil.getClassAnn(TableAnn.class, cls);
		if (ann != null) {
			name = ann.name;
		}
		if (isEmpty(name)) {
			name = cls.getSimpleName();
		}
		return name;
	}

	public static ColumnSpec[] getTableColumns(Class<? extends Entity> cls) {
		ArrayList<ColumnSpec> list = new ArrayList<ColumnSpec>();
		for (Field field : getClassHierarchyFields(cls)) {
			ColumnAnn columnAnn = (ColumnAnn) AnnUtil.getFieldAnn(
					ColumnAnn.class, cls, field);
			if (columnAnn != null) {
				ColumnSpec spec = new ColumnSpec();
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
		return list.toArray(new ColumnSpec[list.size()]);
	}

	public static String getJsonObjectName(Class<? extends Model> cls) {
		String name = null;
		ObjectAnn ann = (ObjectAnn) AnnUtil.getClassAnn(ObjectAnn.class, cls);
		if (ann != null) {
			name = ann.name;
		}
		if (isEmpty(name)) {
			name = cls.getSimpleName();
		}
		return name;
	}

	public static KeySpec[] getJsonKeySpecs(Class<? extends Model> cls) {
		ArrayList<KeySpec> list = new ArrayList<KeySpec>();
		for (Field field : getClassHierarchyFields(cls)) {
			KeyAnn keyAnn = (KeyAnn) AnnUtil.getFieldAnn(KeyAnn.class, cls,
					field);
			if (keyAnn != null) {
				KeySpec spec = new KeySpec();
				spec.field = field;
				spec.multiFieldArgType = getMultiFieldArgType(field);
				spec.key.name = getKeyName(keyAnn, field);
				spec.key.optional = keyAnn.optional;
				list.add(spec);
			}
		}
		return list.toArray(new KeySpec[list.size()]);
	}

	//

	private static List<Field> getClassHierarchyFields(Class<?> cls) {
		return AnnUtil.listAnnotatedFields(cls);
	}

	private static Class<?> getMultiFieldArgType(Field field) {
		Class<?> cls = null;
		Class<?> fieldType = field.getType();
		if (isArray(fieldType)) {
			cls = getArrayType(fieldType);
		} else if (isCollection(fieldType)) {
			Class<?>[] genericArgs = getFieldGenericArgs(field);
			cls = (genericArgs.length > 0) ? genericArgs[0] : Object.class;
		}
		return cls;
	}

	//

	private static String getKeyName(KeyAnn ann, Field field) {
		String name = ann.name;
		if (isEmpty(name)) {
			name = field.getName();
		}
		return name;
	}

	//

	private static String getColumnName(ColumnAnn ann, Field field) {
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

	private static void sanitizeFields(ArrayList<ColumnSpec> entitySpecs) {
		for (ColumnSpec spec : entitySpecs) {
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
