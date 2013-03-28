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
import static org.droidparts.reflect.util.TypeHelper.isCollection;
import static org.droidparts.reflect.util.TypeHelper.isDate;
import static org.droidparts.reflect.util.TypeHelper.isDouble;
import static org.droidparts.reflect.util.TypeHelper.isEntity;
import static org.droidparts.reflect.util.TypeHelper.isEnum;
import static org.droidparts.reflect.util.TypeHelper.isFloat;
import static org.droidparts.reflect.util.TypeHelper.isInteger;
import static org.droidparts.reflect.util.TypeHelper.isJsonArray;
import static org.droidparts.reflect.util.TypeHelper.isJsonObject;
import static org.droidparts.reflect.util.TypeHelper.isLong;
import static org.droidparts.reflect.util.TypeHelper.isShort;
import static org.droidparts.reflect.util.TypeHelper.isString;
import static org.droidparts.reflect.util.TypeHelper.isUUID;
import static org.droidparts.reflect.util.TypeHelper.isUri;
import static org.droidparts.reflect.util.TypeHelper.toObjectArr;
import static org.droidparts.reflect.util.TypeHelper.toTypeArr;
import static org.droidparts.reflect.util.TypeHelper.toTypeColl;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.droidparts.util.Strings;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;

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
		} else if (isUri(valueType)) {
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
		} else if (isJsonObject(valueType) || isJsonArray(valueType)) {
			cv.put(key, value.toString());
		} else if (isEntity(valueType)) {
			Long id = value != null ? ((Entity) value).id : null;
			cv.put(key, id);
		} else if (isArray(valueType) || isCollection(valueType)) {
			final ArrayList<Object> list = new ArrayList<Object>();
			if (isArray(valueType)) {
				list.addAll(Arrays.asList(toObjectArr(value)));
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

	protected Object readFromCursor(Cursor cursor, int columnIndex,
			Class<?> valType, Class<?> arrCollItemType)
			throws IllegalArgumentException {
		if (cursor.isNull(columnIndex)) {
			return null;
		} else if (isBoolean(valType)) {
			return cursor.getInt(columnIndex) == 1;
		} else if (isByte(valType)) {
			return Byte.valueOf(cursor.getString(columnIndex));
		} else if (isByteArray(valType)) {
			return cursor.getBlob(columnIndex);
		} else if (isDouble(valType)) {
			return cursor.getDouble(columnIndex);
		} else if (isFloat(valType)) {
			return cursor.getFloat(columnIndex);
		} else if (isInteger(valType)) {
			return cursor.getInt(columnIndex);
		} else if (isLong(valType)) {
			return cursor.getLong(columnIndex);
		} else if (isShort(valType)) {
			return cursor.getShort(columnIndex);
		} else if (isString(valType)) {
			return cursor.getString(columnIndex);
		} else if (isUUID(valType)) {
			return UUID.fromString(cursor.getString(columnIndex));
		} else if (isUri(valType)) {
			return Uri.parse(cursor.getString(columnIndex));
		} else if (isDate(valType)) {
			return new Date(cursor.getLong(columnIndex));
		} else if (isBitmap(valType)) {
			byte[] arr = cursor.getBlob(columnIndex);
			return BitmapFactory.decodeByteArray(arr, 0, arr.length);
		} else if (isJsonObject(valType) || isJsonArray(valType)) {
			String str = cursor.getString(columnIndex);
			try {
				return isJsonObject(valType) ? new JSONObject(str)
						: new JSONArray(str);
			} catch (JSONException e) {
				throw new IllegalArgumentException(e);
			}
		} else if (isEnum(valType)) {
			return instantiateEnum(valType, cursor.getString(columnIndex));
		} else if (isEntity(valType)) {
			long id = cursor.getLong(columnIndex);
			@SuppressWarnings("unchecked")
			Entity entity = instantiate((Class<Entity>) valType);
			entity.id = id;
			return entity;
		} else if (isArray(valType) || isCollection(valType)) {
			String str = cursor.getString(columnIndex);
			String[] parts = (str.length() > 0) ? str.split("\\" + SEP)
					: new String[0];
			if (isArray(valType)) {
				return toTypeArr(arrCollItemType, parts);
			} else {
				@SuppressWarnings("unchecked")
				Collection<Object> coll = (Collection<Object>) instantiate(valType);
				coll.addAll(toTypeColl(arrCollItemType, parts));
				return coll;
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