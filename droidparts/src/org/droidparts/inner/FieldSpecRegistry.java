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
package org.droidparts.inner;

import static org.droidparts.inner.AnnBuilder.getClassAnn;
import static org.droidparts.inner.AnnBuilder.getFieldAnn;
import static org.droidparts.inner.AnnBuilder.getFieldAnns;
import static org.droidparts.inner.ReflectionUtils.getArrayComponentType;
import static org.droidparts.inner.ReflectionUtils.getFieldGenericArgs;
import static org.droidparts.inner.ReflectionUtils.listAnnotatedFields;
import static org.droidparts.inner.TypeHelper.isArray;
import static org.droidparts.inner.TypeHelper.isBoolean;
import static org.droidparts.inner.TypeHelper.isByte;
import static org.droidparts.inner.TypeHelper.isCharacter;
import static org.droidparts.inner.TypeHelper.isCollection;
import static org.droidparts.inner.TypeHelper.isDouble;
import static org.droidparts.inner.TypeHelper.isEntity;
import static org.droidparts.inner.TypeHelper.isFloat;
import static org.droidparts.inner.TypeHelper.isInteger;
import static org.droidparts.inner.TypeHelper.isLong;
import static org.droidparts.inner.TypeHelper.isShort;
import static org.droidparts.util.Strings.isEmpty;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.droidparts.inner.ann.Ann;
import org.droidparts.inner.ann.FieldSpec;
import org.droidparts.inner.ann.inject.InjectAnn;
import org.droidparts.inner.ann.json.KeyAnn;
import org.droidparts.inner.ann.sql.ColumnAnn;
import org.droidparts.inner.ann.sql.TableAnn;
import org.droidparts.model.Entity;
import org.droidparts.model.Model;
import org.droidparts.util.L;

public final class FieldSpecRegistry {

	// Inject

	@SuppressWarnings("unchecked")
	public static FieldSpec<InjectAnn<?>>[] getInjectSpecs(Class<?> cls) {
		FieldSpec<InjectAnn<?>>[] specs = injectSpecCache.get(cls);
		if (specs == null) {
			ArrayList<FieldSpec<InjectAnn<?>>> list = new ArrayList<FieldSpec<InjectAnn<?>>>();
			List<Field> fields = ReflectionUtils.listAnnotatedFields(cls);
			for (Field field : fields) {
				for (Ann<?> ann : getFieldAnns(cls, field)) {
					if (ann instanceof InjectAnn) {
						list.add(new FieldSpec<InjectAnn<?>>(field, null,
								(InjectAnn<?>) ann));
						break;
					}
				}
			}
			specs = list.toArray(new FieldSpec[list.size()]);
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

	@SuppressWarnings("unchecked")
	public static FieldSpec<ColumnAnn>[] getTableColumnSpecs(
			Class<? extends Entity> cls) {
		FieldSpec<ColumnAnn>[] specs = columnSpecCache.get(cls);
		if (specs == null) {
			ArrayList<FieldSpec<ColumnAnn>> list = new ArrayList<FieldSpec<ColumnAnn>>();
			for (Field field : listAnnotatedFields(cls)) {
				ColumnAnn columnAnn = (ColumnAnn) getFieldAnn(ColumnAnn.class,
						cls, field);
				if (columnAnn != null) {
					Class<?> componentType = getComponentType(field);
					ColumnAnn ann = new ColumnAnn();
					ann.name = getColumnName(columnAnn, field);
					ann.nullable = columnAnn.nullable;
					ann.unique = columnAnn.unique;
					ann.eager = columnAnn.eager;
					list.add(new FieldSpec<ColumnAnn>(field, componentType, ann));
				}
			}
			sanitizeSpecs(list);
			specs = list.toArray(new FieldSpec[list.size()]);
			columnSpecCache.put(cls, specs);
		}
		return specs;
	}

	// JSON
	@SuppressWarnings("unchecked")
	public static FieldSpec<KeyAnn>[] getJsonKeySpecs(Class<? extends Model> cls) {
		FieldSpec<KeyAnn>[] specs = keySpecCache.get(cls);
		if (specs == null) {
			ArrayList<FieldSpec<KeyAnn>> list = new ArrayList<FieldSpec<KeyAnn>>();
			for (Field field : listAnnotatedFields(cls)) {
				KeyAnn keyAnn = (KeyAnn) getFieldAnn(KeyAnn.class, cls, field);
				if (keyAnn != null) {
					Class<?> componentType = getComponentType(field);
					KeyAnn ann = new KeyAnn();
					ann.name = getKeyName(keyAnn, field);
					ann.optional = keyAnn.optional;
					list.add(new FieldSpec<KeyAnn>(field, componentType,
							(KeyAnn) ann));
				}
			}
			specs = list.toArray(new FieldSpec[list.size()]);
			keySpecCache.put(cls, specs);
		}
		return specs;
	}

	// caches

	private static final ConcurrentHashMap<Class<?>, FieldSpec<InjectAnn<?>>[]> injectSpecCache = new ConcurrentHashMap<Class<?>, FieldSpec<InjectAnn<?>>[]>();

	private static final ConcurrentHashMap<Class<? extends Entity>, String> tableNameCache = new ConcurrentHashMap<Class<? extends Entity>, String>();
	private static final ConcurrentHashMap<Class<? extends Entity>, FieldSpec<ColumnAnn>[]> columnSpecCache = new ConcurrentHashMap<Class<? extends Entity>, FieldSpec<ColumnAnn>[]>();

	private static final ConcurrentHashMap<Class<? extends Model>, FieldSpec<KeyAnn>[]> keySpecCache = new ConcurrentHashMap<Class<? extends Model>, FieldSpec<KeyAnn>[]>();

	// Utils

	private static Class<?> getComponentType(Field field) {
		Class<?> componentType = null;
		Class<?> fieldType = field.getType();
		if (isArray(fieldType)) {
			componentType = getArrayComponentType(fieldType);
		} else if (isCollection(fieldType)) {
			Class<?>[] genericArgs = getFieldGenericArgs(field);
			componentType = (genericArgs.length > 0) ? genericArgs[0]
					: Object.class;
		}
		return componentType;
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
			if (isEntity(field.getType()) && !name.endsWith(ID_SUFFIX)) {
				name += ID_SUFFIX;
			}
		}
		return name;
	}

	private static final String ID_SUFFIX = "_id";

	private static void sanitizeSpecs(
			ArrayList<FieldSpec<ColumnAnn>> columnSpecs) {
		for (FieldSpec<ColumnAnn> spec : columnSpecs) {
			Class<?> fieldType = spec.field.getType();
			if (spec.ann.nullable) {
				if (isByte(fieldType) || isShort(fieldType)
						|| isInteger(fieldType) || isLong(fieldType)
						|| isFloat(fieldType) || isDouble(fieldType)
						|| isBoolean(fieldType) || isCharacter(fieldType)) {
					L.w("%s can't be null.", fieldType.getSimpleName());
					spec.ann.nullable = false;
				}
			} else if (spec.ann.eager) {
				if (!isEntity(fieldType) && !isEntity(spec.componentType)) {
					L.w("%s can't be eager.", fieldType.getSimpleName());
					spec.ann.eager = false;
				}
			}
		}
	}

}
