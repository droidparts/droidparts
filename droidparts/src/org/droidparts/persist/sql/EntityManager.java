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
package org.droidparts.persist.sql;

import static java.util.Arrays.asList;
import static org.droidparts.inner.ReflectionUtils.getFieldVal;
import static org.droidparts.inner.ReflectionUtils.newInstance;
import static org.droidparts.inner.ReflectionUtils.setFieldVal;
import static org.droidparts.inner.TypeHelper.isArray;
import static org.droidparts.inner.TypeHelper.isCollection;
import static org.droidparts.inner.TypeHelper.isEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.droidparts.Injector;
import org.droidparts.inner.ClassSpecRegistry;
import org.droidparts.inner.ConverterRegistry;
import org.droidparts.inner.ann.FieldSpec;
import org.droidparts.inner.ann.sql.ColumnAnn;
import org.droidparts.inner.converter.Converter;
import org.droidparts.model.Entity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class EntityManager<EntityType extends Entity> extends
		AbstractEntityManager<EntityType> {

	private final Class<EntityType> cls;
	private final Context ctx;
	private final SQLiteDatabase db;

	public EntityManager(Class<EntityType> cls, Context ctx) {
		this(cls, ctx, Injector.getDependency(ctx, SQLiteDatabase.class));
	}

	protected EntityManager(Class<EntityType> cls, Context ctx,
			SQLiteDatabase db) {
		this.cls = cls;
		this.ctx = ctx.getApplicationContext();
		this.db = db;
		Injector.inject(ctx, this);
	}

	protected Context getContext() {
		return ctx;
	}

	@Override
	public EntityType readRow(Cursor cursor) {
		EntityType entity = newInstance(cls);
		FieldSpec<ColumnAnn>[] columnSpecs = ClassSpecRegistry
				.getTableColumnSpecs(cls);
		for (FieldSpec<ColumnAnn> spec : columnSpecs) {
			int colIdx = cursor.getColumnIndex(spec.ann.name);
			if (colIdx >= 0) {
				Object columnVal = readFromCursor(cursor, colIdx,
						spec.field.getType(), spec.componentType);
				if (columnVal != null || spec.ann.nullable) {
					setFieldVal(entity, spec.field, columnVal);
				}
			}
		}
		return entity;
	}

	@Override
	public void fillForeignKeys(EntityType item, String... columnNames) {
		HashSet<String> columnNameSet = new HashSet<String>(asList(columnNames));
		boolean fillAll = columnNameSet.isEmpty();
		FieldSpec<ColumnAnn>[] columnSpecs = ClassSpecRegistry
				.getTableColumnSpecs(cls);
		for (FieldSpec<ColumnAnn> spec : columnSpecs) {
			if (fillAll || columnNameSet.contains(spec.ann.name)) {
				Class<?> fieldType = spec.field.getType();
				if (isEntity(fieldType)) {
					Entity foreignEntity = getFieldVal(item, spec.field);
					if (foreignEntity != null) {
						EntityManager<Entity> manager = subManager(spec.field
								.getType());
						Object obj = manager.read(foreignEntity.id);
						setFieldVal(item, spec.field, obj);
					}
				} else if ((isArray(fieldType) || isCollection(fieldType))
						&& isEntity(spec.componentType)) {
					EntityManager<Entity> manager = subManager(spec.componentType);
					if (isArray(fieldType)) {
						Entity[] arr = getFieldVal(item, spec.field);
						if (arr != null) {
							for (int i = 0; i < arr.length; i++) {
								Entity ent = arr[i];
								if (ent != null) {
									arr[i] = manager.read(ent.id);
								}
							}
						}
					} else {
						Collection<Entity> coll = getFieldVal(item, spec.field);
						if (coll != null) {
							ArrayList<Entity> entities = new ArrayList<Entity>(
									coll.size());
							for (Entity ent : coll) {
								if (ent != null) {
									entities.add(manager.read(ent.id));
								}
							}
							coll.clear();
							coll.addAll(entities);
						}

					}
				}
			}
		}
	}

	@Override
	protected SQLiteDatabase getDB() {
		return db;
	}

	@Override
	protected String getTableName() {
		return ClassSpecRegistry.getTableName(cls);
	}

	@Override
	protected ContentValues toContentValues(EntityType item) {
		ContentValues cv = new ContentValues();
		FieldSpec<ColumnAnn>[] columnSpecs = ClassSpecRegistry
				.getTableColumnSpecs(cls);
		for (FieldSpec<ColumnAnn> spec : columnSpecs) {
			Object columnVal = getFieldVal(item, spec.field);
			putToContentValues(cv, spec.ann.name, spec.field.getType(),
					spec.componentType, columnVal);
		}
		return cv;
	}

	@Override
	protected void createForeignKeys(EntityType item) {
		FieldSpec<ColumnAnn>[] columnSpecs = ClassSpecRegistry
				.getTableColumnSpecs(cls);
		for (FieldSpec<ColumnAnn> spec : columnSpecs) {
			Class<?> fieldType = spec.field.getType();
			if (isEntity(fieldType)) {
				Entity foreignEntity = getFieldVal(item, spec.field);
				if (foreignEntity != null && foreignEntity.id == 0) {
					subManager(spec.field.getType()).create(foreignEntity);
				}
			} else if ((isArray(fieldType) || isCollection(fieldType))
					&& isEntity(spec.componentType)) {
				ArrayList<Entity> toCreate = new ArrayList<Entity>();
				if (isArray(fieldType)) {
					Entity[] arr = getFieldVal(item, spec.field);
					if (arr != null) {
						for (Entity ent : arr) {
							if (ent != null && ent.id == 0) {
								toCreate.add(ent);
							}
						}
					}
				} else {
					Collection<Entity> coll = getFieldVal(item, spec.field);
					if (coll != null) {
						for (Entity ent : coll) {
							if (ent != null && ent.id == 0) {
								toCreate.add(ent);
							}
						}
					}
				}
				if (!toCreate.isEmpty()) {
					subManager(spec.componentType).create(toCreate);
				}

			}
		}
	}

	@Override
	protected void fillEagerForeignKeys(EntityType item) {
		String[] eagerColumnNames = getEagerForeignKeyColumnNames();
		if (eagerColumnNames.length != 0) {
			fillForeignKeys(item, eagerColumnNames);
		}
	}

	protected String[] getEagerForeignKeyColumnNames() {
		if (eagerForeignKeyColumnNames == null) {
			HashSet<String> eagerColumnNames = new HashSet<String>();
			FieldSpec<ColumnAnn>[] columnSpecs = ClassSpecRegistry
					.getTableColumnSpecs(cls);
			for (FieldSpec<ColumnAnn> spec : columnSpecs) {
				if (spec.ann.eager) {
					eagerColumnNames.add(spec.ann.name);
				}
			}
			eagerForeignKeyColumnNames = eagerColumnNames
					.toArray(new String[eagerColumnNames.size()]);
		}
		return eagerForeignKeyColumnNames;
	}

	private String[] eagerForeignKeyColumnNames;

	@SuppressWarnings("unchecked")
	protected <T, V> void putToContentValues(ContentValues cv, String key,
			Class<T> valueType, Class<V> componentType, Object value)
			throws IllegalArgumentException {
		if (value == null) {
			cv.putNull(key);
		} else {
			Converter<T> converter = ConverterRegistry.getConverter(valueType);
			converter.putToContentValues(valueType, componentType, cv, key,
					(T) value);
		}
	}

	protected <T, V> Object readFromCursor(Cursor cursor, int columnIndex,
			Class<T> valType, Class<V> componentType)
			throws IllegalArgumentException {
		if (cursor.isNull(columnIndex)) {
			return null;
		} else {
			Converter<T> converter = ConverterRegistry.getConverter(valType);
			return converter.readFromCursor(valType, componentType, cursor,
					columnIndex);
		}
	}

	@SuppressWarnings("unchecked")
	private EntityManager<Entity> subManager(Class<?> entityType) {
		return new EntityManager<Entity>((Class<Entity>) entityType, ctx, db);
	}
}