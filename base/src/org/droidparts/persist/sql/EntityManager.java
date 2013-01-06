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
import static org.droidparts.reflect.util.ReflectionUtils.instantiateEnum;
import static org.droidparts.reflect.util.ReflectionUtils.setFieldVal;
import static org.droidparts.reflect.util.TypeHelper.isArray;
import static org.droidparts.reflect.util.TypeHelper.isBitmap;
import static org.droidparts.reflect.util.TypeHelper.isBoolean;
import static org.droidparts.reflect.util.TypeHelper.isByte;
import static org.droidparts.reflect.util.TypeHelper.isByteArray;
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
import static org.droidparts.util.PersistUtils.isConvertibleToStringArrayOrCollection;
import static org.droidparts.util.io.IOUtils.fromBlob;
import static org.droidparts.util.io.IOUtils.toBlob;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

import org.droidparts.annotation.inject.InjectDependency;
import org.droidparts.inject.Injector;
import org.droidparts.model.Entity;
import org.droidparts.reflect.FieldSpecBuilder;
import org.droidparts.reflect.ann.FieldSpec;
import org.droidparts.reflect.ann.sql.ColumnAnn;
import org.droidparts.reflect.util.ReflectionUtils;
import org.droidparts.util.L;
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

	protected final Context ctx;
	private final Class<EntityType> cls;

	public EntityManager(Context ctx, Class<EntityType> cls) {
		this(ctx, cls, null);
		Injector.get().inject(ctx, this);
	}

	private EntityManager(Context ctx, Class<EntityType> cls, SQLiteDatabase db) {
		this.ctx = ctx.getApplicationContext();
		this.cls = cls;
		this.db = db;
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
		for (FieldSpec<ColumnAnn> spec : getTableColumnSpecs(cls)) {
			int colIdx = cursor.getColumnIndex(spec.ann.name);
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
			putToContentValues(cv, spec.ann.name, spec.field,
					spec.multiFieldArgType, columnVal);
		}
		return cv;
	}

	@Override
	protected void createOrUpdateForeignKeys(EntityType item) {
		for (FieldSpec<ColumnAnn> spec : getTableColumnSpecs(cls)) {
			if (isEntity(spec.field.getType())) {
				Entity foreignEntity = ReflectionUtils.getFieldVal(item,
						spec.field);
				if (foreignEntity != null) {
					subManager(spec.field).createOrUpdate(foreignEntity);
				}
			}
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
			Field field, Class<?> multiFieldArgType, Object value) {
		Class<?> valueType = field.getType();
		if (value == null) {
			cv.putNull(key);
		} else if (isBoolean(valueType)) {
			cv.put(key, ((Boolean) value));
		} else if (isByte(valueType)) {
			cv.put(key, (Byte) value);
		} else if (isByteArray(valueType)) {
			cv.put(key, (byte[]) value);
		} else if (isDouble(valueType)) {
			cv.put(key, (Double) value);
		} else if (isFloat(valueType)) {
			cv.put(key, (Float) value);
		} else if (isInteger(valueType)) {
			cv.put(key, (Integer) value);
		} else if (isLong(valueType)) {
			cv.put(key, (Long) value);
		} else if (isShort(valueType)) {
			cv.put(key, (Short) value);
		} else if (isString(valueType)) {
			cv.put(key, (String) value);
		} else if (isUUID(valueType)) {
			cv.put(key, value.toString());
		} else if (isDate(valueType)) {
			cv.put(key, ((Date) value).getTime());
		} else if (isBitmap(valueType)) {
			Bitmap bm = (Bitmap) value;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bm.compress(CompressFormat.PNG, 0, baos);
			cv.put(key, baos.toByteArray());
		} else if (isEnum(valueType)) {
			cv.put(key, value.toString());
		} else if (isEntity(valueType)) {
			Long id = value != null ? ((Entity) value).id : null;
			cv.put(key, id);
		} else if (isConvertibleToStringArrayOrCollection(valueType,
				multiFieldArgType)) {
			Object[] arr;
			if (isArray(valueType)) {
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
			L.w("Saving " + valueType.getName() + " as BLOB is inefficient.");
			try {
				cv.put(key, toBlob(value));
			} catch (Exception e) {
				L.e(e);
			}
		}
	}

	protected Object readFromCursor(Cursor cursor, int columnIndex,
			Field field, Class<?> multiFieldArgType) {
		Class<?> valueType = field.getType();
		if (cursor.isNull(columnIndex)) {
			return null;
		} else if (isBoolean(valueType)) {
			return cursor.getInt(columnIndex) == 1;
		} else if (isByte(valueType)) {
			return Byte.valueOf(cursor.getString(columnIndex));
		} else if (isByteArray(valueType)) {
			return cursor.getBlob(columnIndex);
		} else if (isDouble(valueType)) {
			return cursor.getDouble(columnIndex);
		} else if (isFloat(valueType)) {
			return cursor.getFloat(columnIndex);
		} else if (isInteger(valueType)) {
			return cursor.getInt(columnIndex);
		} else if (isLong(valueType)) {
			return cursor.getLong(columnIndex);
		} else if (isShort(valueType)) {
			return cursor.getShort(columnIndex);
		} else if (isString(valueType)) {
			return cursor.getString(columnIndex);
		} else if (isUUID(valueType)) {
			return UUID.fromString(cursor.getString(columnIndex));
		} else if (isDate(valueType)) {
			return new Date(cursor.getLong(columnIndex));
		} else if (isBitmap(valueType)) {
			byte[] arr = cursor.getBlob(columnIndex);
			return BitmapFactory.decodeByteArray(arr, 0, arr.length);
		} else if (isEnum(valueType)) {
			return instantiateEnum(valueType, cursor.getString(columnIndex));
		} else if (isEntity(valueType)) {
			long id = cursor.getLong(columnIndex);
			@SuppressWarnings("unchecked")
			Entity entity = instantiate((Class<Entity>) valueType);
			entity.id = id;
			return entity;
		} else if (isConvertibleToStringArrayOrCollection(valueType,
				multiFieldArgType)) {
			String str = cursor.getString(columnIndex);
			String[] parts = (str.length() > 0) ? str.split("\\" + SEP)
					: new String[0];
			if (isArray(valueType)) {
				return toTypeArr(multiFieldArgType, parts);
			} else {
				@SuppressWarnings("unchecked")
				Collection<Object> coll = (Collection<Object>) instantiate(valueType);
				coll.addAll(toTypeColl(multiFieldArgType, parts));
				return coll;
			}
		} else {
			try {
				return fromBlob(cursor.getBlob(columnIndex));
			} catch (Exception e) {
				L.e(e);
				return null;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private EntityManager<Entity> subManager(Field field) {
		return new EntityManager<Entity>(ctx, (Class<Entity>) field.getType(),
				db);
	}
}