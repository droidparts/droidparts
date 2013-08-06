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

import static org.droidparts.inner.AnnBuilder.getClassAnns;
import static org.droidparts.inner.AnnBuilder.getFieldAnns;
import static org.droidparts.inner.AnnBuilder.getMethodAnns;
import static org.droidparts.inner.ReflectionUtils.buildClassHierarchy;
import static org.droidparts.inner.ReflectionUtils.getArrayComponentType;
import static org.droidparts.inner.ReflectionUtils.getFieldGenericArgs;
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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.droidparts.inner.ann.Ann;
import org.droidparts.inner.ann.FieldSpec;
import org.droidparts.inner.ann.MethodSpec;
import org.droidparts.inner.ann.bus.ReceiveEventsAnn;
import org.droidparts.inner.ann.inject.InjectAnn;
import org.droidparts.inner.ann.json.KeyAnn;
import org.droidparts.inner.ann.sql.ColumnAnn;
import org.droidparts.inner.ann.sql.TableAnn;
import org.droidparts.model.Entity;
import org.droidparts.model.Model;
import org.droidparts.util.L;

public final class ClassSpecRegistry {

	// Inject

	@SuppressWarnings("unchecked")
	public static FieldSpec<InjectAnn<?>>[] getInjectSpecs(Class<?> cls) {
		FieldSpec<InjectAnn<?>>[] specs = INJECT_SPECS.get(cls);
		if (specs == null) {
			ArrayList<FieldSpec<InjectAnn<?>>> list = new ArrayList<FieldSpec<InjectAnn<?>>>();
			for (Class<?> cl : buildClassHierarchy(cls)) {
				for (Field field : cl.getDeclaredFields()) {
					for (Ann<?> ann : getFieldAnns(field)) {
						if (ann instanceof InjectAnn) {
							list.add(new FieldSpec<InjectAnn<?>>(field, null,
									(InjectAnn<?>) ann));
							break;
						}
					}
				}
			}
			specs = list.toArray(new FieldSpec[list.size()]);
			INJECT_SPECS.put(cls, specs);
		}
		return specs;
	}

	// Bus

	@SuppressWarnings("unchecked")
	public static MethodSpec<ReceiveEventsAnn>[] getReceiveEventsSpecs(
			Class<?> cls) {
		MethodSpec<ReceiveEventsAnn>[] specs = RECEIVE_EVENTS_SPECS.get(cls);
		if (specs == null) {
			ArrayList<MethodSpec<ReceiveEventsAnn>> list = new ArrayList<MethodSpec<ReceiveEventsAnn>>();
			for (Class<?> cl : buildClassHierarchy(cls)) {
				for (Method method : cl.getDeclaredMethods()) {
					for (Ann<?> ann : getMethodAnns(method)) {
						if (ann instanceof ReceiveEventsAnn) {
							list.add(new MethodSpec<ReceiveEventsAnn>(method,
									(ReceiveEventsAnn) ann));
							break;
						}
					}
				}
			}
			specs = list.toArray(new MethodSpec[list.size()]);
			RECEIVE_EVENTS_SPECS.put(cls, specs);
		}
		return specs;
	}

	// SQL

	public static String getTableName(Class<? extends Entity> cls) {
		String name = TABLE_NAMES.get(cls);
		if (name == null) {
			for (Ann<?> ann : getClassAnns(cls)) {
				if (ann instanceof TableAnn) {
					name = ((TableAnn) ann).name;
					break;
				}
			}
			if (isEmpty(name)) {
				name = cls.getSimpleName();
			}
			TABLE_NAMES.put(cls, name);
		}
		return name;
	}

	@SuppressWarnings("unchecked")
	public static FieldSpec<ColumnAnn>[] getTableColumnSpecs(
			Class<? extends Entity> cls) {
		FieldSpec<ColumnAnn>[] specs = COLUMN_SPECS.get(cls);
		if (specs == null) {
			ArrayList<FieldSpec<ColumnAnn>> list = new ArrayList<FieldSpec<ColumnAnn>>();
			for (Class<?> cl : buildClassHierarchy(cls)) {
				for (Field field : cl.getDeclaredFields()) {
					for (Ann<?> ann : getFieldAnns(field)) {
						if (ann instanceof ColumnAnn) {
							ColumnAnn columnAnn = (ColumnAnn) ann;
							Class<?> componentType = getComponentType(field);
							columnAnn.name = getColumnName(columnAnn, field);
							list.add(new FieldSpec<ColumnAnn>(field,
									componentType, columnAnn));
							break;
						}
					}
				}
			}
			sanitizeSpecs(list);
			specs = list.toArray(new FieldSpec[list.size()]);
			COLUMN_SPECS.put(cls, specs);
		}
		return specs;
	}

	// JSON
	@SuppressWarnings("unchecked")
	public static FieldSpec<KeyAnn>[] getJsonKeySpecs(Class<? extends Model> cls) {
		FieldSpec<KeyAnn>[] specs = KEY_SPECS.get(cls);
		if (specs == null) {
			ArrayList<FieldSpec<KeyAnn>> list = new ArrayList<FieldSpec<KeyAnn>>();
			for (Class<?> cl : buildClassHierarchy(cls)) {
				for (Field field : cl.getDeclaredFields()) {
					for (Ann<?> ann : getFieldAnns(field)) {
						if (ann instanceof KeyAnn) {
							KeyAnn keyAnn = (KeyAnn) ann;
							Class<?> componentType = getComponentType(field);
							keyAnn.name = getKeyName(keyAnn, field);
							list.add(new FieldSpec<KeyAnn>(field,
									componentType, keyAnn));
							break;
						}
					}
				}
			}
			specs = list.toArray(new FieldSpec[list.size()]);
			KEY_SPECS.put(cls, specs);
		}
		return specs;
	}

	// caches

	private static final ConcurrentHashMap<Class<?>, FieldSpec<InjectAnn<?>>[]> INJECT_SPECS = new ConcurrentHashMap<Class<?>, FieldSpec<InjectAnn<?>>[]>();
	private static final ConcurrentHashMap<Class<?>, MethodSpec<ReceiveEventsAnn>[]> RECEIVE_EVENTS_SPECS = new ConcurrentHashMap<Class<?>, MethodSpec<ReceiveEventsAnn>[]>();

	private static final ConcurrentHashMap<Class<? extends Entity>, String> TABLE_NAMES = new ConcurrentHashMap<Class<? extends Entity>, String>();
	private static final ConcurrentHashMap<Class<? extends Entity>, FieldSpec<ColumnAnn>[]> COLUMN_SPECS = new ConcurrentHashMap<Class<? extends Entity>, FieldSpec<ColumnAnn>[]>();

	private static final ConcurrentHashMap<Class<? extends Model>, FieldSpec<KeyAnn>[]> KEY_SPECS = new ConcurrentHashMap<Class<? extends Model>, FieldSpec<KeyAnn>[]>();

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
			if (isEntity(field.getType()) && !name.endsWith(ID_AFFIX)) {
				name += ID_AFFIX;
			}
		}
		return name;
	}

	private static final String ID_AFFIX = "_id";

	private static void sanitizeSpecs(
			ArrayList<FieldSpec<ColumnAnn>> columnSpecs) {
		for (FieldSpec<ColumnAnn> spec : columnSpecs) {
			Class<?> fieldType = spec.field.getType();
			if (spec.ann.nullable) {
				if (isByte(fieldType, false) || isShort(fieldType, false)
						|| isInteger(fieldType, false)
						|| isLong(fieldType, false)
						|| isFloat(fieldType, false)
						|| isDouble(fieldType, false)
						|| isBoolean(fieldType, false)
						|| isCharacter(fieldType, false)) {
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
