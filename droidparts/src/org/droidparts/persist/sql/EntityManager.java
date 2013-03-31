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
import static org.droidparts.reflect.FieldSpecBuilder.getTableColumnSpecs;
import static org.droidparts.reflect.util.ReflectionUtils.getFieldVal;
import static org.droidparts.reflect.util.ReflectionUtils.instantiate;
import static org.droidparts.reflect.util.ReflectionUtils.setFieldVal;
import static org.droidparts.reflect.util.TypeHelper.isArray;
import static org.droidparts.reflect.util.TypeHelper.isBitmap;
import static org.droidparts.reflect.util.TypeHelper.isCollection;
import static org.droidparts.reflect.util.TypeHelper.isDate;
import static org.droidparts.reflect.util.TypeHelper.isEntity;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import org.droidparts.annotation.inject.InjectDependency;
import org.droidparts.inject.Injector;
import org.droidparts.model.Entity;
import org.droidparts.reflect.FieldSpecBuilder;
import org.droidparts.reflect.ann.FieldSpec;
import org.droidparts.reflect.ann.sql.ColumnAnn;
import org.droidparts.reflect.type.TypeHandler;
import org.droidparts.reflect.util.ReflectionUtils;
import org.droidparts.reflect.util.TypeHandlerRegistry;
import org.droidparts.util.Arrays2;
import org.droidparts.util.Strings;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

public class EntityManager<EntityType extends Entity> extends
		AbstractEntityManager<EntityType> {

	// ASCII RS (record separator), '|' for readability
	private static final String SEP = "|" + (char) 30;

	@InjectDependency
	private SQLiteDatabase db;

	private final Context ctx;
	private final Class<EntityType> cls;

	public EntityManager(Context ctx, Class<EntityType> cls) {
		this(ctx, cls, null);
		Injector.get().inject(ctx, this);
	}

	protected EntityManager(Context ctx, Class<EntityType> cls,
			SQLiteDatabase db) {
		this.ctx = ctx.getApplicationContext();
		this.cls = cls;
		this.db = db;
	}

	public Context getContext() {
		return ctx;
	}

	@Override
	public EntityType readRow(Cursor cursor) {
		EntityType entity = instantiate(cls);
		for (FieldSpec<ColumnAnn> spec : getTableColumnSpecs(cls)) {
			int colIdx = cursor.getColumnIndex(spec.ann.name);
			if (colIdx >= 0) {
				Object columnVal = readFromCursor(cursor, colIdx,
						spec.field.getType(), spec.arrCollItemType);
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
			if (isEntity(spec.field.getType())
					&& (fillAll || columnNameSet.contains(spec.ann.name))) {
				Entity foreignEntity = ReflectionUtils.getFieldVal(item,
						spec.field);
				if (foreignEntity != null) {
					Object obj = subManager(spec.field).read(foreignEntity.id);
					setFieldVal(item, spec.field, obj);
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
		return FieldSpecBuilder.getTableName(cls);
	}

	@Override
	protected ContentValues toContentValues(EntityType item) {
		ContentValues cv = new ContentValues();
		for (FieldSpec<ColumnAnn> spec : getTableColumnSpecs(cls)) {
			Object columnVal = getFieldVal(item, spec.field);
			putToContentValues(cv, spec.ann.name, spec.field.getType(),
					spec.arrCollItemType, columnVal);
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

	protected void putToContentValues(ContentValues cv, String key,
			Class<?> valueType, Class<?> arrCollItemType, Object value)
			throws IllegalArgumentException {
		if (value == null) {
			cv.putNull(key);
			return;
		}
		TypeHandler<?> handler = TypeHandlerRegistry.get(valueType);
		if (handler != null) {
			handler.putToContentValues(cv, key, value);
			return;
		}
		// TODO
		if (isBitmap(valueType)) {
			Bitmap bm = (Bitmap) value;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bm.compress(CompressFormat.PNG, 0, baos);
			cv.put(key, baos.toByteArray());
		} else if (isEntity(valueType)) {
			Long id = (value != null) ? ((Entity) value).id : null;
			cv.put(key, id);
		} else if (isArray(valueType) || isCollection(valueType)) {
			final ArrayList<Object> list = new ArrayList<Object>();
			if (isArray(valueType)) {
				list.addAll(Arrays.asList(Arrays2.toObjectArr(value)));
			} else {
				list.addAll((Collection<?>) value);
			}
			if (isDate(arrCollItemType)) {
				for (int i = 0; i < list.size(); i++) {
					Long timestamp = ((Date) list.get(i)).getTime();
					list.set(i, timestamp);
				}
			}
			String val = Strings.join(list, SEP, null);
			cv.put(key, val);
		} else {
			throw new IllegalArgumentException("Need to manually put "
					+ valueType.getName() + " to cursor.");
		}
	}

	protected <T, V> T readFromCursor(Cursor cursor, int columnIndex,
			Class<T> valType, Class<V> arrCollItemType)
			throws IllegalArgumentException {
		if (cursor.isNull(columnIndex)) {
			return null;
		}
		TypeHandler<T> handler = TypeHandlerRegistry.get(valType);
		if (handler != null) {
			return handler.readFromCursor(valType, cursor, columnIndex);
		}
		// TODO
		if (isBitmap(valType)) {
			byte[] arr = cursor.getBlob(columnIndex);
			return (T) BitmapFactory.decodeByteArray(arr, 0, arr.length);
		} else if (isEntity(valType)) {
			long id = cursor.getLong(columnIndex);
			@SuppressWarnings("unchecked")
			Entity entity = instantiate((Class<Entity>) valType);
			entity.id = id;
			return (T) entity;
		} else if (isArray(valType) || isCollection(valType)) {
			TypeHandler<V> handler2 = TypeHandlerRegistry.get(arrCollItemType);
			if (handler2 == null) {
				throw new IllegalArgumentException("Unable to convert to "
						+ arrCollItemType + ".");
			}
			String str = cursor.getString(columnIndex);
			String[] parts = (str.length() > 0) ? str.split("\\" + SEP)
					: new String[0];
			if (isArray(valType)) {
				return (T) handler2.parseTypeArr(arrCollItemType, parts);
			} else {
				@SuppressWarnings("unchecked")
				Collection<Object> coll = (Collection<Object>) instantiate(valType);
				coll.addAll(TypeHandler.toTypeColl(arrCollItemType, parts));
				return (T) coll;
			}
		} else {
			throw new IllegalArgumentException("Need to manually read "
					+ valType.getName() + " from cursor.");
		}
	}

	@SuppressWarnings("unchecked")
	private EntityManager<Entity> subManager(Field field) {
		return new EntityManager<Entity>(ctx, (Class<Entity>) field.getType(),
				db);
	}
}