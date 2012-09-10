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

import static org.droidparts.reflect.util.TypeHelper.isArray;
import static org.droidparts.reflect.util.TypeHelper.isBitmap;
import static org.droidparts.reflect.util.TypeHelper.isBoolean;
import static org.droidparts.reflect.util.TypeHelper.isByteArray;
import static org.droidparts.reflect.util.TypeHelper.isCollection;
import static org.droidparts.reflect.util.TypeHelper.isDouble;
import static org.droidparts.reflect.util.TypeHelper.isEntity;
import static org.droidparts.reflect.util.TypeHelper.isEnum;
import static org.droidparts.reflect.util.TypeHelper.isFloat;
import static org.droidparts.reflect.util.TypeHelper.isInteger;
import static org.droidparts.reflect.util.TypeHelper.isLong;
import static org.droidparts.reflect.util.TypeHelper.isString;
import static org.droidparts.reflect.util.TypeHelper.isUUID;
import static org.droidparts.util.Strings.join;

import java.util.ArrayList;

import org.droidparts.contract.DB.Column;
import org.droidparts.contract.SQL;
import org.droidparts.model.Entity;
import org.droidparts.reflect.model.EntityField;
import org.droidparts.reflect.processor.EntityAnnotationProcessor;
import org.droidparts.util.L;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class AbstractDBOpenHelper extends SQLiteOpenHelper implements
		SQL.DDL {

	public AbstractDBOpenHelper(Context ctx, String name, int version) {
		super(ctx.getApplicationContext(), name, null, version);
	}

	@Override
	public final void onCreate(SQLiteDatabase db) {
		ArrayList<String> queries = new ArrayList<String>();
		for (Class<? extends Entity> cls : getModelClasses()) {
			String query = getSQLCreate(new EntityAnnotationProcessor(cls));
			queries.add(query);
		}
		onOpen(db);
		execQueries(db, queries);
		onCreateExtra(db);
	}

	@Override
	public final void onOpen(SQLiteDatabase db) {
		if (!db.isReadOnly()) {
			db.execSQL("PRAGMA foreign_keys = ON;");
		}
		onOpenExtra(db);
	}

	protected void onCreateExtra(SQLiteDatabase db) {
	}

	protected void onOpenExtra(SQLiteDatabase db) {
	}

	public static void execQueries(SQLiteDatabase db, ArrayList<String> queries) {
		db.beginTransaction();
		try {
			for (String query : queries) {
				L.d(query);
				db.execSQL(query);
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	protected static void createIndex(SQLiteDatabase db, String table,
			boolean unique, String... columns) {
		StringBuilder sb = new StringBuilder();
		sb.append(unique ? CREATE_UNIQUE_INDEX : CREATE_INDEX);
		sb.append("idx_" + table + "_" + join(columns, "_", null));
		sb.append(ON + table);
		sb.append(OPENING_BRACE);
		sb.append(join(columns, SEPARATOR, null));
		sb.append(CLOSING_BRACE);
		db.execSQL(sb.toString());
	}

	protected void dropAll(SQLiteDatabase db, boolean tables, boolean indexes) {
		ArrayList<String> queries = new ArrayList<String>();
		for (Class<? extends Entity> cls : getModelClasses()) {
			String tableName = new EntityAnnotationProcessor(cls)
					.getModelClassName();
			if (tables) {
				queries.add("DROP TABLE IF EXISTS " + tableName + ";");
			}
			if (indexes) {
				// TODO
				// SELECT name FROM sqlite_master WHERE type='index'
				// if (index.startsWith("idx_" + tableName)
				// queries.add("DROP INDEX IF EXISTS " + indexName + ";");
			}
		}
		execQueries(db, queries);
	}

	protected abstract Class<? extends Entity>[] getModelClasses();

	private String getSQLCreate(EntityAnnotationProcessor proc) {
		StringBuilder sb = new StringBuilder();
		sb.append(CREATE_TABLE + proc.getModelClassName() + OPENING_BRACE);
		sb.append(PK);
		StringBuilder fkSb = new StringBuilder();
		for (EntityField dbField : proc.getModelClassFields()) {
			if (Column.ID.equals(dbField.columnName)) {
				// already got it
				continue;
			}
			sb.append(SEPARATOR);
			String columnType = getColumnType(dbField.fieldType,
					dbField.fieldArrOrCollType);
			sb.append(dbField.columnName);
			sb.append(" ");
			sb.append(columnType);
			if (!dbField.columnNullable) {
				sb.append(" ");
				sb.append(NOT_NULL);
			}
			if (dbField.columnUnique) {
				sb.append(" ");
				sb.append(UNIQUE);
			}
			if (isEntity(dbField.fieldType)) {
				fkSb.append(SEPARATOR);
				appendForeignKeyDef(dbField, fkSb);
			}
		}
		sb.append(fkSb);
		sb.append(CLOSING_BRACE);
		return sb.toString();
	}

	private String getColumnType(Class<?> fieldType, Class<?> fieldArrOrCollType) {
		if (isBoolean(fieldType) || isInteger(fieldType) || isLong(fieldType)) {
			return INTEGER;
		} else if (isFloat(fieldType) || isDouble(fieldType)) {
			return REAL;
		} else if (isString(fieldType) || isUUID(fieldType)
				|| isEnum(fieldType)) {
			return TEXT;
		} else if (isByteArray(fieldType) || isBitmap(fieldType)) {
			return BLOB;
		} else if (isArray(fieldType) || isCollection(fieldType)) {
			String arrOrCollColumnType = getColumnType(fieldArrOrCollType, null);
			if (BLOB.equals(arrOrCollColumnType)) {
				// TODO sure?
				return BLOB;
			} else {
				return TEXT;
			}
		} else if (isEntity(fieldType)) {
			// TODO foreign key
			return INTEGER;
		} else {
			// persist any other type as blob
			// TODO sure?
			return BLOB;
		}
	}

	private void appendForeignKeyDef(EntityField dbField, StringBuilder sb) {
		@SuppressWarnings("unchecked")
		Class<? extends Entity> entityType = (Class<? extends Entity>) dbField.fieldType;
		String foreignTableName = new EntityAnnotationProcessor(entityType)
				.getModelClassName();
		sb.append("FOREIGN KEY(");
		sb.append(dbField.columnName);
		sb.append(") REFERENCES ");
		sb.append(foreignTableName);
		sb.append("(").append(Column.ID).append(") ON DELETE CASCADE");
	}
}
