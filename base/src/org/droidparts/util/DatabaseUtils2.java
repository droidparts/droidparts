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

import static org.droidparts.util.Strings.isNotEmpty;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseUtils2 extends DatabaseUtils {

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
				sb.append(", ");
			}
			sb.append("?");
		}
		return sb.toString();
	}

	public static int getRowCount(SQLiteDatabase db, String tableName,
			String selection, String[] selectionArgs) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT count(*) FROM ").append(tableName);
		if (isNotEmpty(selection)) {
			sb.append(" WHERE ").append(selection);
		}
		return (int) longForQuery(db, sb.toString(), selectionArgs);
	}
}
