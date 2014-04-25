/**
 * Copyright 2014 Alex Yanchenko
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
package org.droidparts.inner;

import static java.util.Arrays.asList;
import static org.droidparts.inner.ClassSpecRegistry.getTableName;
import static org.droidparts.inner.TypeHelper.isEntity;
import static org.droidparts.util.Strings.join;
import static org.json.JSONObject.NULL;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.Callable;

import org.droidparts.contract.DB.Column;
import org.droidparts.contract.SQL;
import org.droidparts.inner.ann.FieldSpec;
import org.droidparts.inner.ann.sql.ColumnAnn;
import org.droidparts.inner.converter.Converter;
import org.droidparts.model.Entity;
import org.droidparts.persist.sql.AbstractEntityManager;
import org.droidparts.util.L;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

public final class PersistUtils implements SQL.DDL {

	// JSONSerializer

	public static boolean hasNonNull(JSONObject obj, String key)
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
			} else if (arg instanceof Date) {
				argStr = String.valueOf(((Date) arg).getTime());
			} else if (arg instanceof Entity) {
				argStr = String.valueOf(((Entity) arg).id);
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
				sb.append(SEPARATOR);
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
			L.w(e.getMessage());
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
					L.i(statement);
					db.execSQL(statement);
				}
				return Boolean.TRUE;
			}
		};
		Boolean result = executeInTransaction(db, task);
		return (result != null);
	}

	public static ArrayList<String> getDropTables(SQLiteDatabase db,
			String... optionalTableNames) {
		HashSet<String> tableNames = new HashSet<String>();
		if (optionalTableNames.length == 0) {
			tableNames.addAll(getTableNames(db));
		} else {
			tableNames.addAll(asList(optionalTableNames));
		}
		ArrayList<String> statements = new ArrayList<String>();
		for (String tableName : tableNames) {
			statements.add("DROP TABLE IF EXISTS " + tableName + ";");
		}
		return statements;
	}

	@SuppressWarnings("unchecked")
	public static <T> ArrayList<String> getAddMissingColumns(SQLiteDatabase db,
			Class<? extends Entity> cls) {
		String tableName = getTableName(cls);
		FieldSpec<ColumnAnn>[] columnSpecs = ClassSpecRegistry
				.getTableColumnSpecs(cls);
		ArrayList<String> presentColumns = getColumnNames(db, tableName);

		ArrayList<FieldSpec<ColumnAnn>> columns = new ArrayList<FieldSpec<ColumnAnn>>();
		for (FieldSpec<ColumnAnn> spec : columnSpecs) {
			if (!presentColumns.contains(spec.ann.name)) {
				columns.add(spec);
			}
		}

		ArrayList<String> statements = new ArrayList<String>();
		for (FieldSpec<ColumnAnn> spec : columns) {
			Entity entity = ReflectionUtils.newInstance(cls);
			Converter<T> converter = (Converter<T>) ConverterRegistry
					.getConverter(spec.field.getType());
			Object defaultVal = ReflectionUtils.getFieldVal(entity, spec.field);
			//
			ContentValues cv = new ContentValues();
			converter.putToContentValues((Class<T>) spec.field.getType(),
					spec.componentType, cv, "key", (T) defaultVal);
			defaultVal = cv.get("key");
			//
			String statement = getAddColumn(tableName, spec.ann.name,
					converter.getDBColumnType(), spec.ann.nullable, defaultVal);
			statements.add(statement);
		}
		return statements;
	}

	public static String getAddColumn(String table, String name, String type,
			boolean nullable, Object defaultVal) {
		StringBuilder sb = new StringBuilder();
		sb.append(ALTER_TABLE).append(table);
		sb.append(ADD_COLUMN).append(name).append(type);
		if (!nullable) {
			sb.append(NOT_NULL);
			sb.append(DEFAULT).append(defaultVal);
		}
		sb.append(";");
		return sb.toString();
	}

	public static String getCreateIndex(String table, boolean unique,
			String firstColumn, String... otherColumns) {
		ArrayList<String> columns = new ArrayList<String>();
		columns.add(firstColumn);
		columns.addAll(asList(otherColumns));
		StringBuilder sb = new StringBuilder();
		sb.append(unique ? CREATE_UNIQUE_INDEX : CREATE_INDEX);
		sb.append("idx_" + table + "_" + join(columns, "_"));
		sb.append(ON + table);
		sb.append(OPENING_BRACE);
		sb.append(join(columns, SEPARATOR));
		sb.append(CLOSING_BRACE);
		return sb.toString();
	}

	public static ArrayList<String> getTableNames(SQLiteDatabase db) {
		return readStrings(db,
				"SELECT name FROM sqlite_master WHERE type='table'", 0);
	}

	public static ArrayList<String> getColumnNames(SQLiteDatabase db,
			String table) {
		return readStrings(db, "PRAGMA table_info(" + table + ")", 1);
	}

	public static String getSQLCreate(String tableName,
			FieldSpec<ColumnAnn>[] specs) {
		StringBuilder sb = new StringBuilder();
		sb.append(CREATE_TABLE + tableName + OPENING_BRACE);
		sb.append(PK);
		StringBuilder fkSb = new StringBuilder();
		for (FieldSpec<ColumnAnn> spec : specs) {
			if (Column.ID.equals(spec.ann.name)) {
				// already got it
				continue;
			}
			sb.append(SEPARATOR);
			sb.append(spec.ann.name);
			Converter<?> converter = ConverterRegistry.getConverter(spec.field
					.getType());
			sb.append(converter.getDBColumnType());
			if (!spec.ann.nullable) {
				sb.append(NOT_NULL);
			}
			if (spec.ann.unique) {
				sb.append(UNIQUE);
			}
			if (isEntity(spec.field.getType())) {
				fkSb.append(SEPARATOR);
				appendForeignKeyDef(spec, fkSb);
			}
		}
		sb.append(fkSb);
		sb.append(CLOSING_BRACE);
		return sb.toString();
	}

	private static void appendForeignKeyDef(FieldSpec<ColumnAnn> spec,
			StringBuilder sb) {
		Class<? extends Entity> entityType = spec.field.getType().asSubclass(
				Entity.class);
		String foreignTableName = getTableName(entityType);
		sb.append("FOREIGN KEY(");
		sb.append(spec.ann.name);
		sb.append(") REFERENCES ");
		sb.append(foreignTableName);
		sb.append("(").append(Column.ID).append(") ON DELETE CASCADE");
	}

	private static ArrayList<String> readStrings(SQLiteDatabase db,
			String query, int colIdx) {
		ArrayList<String> rows = new ArrayList<String>();
		Cursor c = db.rawQuery(query, null);
		while (c.moveToNext()) {
			rows.add(c.getString(colIdx));
		}
		c.close();
		return rows;
	}

}
