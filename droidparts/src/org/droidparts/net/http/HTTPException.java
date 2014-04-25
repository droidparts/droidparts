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
package org.droidparts.net.http;

import org.droidparts.contract.HTTP.ResponseCode;

public class HTTPException extends Exception {

	private static final long serialVersionUID = 1L;

	private int respCode = -1;

	public HTTPException(Throwable cause) {
		super(cause);
	}

	public HTTPException(int respCode, String respBody) {
		super(respBody);
		this.respCode = respCode;
	}

	/**
	 * @see ResponseCode
	 */
	public int getResponseCode() {
		return respCode;
	}

	@Override
	public String toString() {
		if (respCode != -1) {
			StringBuilder sb = new StringBuilder();
			sb.append("Response code: ");
			sb.append(respCode);
			sb.append(", body: ");
			sb.append(getMessage());
			return sb.toString();
		} else {
			return super.toString();
		}
	}

}
