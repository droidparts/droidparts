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
package org.droidparts.persist.sql.stmt;

import static java.util.Arrays.asList;
import static org.droidparts.reflect.util.ReflectionUtils.varArgsHack;
import static org.droidparts.util.DatabaseUtils2.buildPlaceholders;
import static org.droidparts.util.DatabaseUtils2.toWhereArgs;

import java.util.ArrayList;

import org.droidparts.contract.SQL;

import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

public abstract class StatementBuilder implements SQL {

	protected final SQLiteDatabase db;
	protected final String tableName;

	private String selection;
	private String[] selectionArgs;
	private final ArrayList<Pair<String, Pair<Is, Object[]>>> whereList = new ArrayList<Pair<String, Pair<Is, Object[]>>>();

	public StatementBuilder(SQLiteDatabase db, String tableName) {
		this.db = db;
		this.tableName = tableName;
	}

	protected StatementBuilder where(String columnName, Is operator,
			Object... columnValue) {
		selection = null;
		columnValue = varArgsHack(columnValue);
		whereList.add(Pair.create(columnName,
				Pair.create(operator, columnValue)));
		return this;
	}

	protected StatementBuilder where(String selection, Object... selectionArgs) {
		this.selection = selection;
		this.selectionArgs = toWhereArgs(selectionArgs);
		return this;
	}

	protected Pair<String, String[]> getSelection() {
		if (selection == null) {
			buildSelection();
		}
		return Pair.create(selection, selectionArgs);
	}

	private void buildSelection() {
		StringBuilder selectionBuilder = new StringBuilder();
		ArrayList<String> selectionArgsBuilder = new ArrayList<String>();
		for (int i = 0; i < whereList.size(); i++) {
			Pair<String, Pair<Is, Object[]>> p = whereList.get(i);
			String columnName = p.first;
			Is operator = p.second.first;
			Object[] columnValues = p.second.second;
			if (i > 0) {
				selectionBuilder.append(AND);
			}
			selectionBuilder.append(columnName).append(operator.str);
			switch (operator) {
			case NULL:
			case NOT_NULL:
				break;
			case IN:
			case NOT_IN:
				selectionBuilder.append("(");
				selectionBuilder.append(buildPlaceholders(columnValues.length));
				selectionBuilder.append(")");
				selectionArgsBuilder.addAll(asList(toWhereArgs(columnValues)));
				break;
			default:
				String columnVal = toWhereArgs(columnValues)[0];
				selectionArgsBuilder.add(columnVal);
				break;
			}
		}
		selection = selectionBuilder.toString();
		selectionArgs = selectionArgsBuilder
				.toArray(new String[selectionArgsBuilder.size()]);
	}

}
