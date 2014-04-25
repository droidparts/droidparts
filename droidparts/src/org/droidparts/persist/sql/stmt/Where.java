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
package org.droidparts.persist.sql.stmt;

import static java.util.Arrays.asList;
import static org.droidparts.inner.PersistUtils.buildPlaceholders;
import static org.droidparts.inner.PersistUtils.toWhereArgs;
import static org.droidparts.inner.ReflectionUtils.varArgsHack;

import java.util.ArrayList;

import org.droidparts.contract.SQL;
import org.droidparts.util.L;

import android.util.Pair;

public class Where implements SQL {

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

	Pair<String, Object[]> build() {
		Pair<String, ArrayList<String>> p = build(this);
		return Pair.create(p.first, p.second.toArray());
	}

	private static Pair<String, ArrayList<String>> build(Where where) {
		StringBuilder selectionBuilder = new StringBuilder();
		ArrayList<String> selectionArgsBuilder = new ArrayList<String>();
		for (int i = 0; i < where.whereSpecs.size(); i++) {
			Object obj = where.whereSpecs.get(i);
			boolean and;
			boolean braces = false;
			Pair<String, ArrayList<String>> sel;
			if (obj instanceof Where) {
				braces = true;
				Where where2 = (Where) obj;
				and = where2.and;
				sel = build(where2);
			} else {
				WhereSpec spec = (WhereSpec) obj;
				and = spec.and;
				sel = build(spec);
			}
			if (i > 0) {
				selectionBuilder.append(and ? AND : OR);
			}
			if (braces) {
				selectionBuilder.append("(").append(sel.first).append(")");
			} else {
				selectionBuilder.append(sel.first);
			}
			selectionArgsBuilder.addAll(sel.second);
		}
		return Pair.create(selectionBuilder.toString(), selectionArgsBuilder);
	}

	private static Pair<String, ArrayList<String>> build(WhereSpec spec) {
		StringBuilder selectionBuilder = new StringBuilder();
		ArrayList<String> selectionArgsBuilder = new ArrayList<String>();
		String[] whereArgs = toWhereArgs(spec.columnValue);
		int argNum = whereArgs.length;
		//
		selectionBuilder.append(spec.columnName).append(spec.operator.str);
		switch (spec.operator) {
		case NULL:
		case NOT_NULL:
			if (argNum != 0) {
				errArgs(spec.operator, argNum);
			}
			break;
		case BETWEEN:
		case NOT_BETWEEN:
			if (argNum != 2) {
				errArgs(spec.operator, argNum);
			}
			break;
		case IN:
		case NOT_IN:
			if (argNum < 1) {
				errArgs(spec.operator, argNum);
			}
			selectionBuilder.append("(");
			selectionBuilder.append(buildPlaceholders(whereArgs.length));
			selectionBuilder.append(")");
			break;
		default:
			if (argNum != 1) {
				errArgs(spec.operator, argNum);
			}
		}
		selectionArgsBuilder.addAll(asList(whereArgs));
		return Pair.create(selectionBuilder.toString(), selectionArgsBuilder);
	}

	private static void errArgs(Is operator, int num) {
		L.e("Invalid number of agruments for '%s': %d.", operator, num);
	}

	//

	private boolean and;

	private final ArrayList<Object> whereSpecs = new ArrayList<Object>();

	private static class WhereSpec {
		final boolean and;

		final String columnName;
		final Is operator;
		final Object[] columnValue;

		WhereSpec(boolean and, String columnName, Is operator,
				Object... columnValue) {
			this.and = and;
			this.columnName = columnName;
			this.operator = operator;
			this.columnValue = varArgsHack(columnValue);
		}
	}
}
