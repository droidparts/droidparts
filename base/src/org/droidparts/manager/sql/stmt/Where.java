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
package org.droidparts.manager.sql.stmt;

import java.util.ArrayList;

import org.droidparts.contract.DB;

import android.util.Pair;

public class Where implements DB {

	public static enum Operator {

		EQUALS(DB.EQUALS), NOT_EQUAL(DB.NOT_EQUAL), LESS(DB.LESS), GREATER(
				DB.GREATER);

		public String str;

		Operator(String str) {
			this.str = str;
		}

	}

	private final ArrayList<Pair<String, Pair<Operator, Object>>> selection = new ArrayList<Pair<String, Pair<Operator, Object>>>();

	protected Where where(String column, Operator operator, Object val) {
		selection.add(Pair.create(column, Pair.create(operator, val)));
		return this;
	}

	protected Pair<String, String[]> buildSelection() {
		StringBuilder whereBuilder = new StringBuilder();
		ArrayList<String> whereArgs = new ArrayList<String>();
		for (int i = 0; i < selection.size(); i++) {
			Pair<String, Pair<Operator, Object>> p = selection.get(i);
			String columnName = p.first;
			String query = p.second.first.str;
			String columnVal = BaseBuilder.toArg(p.second.second);
			if (i > 0) {
				whereBuilder.append(AND);
			}
			whereBuilder.append(query).append(columnName);
			whereArgs.add(columnVal);
		}
		String where = whereBuilder.toString();
		String[] whereArgsArr = whereArgs.toArray(new String[whereArgs.size()]);
		return Pair.create(where, whereArgsArr);
	}

}
