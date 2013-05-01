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
package org.droidparts.persist.sql.stmt;

import java.util.ArrayList;
import java.util.Arrays;

import org.droidparts.contract.SQL;

import android.util.Pair;

public class Where implements SQL {

	// TODO

	public Where(String columnName, Is operator, Object... columnValue) {
		whereSpecs.add(new WhereSpec(true, columnName, operator, columnValue));
	}

	public Where and(Where where) {
		where.and = true;
		whereSpecs.add(where);
		return this;
	}

	public Where or(Where where) {
		where.and = false;
		whereSpecs.add(where);
		return this;
	}

	public Where and(String columnName, Is operator, Object... columnValue) {
		whereSpecs.add(new WhereSpec(true, columnName, operator, columnValue));
		return this;
	}

	public Where or(String columnName, Is operator, Object... columnValue) {
		whereSpecs.add(new WhereSpec(false, columnName, operator, columnValue));
		return this;
	}

	Pair<String, String[]> build() {
		return build(this);
	}

	private static Pair<String, String[]> build(Where where) {
		StringBuilder selection = new StringBuilder();
		ArrayList<String> selectionArgs = new ArrayList<String>();
		for (int i = 0; i < where.whereSpecs.size(); i++) {
			Object obj = where.whereSpecs.get(i);
			boolean and;
			Pair<String, String[]> sel;
			if (obj instanceof Where) {
				Where where2 = (Where) obj;
				and = where2.and;
				sel = build(where2);
			} else {
				WhereSpec spec = (WhereSpec) obj;
				and = spec.and;
				sel = build(spec);
			}
			if (i > 0) {
				selection.append(and ? AND : OR);
			}
			selection.append(sel.first);
			selectionArgs.addAll(Arrays.asList(sel.second));
		}
		return Pair.create(selection.toString(),
				selectionArgs.toArray(new String[selectionArgs.size()]));
	}

	private static Pair<String, String[]> build(WhereSpec spec) {
		return null;
	}

	//
	boolean and;

	private final ArrayList<Object> whereSpecs = new ArrayList<Object>();

	private static class WhereSpec {
		boolean and;

		String columnName;
		Is operator;
		Object[] columnValue;

		WhereSpec(boolean and, String columnName, Is operator,
				Object... columnValue) {
			this.and = and;
			this.columnName = columnName;
			this.operator = operator;
			this.columnValue = columnValue;
		}
	}
}
