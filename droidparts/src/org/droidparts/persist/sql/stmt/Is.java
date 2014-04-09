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

import org.droidparts.contract.SQL;

public enum Is {

	EQUAL(SQL.EQUAL), NOT_EQUAL(SQL.NOT_EQUAL), //
	LESS(SQL.LESS), LESS_OR_EQUAL(SQL.LESS_OR_EQUAL), //
	GREATER(SQL.GREATER), GREATER_OR_EQUAL(SQL.GREATER_OR_EQUAL), //

	NULL(SQL.NULL), NOT_NULL(SQL.NOT_NULL),

	BETWEEN(SQL.BETWEEN), NOT_BETWEEN(SQL.NOT_BETWEEN),

	IN(SQL.IN), NOT_IN(SQL.NOT_IN),

	LIKE(SQL.LIKE), NOT_LIKE(SQL.NOT_LIKE);

	public final String str;

	Is(String str) {
		this.str = str;
	}

	@Override
	public String toString() {
		return "IS " + super.toString();
	}

}