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
package org.droidparts.util;

import static java.util.Arrays.asList;
import static org.droidparts.reflect.util.TypeHelper.isArray;
import static org.droidparts.reflect.util.TypeHelper.isBoolean;
import static org.droidparts.reflect.util.TypeHelper.isByte;
import static org.droidparts.reflect.util.TypeHelper.isCharacter;
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
import static org.droidparts.util.Strings.join;
import static org.json.JSONObject.NULL;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Callable;

import org.droidparts.contract.DB.Column;
import org.droidparts.contract.SQL;
import org.droidparts.model.Entity;
import org.droidparts.persist.sql.AbstractEntityManager;
import org.droidparts.reflect.model.EntityField;
import org.droidparts.reflect.processor.EntityAnnotationProcessor;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

public final class PersistUtils implements SQL.DDL {

	// JSONSerializer

	public static boolean gotNonNull(JSONObject obj, String key)
			throws JSONException {
		return obj.has(key) && !NULL.equals(obj.get(key));
	}

	// EntityManager

	public static long[] readIds(Cursor cursor) {
		long[] arr = new long[cursor.getCount()];
		int count = 0;
		try {
			while (cursor.moveToNext()) {
				arr[count++] = cursor.getLong(0);
			}
		} finally {
			cursor.close();
		}
		return arr;
	}

	public static <EntityType extends Entity> EntityType readFirst(
			AbstractEntityManager<EntityType> entityManager, Cursor cursor) {
		EntityType item = null;
		try {
			if (cursor.moveToFirst()) {
				item = entityManager.readRow(cursor);
			}
		} finally {
			cursor.close();
		}
		return item;
	}

	public static <EntityType extends Entity> ArrayList<EntityType> readAll(
			AbstractEntityManager<EntityType> entityManager, Cursor cursor) {
		ArrayList<EntityType> list = new ArrayList<EntityType>();
		try {
			while (cursor.moveToNext()) {
				list.add(entityManager.readRow(cursor));
			}
		} finally {
			cursor.close();
		}
		return list;
	}

	// StatementBuilder

	public static String[] toWhereArgs(Object... args) {
		String[] arr = new String[args.length];
		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			String argStr;
			if (arg == null) {
				argStr = "NULL";
			} else if (arg instanceof Boolean) {
				argStr = ((Boolean) arg) ? "1" : "0";
			} else {
				argStr = arg.toString();
			}
			arr[i] = argStr;
		}
		return arr;
	}

	public static String buildPlaceholders(int count) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < count; i++) {
			if (i != 0) {
				sb.append(SQL.DDL.SEPARATOR);
			}
			sb.append("?");
		}
		return sb.toString();
	}

	public static int getRowCount(SQLiteDatabase db, boolean distinct,
			String table, String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy, String limit) {
		if (columns != null && columns.length > 0) {
			columns = new String[] { columns[0] };
		}
		String sql = SQLiteQueryBuilder.buildQueryString(distinct, table,
				columns, selection, groupBy, having, orderBy, limit);
		String countSelection = "SELECT count(*) FROM (" + sql + ")";
		return (int) DatabaseUtils.longForQuery(db, countSelection,
				selectionArgs);
	}

	public static <Result> Result executeInTransaction(SQLiteDatabase db,
			Callable<Result> task) {
		db.beginTransaction();
		try {
			Result result = task.call();
			db.setTransactionSuccessful();
			return result;
		} catch (Exception e) {
			L.e(e.getMessage());
			L.d(e);
			return null;
		} finally {
			db.endTransaction();
		}
	}

	// DBOpenHelper

	public static boolean executeStatements(final SQLiteDatabase db,
			final ArrayList<String> statements) {
		Callable<Boolean> task = new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				for (String statement : statements) {
					L.d(statement);
					db.execSQL(statement);
				}
				return Boolean.TRUE;
			}
		};
		Boolean result = executeInTransaction(db, task);
		return (result != null);
	}

	public static boolean dropTables(SQLiteDatabase db,
			String... optionalTableNames) {
		HashSet<String> tableNames = new HashSet<String>();
		if (optionalTableNames.length == 0) {
			Cursor c = db.rawQuery(
					"SELECT name FROM sqlite_master WHERE type='table'", null);
			while (c.moveToNext()) {
				tableNames.add(c.getString(0));
			}
			c.close();
		} else {
			tableNames.addAll(asList(optionalTableNames));
		}
		ArrayList<String> statements = new ArrayList<String>();
		for (String tableName : tableNames) {
			statements.add("DROP TABLE IF EXISTS " + tableName + ";");
		}
		return executeStatements(db, statements);
	}

	public static String getCreateIndex(String table, boolean unique,
			String firstColumn, String... otherColumns) {
		ArrayList<String> columns = new ArrayList<String>();
		columns.add(firstColumn);
		columns.addAll(asList(otherColumns));
		StringBuilder sb = new StringBuilder();
		sb.append(unique ? CREATE_UNIQUE_INDEX : CREATE_INDEX);
		sb.append("idx_" + table + "_" + join(columns, "_", null));
		sb.append(ON + table);
		sb.append(OPENING_BRACE);
		sb.append(join(columns, SEPARATOR, null));
		sb.append(CLOSING_BRACE);
		return sb.toString();
	}

	public static String getSQLCreate(String tableName, EntityField[] fields) {
		StringBuilder sb = new StringBuilder();
		sb.append(CREATE_TABLE + tableName + OPENING_BRACE);
		sb.append(PK);
		StringBuilder fkSb = new StringBuilder();
		for (EntityField dbField : fields) {
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

	private static String getColumnType(Class<?> fieldType,
			Class<?> fieldArrOrCollType) {
		if (isByte(fieldType)) {
			return INTEGER;
		} else if (isShort(fieldType)) {
			return INTEGER;
		} else if (isInteger(fieldType)) {
			return INTEGER;
		} else if (isLong(fieldType)) {
			return INTEGER;
		} else if (isFloat(fieldType)) {
			return REAL;
		} else if (isDouble(fieldType)) {
			return REAL;
		} else if (isBoolean(fieldType)) {
			return INTEGER;
		} else if (isCharacter(fieldType)) {
			return INTEGER;
		} else if (isString(fieldType)) {
			return TEXT;
		} else if (isEnum(fieldType)) {
			return TEXT;
		} else if (isUUID(fieldType)) {
			return TEXT;
		} else if (isDate(fieldType)) {
			return INTEGER;
		} else if (isArray(fieldType) || isCollection(fieldType)) {
			String arrOrCollColumnType = getColumnType(fieldArrOrCollType, null);
			if (!BLOB.equals(arrOrCollColumnType)) {
				return TEXT;
			}
		} else if (isEntity(fieldType)) {
			return INTEGER;
		}
		// persist any other type as blob
		// TODO sure?
		return BLOB;
	}

	private static void appendForeignKeyDef(EntityField dbField,
			StringBuilder sb) {
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
