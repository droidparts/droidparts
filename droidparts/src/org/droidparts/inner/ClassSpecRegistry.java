/**
 * Copyright 2017 Alex Yanchenko
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.droidparts.inner.ann.FieldSpec;
import org.droidparts.inner.ann.MethodSpec;
import org.droidparts.inner.ann.bus.ReceiveEventsAnn;
import org.droidparts.inner.ann.inject.InjectAnn;
import org.droidparts.inner.ann.serialize.JSONAnn;
import org.droidparts.inner.ann.serialize.SaveInstanceStateAnn;
import org.droidparts.inner.ann.serialize.XMLAnn;
import org.droidparts.inner.ann.sql.ColumnAnn;
import org.droidparts.inner.ann.sql.TableAnn;
import org.droidparts.model.Entity;
import org.droidparts.model.Model;
import org.droidparts.util.L;

import static org.droidparts.inner.AnnBuilder.getColumnAnn;
import static org.droidparts.inner.AnnBuilder.getInjectAnn;
import static org.droidparts.inner.AnnBuilder.getJSONAnn;
import static org.droidparts.inner.AnnBuilder.getReceiveEventsAnn;
import static org.droidparts.inner.AnnBuilder.getSaveInstanceStateAnn;
import static org.droidparts.inner.AnnBuilder.getTableAnn;
import static org.droidparts.inner.AnnBuilder.getXMLAnn;
import static org.droidparts.inner.ReflectionUtils.buildClassHierarchy;
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

public final class ClassSpecRegistry {

	// Inject

	@SuppressWarnings("unchecked")
	public static FieldSpec<InjectAnn<?>>[] getInjectSpecs(Class<?> cls) {
		FieldSpec<InjectAnn<?>>[] specs = INJECT_SPECS.get(cls);
		if (specs == null) {
			ArrayList<FieldSpec<InjectAnn<?>>> list = new ArrayList<FieldSpec<InjectAnn<?>>>();
			for (Field field : getFieldHierarchy(cls)) {
				InjectAnn<?> ann = getInjectAnn(field);
				if (ann != null) {
					list.add(new FieldSpec<InjectAnn<?>>(field, ann));
				}
			}
			specs = list.toArray(new FieldSpec[list.size()]);
			INJECT_SPECS.put(cls, specs);
		}
		return specs;
	}

	// SavevInstanceState

	@SuppressWarnings("unchecked")
	public static FieldSpec<SaveInstanceStateAnn>[] getSaveInstanceSpecs(Class<?> cls) {
		FieldSpec<SaveInstanceStateAnn>[] specs = SAVE_INSTANCE_SPECS.get(cls);
		if (specs == null) {
			ArrayList<FieldSpec<SaveInstanceStateAnn>> list = new ArrayList<FieldSpec<SaveInstanceStateAnn>>();
			for (Field field : getFieldHierarchy(cls)) {
				SaveInstanceStateAnn ann = getSaveInstanceStateAnn(field);
				if (ann != null) {
					list.add(new FieldSpec<SaveInstanceStateAnn>(field, ann));
				}
			}
			specs = list.toArray(new FieldSpec[list.size()]);
			SAVE_INSTANCE_SPECS.put(cls, specs);
		}
		return specs;
	}

	// Bus

	@SuppressWarnings("unchecked")
	public static MethodSpec<ReceiveEventsAnn>[] getReceiveEventsSpecs(Class<?> cls) {
		MethodSpec<ReceiveEventsAnn>[] specs = RECEIVE_EVENTS_SPECS.get(cls);
		if (specs == null) {
			ArrayList<MethodSpec<ReceiveEventsAnn>> list = new ArrayList<MethodSpec<ReceiveEventsAnn>>();
			for (Method method : getMethodHierarchy(cls)) {
				ReceiveEventsAnn ann = getReceiveEventsAnn(method);
				if (ann != null) {
					list.add(new MethodSpec<ReceiveEventsAnn>(method, ann));
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
			TableAnn ann = getTableAnn(cls);
			if (ann != null) {
				name = ann.name;
			}
			if (isEmpty(name)) {
				name = cls.getSimpleName();
			}
			TABLE_NAMES.put(cls, name);
		}
		return name;
	}

	@SuppressWarnings("unchecked")
	public static FieldSpec<ColumnAnn>[] getTableColumnSpecs(Class<? extends Entity> cls) {
		FieldSpec<ColumnAnn>[] specs = COLUMN_SPECS.get(cls);
		if (specs == null) {
			ArrayList<FieldSpec<ColumnAnn>> list = new ArrayList<FieldSpec<ColumnAnn>>();
			for (Field field : getFieldHierarchy(cls)) {
				ColumnAnn ann = getColumnAnn(field);
				if (ann != null) {
					ann.name = getColumnName(ann, field);
					list.add(new FieldSpec<ColumnAnn>(field, ann));
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
	public static FieldSpec<JSONAnn>[] getJSONSpecs(Class<? extends Model> cls) {
		FieldSpec<JSONAnn>[] specs = JSON_SPECS.get(cls);
		if (specs == null) {
			ArrayList<FieldSpec<JSONAnn>> list = new ArrayList<FieldSpec<JSONAnn>>();
			for (Field field : getFieldHierarchy(cls)) {
				JSONAnn ann = getJSONAnn(field);
				if (ann != null) {
					ann.key = getName(ann.key, field);
					list.add(new FieldSpec<JSONAnn>(field, ann));
				}
			}
			specs = list.toArray(new FieldSpec[list.size()]);
			JSON_SPECS.put(cls, specs);
		}
		return specs;
	}

	// XML
	@SuppressWarnings("unchecked")
	public static FieldSpec<XMLAnn>[] getXMLSpecs(Class<? extends Model> cls) {
		FieldSpec<XMLAnn>[] specs = XML_SPECS.get(cls);
		if (specs == null) {
			ArrayList<FieldSpec<XMLAnn>> list = new ArrayList<FieldSpec<XMLAnn>>();
			for (Field field : getFieldHierarchy(cls)) {
				XMLAnn ann = getXMLAnn(field);
				if (ann != null) {
					ann.tag = getName(ann.tag, field);
					list.add(new FieldSpec<XMLAnn>(field, ann));
				}
			}
			specs = list.toArray(new FieldSpec[list.size()]);
			XML_SPECS.put(cls, specs);
		}
		return specs;
	}

	// caches

	private static final Map<Class<?>, FieldSpec<InjectAnn<?>>[]> INJECT_SPECS = new ConcurrentHashMap<Class<?>, FieldSpec<InjectAnn<?>>[]>();
	private static final Map<Class<?>, FieldSpec<SaveInstanceStateAnn>[]> SAVE_INSTANCE_SPECS = new ConcurrentHashMap<Class<?>, FieldSpec<SaveInstanceStateAnn>[]>();
	private static final Map<Class<?>, MethodSpec<ReceiveEventsAnn>[]> RECEIVE_EVENTS_SPECS = new ConcurrentHashMap<Class<?>, MethodSpec<ReceiveEventsAnn>[]>();

	private static final Map<Class<? extends Entity>, String> TABLE_NAMES = new ConcurrentHashMap<Class<? extends Entity>, String>();
	private static final Map<Class<? extends Entity>, FieldSpec<ColumnAnn>[]> COLUMN_SPECS = new ConcurrentHashMap<Class<? extends Entity>, FieldSpec<ColumnAnn>[]>();

	private static final Map<Class<? extends Model>, FieldSpec<JSONAnn>[]> JSON_SPECS = new ConcurrentHashMap<Class<? extends Model>, FieldSpec<JSONAnn>[]>();
	private static final Map<Class<? extends Model>, FieldSpec<XMLAnn>[]> XML_SPECS = new ConcurrentHashMap<Class<? extends Model>, FieldSpec<XMLAnn>[]>();

	// Utils

	private static ArrayList<Field> getFieldHierarchy(Class<?> cls) {
		ArrayList<Field> list = new ArrayList<Field>();
		for (Class<?> cl : buildClassHierarchy(cls)) {
			list.addAll(Arrays.asList(cl.getDeclaredFields()));
		}
		return list;
	}

	private static ArrayList<Method> getMethodHierarchy(Class<?> cls) {
		ArrayList<Method> list = new ArrayList<Method>();
		for (Class<?> cl : buildClassHierarchy(cls)) {
			list.addAll(Arrays.asList(cl.getDeclaredMethods()));
		}
		return list;
	}

	// JSON & XML

	private static String getName(String name, Field field) {
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

	private static void sanitizeSpecs(ArrayList<FieldSpec<ColumnAnn>> columnSpecs) {
		for (FieldSpec<ColumnAnn> spec : columnSpecs) {
			Class<?> fieldType = spec.field.getType();
			if (spec.ann.nullable) {
				if (isBoolean(fieldType, false) || isInteger(fieldType, false) || isLong(fieldType, false)
						|| isFloat(fieldType, false) || isDouble(fieldType, false) || isByte(fieldType, false)
						|| isShort(fieldType, false) || isCharacter(fieldType, false)) {
					L.w("%s can't be null.", fieldType.getSimpleName());
					spec.ann.nullable = false;
				}
			} else if (spec.ann.eager) {
				boolean entity = isEntity(fieldType)
						|| ((isArray(fieldType) || isCollection(fieldType)) && isEntity(spec.genericArg1));
				if (!entity) {
					L.w("%s can't be eager.", fieldType.getSimpleName());
					spec.ann.eager = false;
				}
			}
		}
	}

}
