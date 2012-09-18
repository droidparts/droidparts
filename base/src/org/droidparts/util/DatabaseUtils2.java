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
import static org.droidparts.reflect.util.TypeHelper.isBitmap;
import static org.droidparts.reflect.util.TypeHelper.isBoolean;
import static org.droidparts.reflect.util.TypeHelper.isByteArray;
import static org.droidparts.reflect.util.TypeHelper.isCollection;
import static org.droidparts.reflect.util.TypeHelper.isDate;
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
import java.util.HashSet;

import org.droidparts.contract.DB.Column;
import org.droidparts.contract.SQL;
import org.droidparts.model.Entity;
import org.droidparts.reflect.model.EntityField;
import org.droidparts.reflect.processor.EntityAnnotationProcessor;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

public final class DatabaseUtils2 implements SQL.DDL {

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

	public static void createIndex(SQLiteDatabase db, String table,
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

	public static void dropTables(SQLiteDatabase db,
			String... optionalTableNames) {
		ArrayList<String> queries = new ArrayList<String>();
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
		for (String tableName : tableNames) {
			queries.add("DROP TABLE IF EXISTS " + tableName + ";");
		}
		execQueries(db, queries);
	}

	//

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
		if (isBoolean(fieldType) || isInteger(fieldType) || isLong(fieldType)) {
			return INTEGER;
		} else if (isFloat(fieldType) || isDouble(fieldType)) {
			return REAL;
		} else if (isString(fieldType) || isEnum(fieldType)
				|| isUUID(fieldType)) {
			return TEXT;
		} else if (isByteArray(fieldType) || isBitmap(fieldType)) {
			return BLOB;
		} else if (isDate(fieldType)) {
			return INTEGER;
		} else if (isArray(fieldType) || isCollection(fieldType)) {
			String arrOrCollColumnType = getColumnType(fieldArrOrCollType, null);
			if (BLOB.equals(arrOrCollColumnType)) {
				// TODO sure?
				return BLOB;
			} else {
				return TEXT;
			}
		} else if (isEntity(fieldType)) {
			return INTEGER;
		} else {
			// persist any other type as blob
			// TODO sure?
			return BLOB;
		}
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
