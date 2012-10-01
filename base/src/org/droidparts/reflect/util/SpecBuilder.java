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

import static org.droidparts.reflect.util.AnnBuilder.getClassAnn;
import static org.droidparts.reflect.util.AnnBuilder.getFieldAnn;
import static org.droidparts.reflect.util.AnnBuilder.getFieldAnns;
import static org.droidparts.reflect.util.ReflectionUtils.getArrayType;
import static org.droidparts.reflect.util.ReflectionUtils.getFieldGenericArgs;
import static org.droidparts.reflect.util.ReflectionUtils.listAnnotatedFields;
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
import java.util.concurrent.ConcurrentHashMap;

import org.droidparts.model.Entity;
import org.droidparts.model.Model;
import org.droidparts.reflect.model.Ann;
import org.droidparts.reflect.model.inject.InjectSpec;
import org.droidparts.reflect.model.inject.ann.InjectAnn;
import org.droidparts.reflect.model.json.KeySpec;
import org.droidparts.reflect.model.json.ann.KeyAnn;
import org.droidparts.reflect.model.sql.ColumnSpec;
import org.droidparts.reflect.model.sql.ann.ColumnAnn;
import org.droidparts.reflect.model.sql.ann.TableAnn;
import org.droidparts.util.L;

public final class SpecBuilder {

	// Inject

	public static InjectSpec[] getInjectSpecs(Class<?> cls) {
		InjectSpec[] specs = injectSpecCache.get(cls);
		if (specs == null) {
			ArrayList<InjectSpec> list = new ArrayList<InjectSpec>();
			List<Field> fields = ReflectionUtils.listAnnotatedFields(cls);
			for (Field field : fields) {
				for (Ann<?> ann : getFieldAnns(cls, field)) {
					if (ann instanceof InjectAnn) {
						InjectSpec spec = new InjectSpec();
						spec.field = field;
						spec.ann = (InjectAnn<?>) ann;
						list.add(spec);
						break;
					}
				}
			}
			specs = list.toArray(new InjectSpec[list.size()]);
			injectSpecCache.put(cls, specs);
		}
		return specs;
	}

	// SQL

	public static String getTableName(Class<? extends Entity> cls) {
		String name = tableNameCache.get(cls);
		if (name == null) {
			TableAnn ann = (TableAnn) getClassAnn(TableAnn.class, cls);
			if (ann != null) {
				name = ann.name;
			}
			if (isEmpty(name)) {
				name = cls.getSimpleName();
			}
			tableNameCache.put(cls, name);
		}
		return name;
	}

	public static ColumnSpec[] getTableColumnSpecs(Class<? extends Entity> cls) {
		ColumnSpec[] specs = columnSpecCache.get(cls);
		if (specs == null) {
			ArrayList<ColumnSpec> list = new ArrayList<ColumnSpec>();
			for (Field field : listAnnotatedFields(cls)) {
				ColumnAnn columnAnn = (ColumnAnn) getFieldAnn(ColumnAnn.class,
						cls, field);
				if (columnAnn != null) {
					ColumnSpec spec = new ColumnSpec();
					spec.field = field;
					spec.multiFieldArgType = getMultiFieldArgType(field);
					spec.ann = new ColumnAnn();
					spec.ann.name = getColumnName(columnAnn, field);
					spec.ann.nullable = columnAnn.nullable;
					spec.ann.unique = columnAnn.unique;
					spec.ann.eager = columnAnn.eager;
					list.add(spec);
				}
			}
			sanitizeFields(list);
			specs = list.toArray(new ColumnSpec[list.size()]);
			columnSpecCache.put(cls, specs);
		}
		return specs;
	}

	// JSON

	public static KeySpec[] getJsonKeySpecs(Class<? extends Model> cls) {
		KeySpec[] specs = keySpecCache.get(cls);
		if (specs == null) {
			ArrayList<KeySpec> list = new ArrayList<KeySpec>();
			for (Field field : listAnnotatedFields(cls)) {
				KeyAnn keyAnn = (KeyAnn) getFieldAnn(KeyAnn.class, cls, field);
				if (keyAnn != null) {
					KeySpec spec = new KeySpec();
					spec.field = field;
					spec.multiFieldArgType = getMultiFieldArgType(field);
					spec.ann = new KeyAnn();
					spec.ann.name = getKeyName(keyAnn, field);
					spec.ann.optional = keyAnn.optional;
					list.add(spec);
				}
			}
			specs = list.toArray(new KeySpec[list.size()]);
			keySpecCache.put(cls, specs);
		}
		return specs;
	}

	// caches

	private static final ConcurrentHashMap<Class<?>, InjectSpec[]> injectSpecCache = new ConcurrentHashMap<Class<?>, InjectSpec[]>();

	private static final ConcurrentHashMap<Class<? extends Entity>, String> tableNameCache = new ConcurrentHashMap<Class<? extends Entity>, String>();
	private static final ConcurrentHashMap<Class<? extends Entity>, ColumnSpec[]> columnSpecCache = new ConcurrentHashMap<Class<? extends Entity>, ColumnSpec[]>();

	private static final ConcurrentHashMap<Class<? extends Model>, KeySpec[]> keySpecCache = new ConcurrentHashMap<Class<? extends Model>, KeySpec[]>();

	// Utils

	private static Class<?> getMultiFieldArgType(Field field) {
		Class<?> argType = null;
		Class<?> fieldType = field.getType();
		if (isArray(fieldType)) {
			argType = getArrayType(fieldType);
		} else if (isCollection(fieldType)) {
			Class<?>[] genericArgs = getFieldGenericArgs(field);
			argType = (genericArgs.length > 0) ? genericArgs[0] : Object.class;
		}
		return argType;
	}

	// JSON

	private static String getKeyName(KeyAnn ann, Field field) {
		String name = ann.name;
		if (isEmpty(name)) {
			name = field.getName();
		}
		return name;
	}

	// SQL

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
			if (spec.ann.nullable) {
				Class<?> fieldType = spec.field.getType();
				if (isBoolean(fieldType) || isByte(fieldType)
						|| isFloat(fieldType) || isInteger(fieldType)
						|| isLong(fieldType) || isShort(fieldType)) {
					L.e(fieldType.getSimpleName() + " can't be null.");
					spec.ann.nullable = false;
				}
			}
		}
	}

}
