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
package org.droidparts.persist.sql;

import static java.util.Arrays.asList;
import static org.droidparts.reflect.util.ReflectionUtils.getFieldVal;
import static org.droidparts.reflect.util.ReflectionUtils.instantiate;
import static org.droidparts.reflect.util.ReflectionUtils.instantiateEnum;
import static org.droidparts.reflect.util.ReflectionUtils.setFieldVal;
import static org.droidparts.reflect.util.TypeHelper.isArray;
import static org.droidparts.reflect.util.TypeHelper.isBitmap;
import static org.droidparts.reflect.util.TypeHelper.isBoolean;
import static org.droidparts.reflect.util.TypeHelper.isByte;
import static org.droidparts.reflect.util.TypeHelper.isByteArray;
import static org.droidparts.reflect.util.TypeHelper.isCollection;
import static org.droidparts.reflect.util.TypeHelper.isDate;
import static org.droidparts.reflect.util.TypeHelper.isDouble;
import static org.droidparts.reflect.util.TypeHelper.isEntity;
import static org.droidparts.reflect.util.TypeHelper.isEnum;
import static org.droidparts.reflect.util.TypeHelper.isFloat;
import static org.droidparts.reflect.util.TypeHelper.isInteger;
import static org.droidparts.reflect.util.TypeHelper.isLong;
import static org.droidparts.reflect.util.TypeHelper.isShort;
import static org.droidparts.reflect.util.TypeHelper.isString;
import static org.droidparts.reflect.util.TypeHelper.isUUID;
import static org.droidparts.reflect.util.TypeHelper.toObjectArr;
import static org.droidparts.reflect.util.TypeHelper.toTypeArr;
import static org.droidparts.reflect.util.TypeHelper.toTypeColl;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

import org.droidparts.annotation.inject.InjectDependency;
import org.droidparts.inject.Injector;
import org.droidparts.model.Entity;
import org.droidparts.reflect.model.sql.EntitySpec;
import org.droidparts.reflect.processor.EntityAnnotationProcessor;
import org.droidparts.reflect.util.ReflectionUtils;
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

	public static <EntityType extends Entity> EntityManager<EntityType> getInstance(
			Context ctx, Class<EntityType> cls) {
		return new EntityManager<EntityType>(ctx, (Class<EntityType>) cls);
	}

	// ASCII RS (record separator), '|' for readability
	private static final String SEP = "|" + (char) 30;

	@InjectDependency
	private SQLiteDatabase db;

	protected final Context ctx;
	private final Class<? extends EntityType> cls;
	private final EntityAnnotationProcessor processor;

	public EntityManager(Context ctx, Class<EntityType> cls) {
		Injector.get().inject(ctx, this);
		this.ctx = ctx.getApplicationContext();
		this.cls = cls;
		processor = new EntityAnnotationProcessor(cls);
	}

	public void fillEagerForeignKeys(EntityType item) {
		String[] eagerColumnNames = getEagerForeignKeyColumnNames();
		if (eagerColumnNames.length != 0) {
			fillForeignKeys(item, eagerColumnNames);
		}
	}

	@Override
	public EntityType readRow(Cursor cursor) {
		EntityType entity = instantiate(cls);
		EntitySpec[] specs = processor.getModelClassFields();
		for (EntitySpec spec : specs) {
			int colIdx = cursor.getColumnIndex(spec.column.name);
			if (colIdx >= 0) {
				Object columnVal = readFromCursor(cursor, colIdx, spec.field,
						spec.multiFieldArgType);
				if (columnVal != null) {
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
		for (EntitySpec entitySpec : processor.getModelClassFields()) {
			if (isEntity(entitySpec.field.getType())
					&& (fillAll || columnNameSet
							.contains(entitySpec.column.name))) {
				EntityType foreignEntity = ReflectionUtils.getFieldVal(item,
						entitySpec.field);
				if (foreignEntity != null) {
					Object obj = getInstance(ctx,
							dirtyCast(entitySpec.field.getType())).read(
							foreignEntity.id);
					setFieldVal(item, entitySpec.field, obj);
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
		return processor.getModelClassName();
	}

	@Override
	protected ContentValues toContentValues(EntityType item) {
		ContentValues cv = new ContentValues();
		EntitySpec[] fields = processor.getModelClassFields();
		for (EntitySpec dbField : fields) {
			Object columnVal = getFieldVal(item, dbField.field);
			putToContentValues(cv, dbField.column.name,
					dbField.field.getType(), columnVal);
		}
		return cv;
	}

	@Override
	protected void createOrUpdateForeignKeys(EntityType item) {
		for (EntitySpec entityField : processor.getModelClassFields()) {
			if (isEntity(entityField.field.getType())) {
				EntityType foreignEntity = ReflectionUtils.getFieldVal(item,
						entityField.field);
				if (foreignEntity != null) {
					getInstance(ctx, dirtyCast(entityField.field.getType()))
							.createOrUpdate(foreignEntity);
				}
			}
		}
	}

	protected String[] getEagerForeignKeyColumnNames() {
		if (eagerForeignKeyColumnNames == null) {
			HashSet<String> eagerColumnNames = new HashSet<String>();
			for (EntitySpec ef : processor.getModelClassFields()) {
				if (ef.column.eager) {
					eagerColumnNames.add(ef.column.name);
				}
			}
			eagerForeignKeyColumnNames = eagerColumnNames
					.toArray(new String[eagerColumnNames.size()]);
		}
		return eagerForeignKeyColumnNames;
	}

	private String[] eagerForeignKeyColumnNames;

	protected void putToContentValues(ContentValues cv, String key,
			Class<?> valueCls, Object value) {
		if (value == null) {
			cv.putNull(key);
		} else if (isBoolean(valueCls)) {
			cv.put(key, ((Boolean) value));
		} else if (isByte(valueCls)) {
			cv.put(key, (Byte) value);
		} else if (isByteArray(valueCls)) {
			cv.put(key, (byte[]) value);
		} else if (isDouble(valueCls)) {
			cv.put(key, (Double) value);
		} else if (isFloat(valueCls)) {
			cv.put(key, (Float) value);
		} else if (isInteger(valueCls)) {
			cv.put(key, (Integer) value);
		} else if (isLong(valueCls)) {
			cv.put(key, (Long) value);
		} else if (isShort(valueCls)) {
			cv.put(key, (Short) value);
		} else if (isString(valueCls)) {
			cv.put(key, (String) value);
		} else if (isUUID(valueCls)) {
			cv.put(key, value.toString());
		} else if (isDate(valueCls)) {
			cv.put(key, ((Date) value).getTime());
		} else if (isBitmap(valueCls)) {
			Bitmap bm = (Bitmap) value;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bm.compress(CompressFormat.PNG, 0, baos);
			cv.put(key, baos.toByteArray());
		} else if (isEnum(valueCls)) {
			cv.put(key, value.toString());
		} else if (isEntity(valueCls)) {
			Long id = value != null ? ((Entity) value).id : null;
			cv.put(key, id);
		} else if (isArray(valueCls) || isCollection(valueCls)) {
			Object[] arr;
			if (isArray(valueCls)) {
				arr = toObjectArr(value);
			} else {
				Collection<?> coll = (Collection<?>) value;
				arr = coll.toArray(new Object[coll.size()]);
			}
			if (arr != null) {
				String val = Strings.join(arr, SEP, null);
				cv.put(key, val);
			}
		} else {
			// TODO ObjectOutputStream
			throw new IllegalArgumentException("Need to manually put "
					+ valueCls + " to ContentValues.");
		}
	}

	protected Object readFromCursor(Cursor cursor, int columnIndex,
			Field field, Class<?> multiFieldArgType) {
		Class<?> fieldType = field.getType();
		if (cursor.isNull(columnIndex)) {
			return null;
		} else if (isBoolean(fieldType)) {
			return cursor.getInt(columnIndex) == 1;
		} else if (isByte(fieldType)) {
			return Byte.valueOf(cursor.getString(columnIndex));
		} else if (isByteArray(fieldType)) {
			return cursor.getBlob(columnIndex);
		} else if (isDouble(fieldType)) {
			return cursor.getDouble(columnIndex);
		} else if (isFloat(fieldType)) {
			return cursor.getFloat(columnIndex);
		} else if (isInteger(fieldType)) {
			return cursor.getInt(columnIndex);
		} else if (isLong(fieldType)) {
			return cursor.getLong(columnIndex);
		} else if (isShort(fieldType)) {
			return cursor.getShort(columnIndex);
		} else if (isString(fieldType)) {
			return cursor.getString(columnIndex);
		} else if (isUUID(fieldType)) {
			return UUID.fromString(cursor.getString(columnIndex));
		} else if (isDate(fieldType)) {
			return new Date(cursor.getLong(columnIndex));
		} else if (isBitmap(fieldType)) {
			byte[] arr = cursor.getBlob(columnIndex);
			return BitmapFactory.decodeByteArray(arr, 0, arr.length);
		} else if (isEnum(fieldType)) {
			return instantiateEnum(fieldType, cursor.getString(columnIndex));
		} else if (isEntity(fieldType)) {
			long id = cursor.getLong(columnIndex);
			EntityType entity = instantiate(fieldType);
			entity.id = id;
			return entity;
		} else if (isArray(fieldType) || isCollection(fieldType)) {
			String str = cursor.getString(columnIndex);
			String[] parts = (str.length() > 0) ? str.split("\\" + SEP)
					: new String[0];
			if (isArray(fieldType)) {
				return toTypeArr(multiFieldArgType, parts);
			} else {
				@SuppressWarnings("unchecked")
				Collection<Object> coll = (Collection<Object>) instantiate(fieldType);
				coll.addAll(toTypeColl(multiFieldArgType, parts));
				return coll;
			}
		} else {
			// TODO ObjectInputStream
			throw new IllegalArgumentException("Need to manually read "
					+ fieldType + " from Cursor.");
		}
	}

	private Class<Entity> dirtyCast(Class<?> cls) {
		@SuppressWarnings("unchecked")
		Class<Entity> cls2 = (Class<Entity>) cls;
		return cls2;
	}

}