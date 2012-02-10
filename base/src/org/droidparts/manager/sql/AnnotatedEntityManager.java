/**
 * Copyright 2011 Alex Yanchenko
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
package org.droidparts.manager.sql;

import static org.droidparts.reflection.util.ReflectionUtils.getField;
import static org.droidparts.reflection.util.ReflectionUtils.getTypedFieldVal;
import static org.droidparts.reflection.util.ReflectionUtils.instantiate;
import static org.droidparts.reflection.util.ReflectionUtils.instantiateEnum;
import static org.droidparts.reflection.util.ReflectionUtils.listAnnotatedFields;
import static org.droidparts.reflection.util.ReflectionUtils.setFieldVal;
import static org.droidparts.reflection.util.TypeHelper.isBitmap;
import static org.droidparts.reflection.util.TypeHelper.isBoolean;
import static org.droidparts.reflection.util.TypeHelper.isByte;
import static org.droidparts.reflection.util.TypeHelper.isByteArray;
import static org.droidparts.reflection.util.TypeHelper.isEntity;
import static org.droidparts.reflection.util.TypeHelper.isDouble;
import static org.droidparts.reflection.util.TypeHelper.isEnum;
import static org.droidparts.reflection.util.TypeHelper.isFloat;
import static org.droidparts.reflection.util.TypeHelper.isInteger;
import static org.droidparts.reflection.util.TypeHelper.isLong;
import static org.droidparts.reflection.util.TypeHelper.isShort;
import static org.droidparts.reflection.util.TypeHelper.isString;
import static org.droidparts.reflection.util.TypeHelper.isUUID;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.UUID;

import org.droidparts.annotation.inject.Inject;
import org.droidparts.inject.Injector;
import org.droidparts.model.Entity;
import org.droidparts.reflection.model.EntityField;
import org.droidparts.reflection.processor.EntityAnnotationProcessor;
import org.droidparts.reflection.util.ReflectionUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

public class AnnotatedEntityManager<Model extends Entity> extends
		EntityManager<Model> {

	@Inject
	private SQLiteDatabase db;

	protected final Context ctx;
	private final Class<? extends Entity> cls;
	private final EntityAnnotationProcessor processor;

	public AnnotatedEntityManager(Context ctx, Class<? extends Entity> cls) {
		Injector.get().inject(ctx, this);
		this.ctx = ctx;
		this.cls = cls;
		processor = new EntityAnnotationProcessor(cls);
	}

	public boolean delete(long id) {
		// TODO delete models referencing this one via foreign keys.
		return super.delete(id);
	}

	@Override
	public Model readFromCursor(Cursor cursor) {
		Model model = instantiate(cls);
		EntityField[] fields = processor.getModelClassFields();
		for (EntityField dbField : fields) {
			int colIdx = cursor.getColumnIndex(dbField.columnName);
			if (colIdx >= 0) {
				Object columnVal = readFromCursor(cursor, colIdx,
						dbField.fieldClass);
				if (columnVal != null) {
					Field f = getField(model.getClass(), dbField.fieldName);
					setFieldVal(f, model, columnVal);
				}
			}
		}
		return model;
	}

	@Override
	public void fillForeignKeys(Model item) {
		for (Field field : listAnnotatedFields(cls)) {
			Class<?> fieldType = field.getType();
			if (isEntity(fieldType)) {
				Entity model = ReflectionUtils.getTypedFieldVal(field, item);
				if (model != null) {
					Object obj = getManager(fieldType).read(model.id);
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
	protected ContentValues toContentValues(Model item) {
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
	protected void createOrUpdateForeignKeys(Model item) {
		for (Field f : listAnnotatedFields(cls)) {
			Class<?> cls = f.getType();
			if (isEntity(cls)) {
				Entity model = ReflectionUtils.getTypedFieldVal(f, item);
				if (model != null) {
					getManager(cls).createOrUpdate(model);
				}
			}
		}
	}

	protected void subPutToContentValues(ContentValues cv, String key,
			Class<?> valueCls, Object value) {
		throw new IllegalArgumentException("Unsupported class: " + valueCls);
	}

	protected Object subReadFromCursor(Cursor cursor, int columnIndex,
			Class<?> fieldCls) {
		return null;
	}

	private void putToContentValues(ContentValues cv, String key,
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
		} else {
			subPutToContentValues(cv, key, valueCls, value);
		}
	}

	private Object readFromCursor(Cursor cursor, int columnIndex,
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
			Model model = instantiate(fieldCls);
			model.id = id;
			return model;
		} else {
			return subReadFromCursor(cursor, columnIndex, fieldCls);
		}
	}

	private AnnotatedEntityManager<Entity> getManager(Class<?> cls) {
		@SuppressWarnings("unchecked")
		AnnotatedEntityManager<Entity> manager = new AnnotatedEntityManager<Entity>(
				ctx, (Class<? extends Entity>) cls);
		return manager;
	}

}