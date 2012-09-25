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
import static org.droidparts.util.PersistUtils.buildPlaceholders;
import static org.droidparts.util.PersistUtils.toWhereArgs;

import java.util.ArrayList;

import org.droidparts.contract.DB;
import org.droidparts.contract.SQL;
import org.droidparts.model.Entity;
import org.droidparts.util.L;

import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

public abstract class Statement<EntityType extends Entity> implements
		SQL {

	protected final SQLiteDatabase db;
	protected final String tableName;

	private String selection;
	private String[] selectionArgs;
	private final ArrayList<Pair<String, Pair<Is, Object[]>>> whereList = new ArrayList<Pair<String, Pair<Is, Object[]>>>();

	public Statement(SQLiteDatabase db, String tableName) {
		this.db = db;
		this.tableName = tableName;
	}

	public Statement<EntityType> whereId(long... oneOrMore) {
		if (oneOrMore.length == 1) {
			return where(DB.Column.ID, Is.EQUAL, oneOrMore[0]);
		} else {
			return where(DB.Column.ID, Is.IN, oneOrMore);
		}
	}

	protected Statement<EntityType> where(String columnName,
			Is operator, Object... columnValue) {
		selection = null;
		columnValue = varArgsHack(columnValue);
		whereList.add(Pair.create(columnName,
				Pair.create(operator, columnValue)));
		return this;
	}

	protected Statement<EntityType> where(String selection,
			Object... selectionArgs) {
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
			String[] whereArgs = toWhereArgs(p.second.second);
			int argNum = whereArgs.length;
			//
			if (i > 0) {
				selectionBuilder.append(AND);
			}
			selectionBuilder.append(columnName).append(operator.str);
			switch (operator) {
			case NULL:
			case NOT_NULL:
				if (argNum != 0) {
					errArgs(operator, argNum);
				}
				break;
			case BETWEEN:
			case NOT_BETWEEN:
				if (argNum != 2) {
					errArgs(operator, argNum);
				}
				break;
			case IN:
			case NOT_IN:
				if (argNum < 1) {
					errArgs(operator, argNum);
				}
				selectionBuilder.append("(");
				selectionBuilder.append(buildPlaceholders(whereArgs.length));
				selectionBuilder.append(")");
				break;
			default:
				if (argNum != 1) {
					errArgs(operator, argNum);
				}
				break;
			}
			selectionArgsBuilder.addAll(asList(whereArgs));
		}
		selection = selectionBuilder.toString();
		selectionArgs = selectionArgsBuilder
				.toArray(new String[selectionArgsBuilder.size()]);
	}

	private void errArgs(Is operator, int num) {
		L.e("Invalid number of agruments for " + operator.str + ": " + num
				+ ".");
	}

}
