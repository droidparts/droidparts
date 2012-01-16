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

import static org.droidparts.contract.DB.BLOB;
import static org.droidparts.contract.DB.CLOSING_BRACE;
import static org.droidparts.contract.DB.CREATE_TABLE;
import static org.droidparts.contract.DB.INTEGER;
import static org.droidparts.contract.DB.NOT_NULL;
import static org.droidparts.contract.DB.OPENING_BRACE;
import static org.droidparts.contract.DB.PK;
import static org.droidparts.contract.DB.REAL;
import static org.droidparts.contract.DB.SEPARATOR;
import static org.droidparts.contract.DB.TEXT;
import static org.droidparts.contract.DB.UNIQUE;
import static org.droidparts.reflection.util.TypeHelper.isBitmap;
import static org.droidparts.reflection.util.TypeHelper.isBoolean;
import static org.droidparts.reflection.util.TypeHelper.isByteArray;
import static org.droidparts.reflection.util.TypeHelper.isDBModel;
import static org.droidparts.reflection.util.TypeHelper.isDouble;
import static org.droidparts.reflection.util.TypeHelper.isEnum;
import static org.droidparts.reflection.util.TypeHelper.isFloat;
import static org.droidparts.reflection.util.TypeHelper.isInteger;
import static org.droidparts.reflection.util.TypeHelper.isLong;
import static org.droidparts.reflection.util.TypeHelper.isString;
import static org.droidparts.reflection.util.TypeHelper.isUUID;

import java.util.ArrayList;

import org.droidparts.contract.DB.Column;
import org.droidparts.model.DBModel;
import org.droidparts.reflection.model.DBField;
import org.droidparts.reflection.processor.DBAnnotationProcessor;
import org.droidparts.util.L;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class DBOpenHelper extends SQLiteOpenHelper {

	public DBOpenHelper(Context context, String name, int version) {
		super(context, name, null, version);
	}

	@Override
	public final void onCreate(SQLiteDatabase db) {
		ArrayList<String> queries = new ArrayList<String>();
		for (Class<? extends DBModel> cls : getModelClasses()) {
			String query = getSQLCreate(new DBAnnotationProcessor(cls));
			queries.add(query);
		}
		execQueries(db, queries);
		onCreateExtra(db);
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

	protected void dropAllTables(SQLiteDatabase db) {
		ArrayList<String> queries = new ArrayList<String>();
		for (Class<? extends DBModel> cls : getModelClasses()) {
			String tableName = new DBAnnotationProcessor(cls).getTableName();
			queries.add("DROP TABLE IF EXISTS " + tableName + ";");
		}
		execQueries(db, queries);
	}

	protected void onCreateExtra(SQLiteDatabase db) {
	}

	protected abstract Class<? extends DBModel>[] getModelClasses();

	private String getSQLCreate(DBAnnotationProcessor proc) {
		DBField[] dbFields = proc.getFields();
		StringBuilder sb = new StringBuilder();
		sb.append(CREATE_TABLE + proc.getTableName() + OPENING_BRACE);
		sb.append(PK + SEPARATOR);
		for (int i = 0; i < dbFields.length; i++) {
			DBField dbField = dbFields[i];
			if (Column.ID.equals(dbField.columnName)) {
				// already got it
				continue;
			}
			String columnType = getColumnType(dbField.fieldType);
			if (columnType != null) {
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
				if (i < dbFields.length - 1) {
					sb.append(SEPARATOR);
				}
			}
		}
		sb.append(CLOSING_BRACE);
		return sb.toString();
	}

	private String getColumnType(Class<?> cls) {
		if (isBoolean(cls) || isInteger(cls) || isLong(cls)) {
			return INTEGER;
		} else if (isFloat(cls) || isDouble(cls)) {
			return REAL;
		} else if (isString(cls) || isUUID(cls) || isEnum(cls)) {
			return TEXT;
		} else if (isByteArray(cls) || isBitmap(cls)) {
			return BLOB;
		} else if (isDBModel(cls)) {
			return INTEGER;
		} else {
			return null;
		}
	}

}
