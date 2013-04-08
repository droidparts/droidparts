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
package org.droidparts.persist.sql;

import static java.util.Arrays.asList;
import static org.droidparts.inner.FieldSpecRegistry.getTableColumnSpecs;
import static org.droidparts.inner.ReflectionUtils.getFieldVal;
import static org.droidparts.inner.ReflectionUtils.newInstance;
import static org.droidparts.inner.ReflectionUtils.setFieldVal;
import static org.droidparts.inner.TypeHelper.isEntity;

import java.lang.reflect.Field;
import java.util.HashSet;

import org.droidparts.Injector;
import org.droidparts.annotation.inject.InjectDependency;
import org.droidparts.inner.FieldSpecRegistry;
import org.droidparts.inner.ReflectionUtils;
import org.droidparts.inner.TypeHandlerRegistry;
import org.droidparts.inner.ann.FieldSpec;
import org.droidparts.inner.ann.sql.ColumnAnn;
import org.droidparts.inner.handler.TypeHandler;
import org.droidparts.model.Entity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class EntityManager<EntityType extends Entity> extends
		AbstractEntityManager<EntityType> {

	@InjectDependency
	private SQLiteDatabase db;

	private final Class<EntityType> cls;
	private final Context ctx;

	public EntityManager(Class<EntityType> cls, Context ctx) {
		this.cls = cls;
		this.ctx = ctx.getApplicationContext();
		Injector.get().inject(ctx, this);
	}

	protected EntityManager(Class<EntityType> cls, Context ctx,
			SQLiteDatabase db) {
		this.cls = cls;
		this.ctx = ctx;
		this.db = db;
	}

	public Context getContext() {
		return ctx;
	}

	@Override
	public EntityType readRow(Cursor cursor) {
		EntityType entity = newInstance(cls);
		for (FieldSpec<ColumnAnn> spec : getTableColumnSpecs(cls)) {
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
		for (FieldSpec<ColumnAnn> spec : getTableColumnSpecs(cls)) {
			if (fillAll || columnNameSet.contains(spec.ann.name)) {
				if (isEntity(spec.field.getType())) {
					Entity foreignEntity = ReflectionUtils.getFieldVal(item,
							spec.field);
					if (foreignEntity != null) {
						Object obj = subManager(spec.field).read(
								foreignEntity.id);
						setFieldVal(item, spec.field, obj);
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
		return FieldSpecRegistry.getTableName(cls);
	}

	@Override
	protected ContentValues toContentValues(EntityType item) {
		ContentValues cv = new ContentValues();
		for (FieldSpec<ColumnAnn> spec : getTableColumnSpecs(cls)) {
			Object columnVal = getFieldVal(item, spec.field);
			putToContentValues(cv, spec.ann.name, spec.field.getType(),
					spec.componentType, columnVal);
		}
		return cv;
	}

	@Override
	protected void createForeignKeys(EntityType item) {
		for (FieldSpec<ColumnAnn> spec : getTableColumnSpecs(cls)) {
			if (isEntity(spec.field.getType())) {
				Entity foreignEntity = ReflectionUtils.getFieldVal(item,
						spec.field);
				if (foreignEntity != null && foreignEntity.id == 0) {
					subManager(spec.field).create(foreignEntity);
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
			for (FieldSpec<ColumnAnn> spec : getTableColumnSpecs(cls)) {
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
			TypeHandler<T> handler = TypeHandlerRegistry.getHandler(valueType);
			handler.putToContentValues(valueType, componentType, cv, key,
					(T) value);
		}
	}

	protected <T, V> Object readFromCursor(Cursor cursor, int columnIndex,
			Class<T> valType, Class<V> componentType)
			throws IllegalArgumentException {
		if (cursor.isNull(columnIndex)) {
			return null;
		} else {
			TypeHandler<T> handler = TypeHandlerRegistry.getHandler(valType);
			return handler.readFromCursor(valType, componentType, cursor,
					columnIndex);
		}
	}

	@SuppressWarnings("unchecked")
	private EntityManager<Entity> subManager(Field field) {
		return new EntityManager<Entity>((Class<Entity>) field.getType(), ctx,
				db);
	}
}