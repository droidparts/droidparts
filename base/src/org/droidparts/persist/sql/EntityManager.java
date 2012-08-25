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

import static org.droidparts.reflect.processor.EntityAnnotationProcessor.toPKColumnName;
import static org.droidparts.reflect.util.ReflectionUtils.getField;
import static org.droidparts.reflect.util.ReflectionUtils.getTypedFieldVal;
import static org.droidparts.reflect.util.ReflectionUtils.instantiate;
import static org.droidparts.reflect.util.ReflectionUtils.instantiateEnum;
import static org.droidparts.reflect.util.ReflectionUtils.setFieldVal;
import static org.droidparts.reflect.util.TypeHelper.isArray;
import static org.droidparts.reflect.util.TypeHelper.isBitmap;
import static org.droidparts.reflect.util.TypeHelper.isBoolean;
import static org.droidparts.reflect.util.TypeHelper.isByte;
import static org.droidparts.reflect.util.TypeHelper.isByteArray;
import static org.droidparts.reflect.util.TypeHelper.isCollection;
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

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import org.droidparts.annotation.inject.InjectDependency;
import org.droidparts.inject.Injector;
import org.droidparts.model.Entity;
import org.droidparts.reflect.model.EntityField;
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

	@Override
	public boolean delete(long id) {
		// TODO delete Entities referencing this one via foreign keys.
		return super.delete(id);
	}

	@Override
	public EntityType readFromCursor(Cursor cursor) {
		EntityType entity = instantiate(cls);
		EntityField[] fields = processor.getModelClassFields();
		for (EntityField dbField : fields) {
			int colIdx = cursor.getColumnIndex(dbField.columnName);
			if (colIdx >= 0) {
				Object columnVal = readFromCursor(cursor, colIdx,
						dbField.fieldClass);
				if (columnVal != null) {
					Field f = getField(entity.getClass(), dbField.fieldName);
					setFieldVal(f, entity, columnVal);
				}
			}
		}
		return entity;
	}

	@Override
	public void fillForeignKeys(EntityType item, String... columnNames) {
		HashSet<String> columnNameSet = new HashSet<String>(columnNames.length);
		for (String colName : columnNames) {
			columnNameSet.add(toPKColumnName(colName));
		}
		boolean fillAll = (columnNames.length == 0);
		for (EntityField entityField : processor.getModelClassFields()) {
			if (isEntity(entityField.fieldClass)
					&& (fillAll || columnNameSet
							.contains(entityField.columnName))) {
				Field field = ReflectionUtils.getField(cls,
						entityField.fieldName);
				EntityType foreignEntity = ReflectionUtils.getTypedFieldVal(
						field, item);
				if (foreignEntity != null) {
					Object obj = getInstance(ctx,
							dirtyCast(entityField.fieldClass)).read(
							foreignEntity.id);
					setFieldVal(field, item, obj);
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
		EntityField[] fields = processor.getModelClassFields();
		for (EntityField dbField : fields) {
			Field field = getField(item.getClass(), dbField.fieldName);
			Object columnVal = getTypedFieldVal(field, item);
			putToContentValues(cv, dbField.columnName, dbField.fieldClass,
					columnVal);
		}
		return cv;
	}

	@Override
	protected void createOrUpdateForeignKeys(EntityType item) {
		for (EntityField entityField : processor.getModelClassFields()) {
			if (isEntity(entityField.fieldClass)) {
				Field field = ReflectionUtils.getField(cls,
						entityField.fieldName);
				EntityType foreignEntity = ReflectionUtils.getTypedFieldVal(
						field, item);
				if (foreignEntity != null) {
					getInstance(ctx, dirtyCast(entityField.fieldClass))
							.createOrUpdate(foreignEntity);
				}
			}
		}
	}

	public String[] getEagerForeignKeyFieldNames() {
		if (eagerForeignKeyFieldNames == null) {
			HashSet<String> eagerFieldNames = new HashSet<String>();
			for (EntityField ef : processor.getModelClassFields()) {
				if (ef.columnEager) {
					eagerFieldNames.add(ef.fieldName);
				}
			}
			eagerForeignKeyFieldNames = eagerFieldNames
					.toArray(new String[eagerFieldNames.size()]);
		}
		return eagerForeignKeyFieldNames;
	}

	private String[] eagerForeignKeyFieldNames;

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
				arr = toObjectArr(valueCls, value);
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
			Class<?> fieldCls) {
		if (cursor.isNull(columnIndex)) {
			return null;
		} else if (isBoolean(fieldCls)) {
			return cursor.getInt(columnIndex) == 1;
		} else if (isByte(fieldCls)) {
			return Byte.valueOf(cursor.getString(columnIndex));
		} else if (isByteArray(fieldCls)) {
			return cursor.getBlob(columnIndex);
		} else if (isDouble(fieldCls)) {
			return cursor.getDouble(columnIndex);
		} else if (isFloat(fieldCls)) {
			return cursor.getFloat(columnIndex);
		} else if (isInteger(fieldCls)) {
			return cursor.getInt(columnIndex);
		} else if (isLong(fieldCls)) {
			return cursor.getLong(columnIndex);
		} else if (isShort(fieldCls)) {
			return cursor.getShort(columnIndex);
		} else if (isString(fieldCls)) {
			return cursor.getString(columnIndex);
		} else if (isUUID(fieldCls)) {
			return UUID.fromString(cursor.getString(columnIndex));
		} else if (isBitmap(fieldCls)) {
			byte[] arr = cursor.getBlob(columnIndex);
			return BitmapFactory.decodeByteArray(arr, 0, arr.length);
		} else if (isEnum(fieldCls)) {
			return instantiateEnum(fieldCls, cursor.getString(columnIndex));
		} else if (isEntity(fieldCls)) {
			long id = cursor.getLong(columnIndex);
			EntityType entity = instantiate(fieldCls);
			entity.id = id;
			return entity;
		} else if (isArray(fieldCls) || isCollection(fieldCls)) {
			String str = cursor.getString(columnIndex);
			String[] parts = str.split("\\" + SEP);
			if (isArray(fieldCls)) {
				return toTypeArr(fieldCls, parts);
			} else {
				Collection<?> coll = (Collection<?>) instantiate(fieldCls);
				// TODO populate
				return coll;
			}
		} else {
			// TODO ObjectInputStream
			throw new IllegalArgumentException("Need to manually read "
					+ fieldCls + " from Cursor.");
		}
	}

	private Class<Entity> dirtyCast(Class<?> cls) {
		@SuppressWarnings("unchecked")
		Class<Entity> cls2 = (Class<Entity>) cls;
		return cls2;
	}

}