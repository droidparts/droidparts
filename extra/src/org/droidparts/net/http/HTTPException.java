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

package org.droidparts.net.http;

import static org.droidparts.util.Strings.isEmpty;

import org.apache.http.HttpStatus;

public class HTTPException extends Exception {

	private static final long serialVersionUID = 1L;

	private final int code;

	public HTTPException(int code) {
		this.code = code;
	}

	public HTTPException(Throwable cause) {
		super(cause);
		code = -1;
	}

	public HTTPException(String message) {
		super(message);
		code = -1;
	}

	/**
	 * @see HttpStatus
	 */
	public int getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		String superMsg = super.getMessage();
		if (isEmpty(superMsg)) {
			return "HTTP response code: " + code;
		} else {
			return superMsg;
		}
	}

	@Override
	public String toString() {
		return getMessage();
	}
}
