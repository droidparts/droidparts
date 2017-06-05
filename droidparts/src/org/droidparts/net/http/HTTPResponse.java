/**
 * Copyright 2017 Alex Yanchenko
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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.droidparts.contract.HTTP.StatusCode;
import org.droidparts.net.http.worker.HTTPInputStream;
import org.droidparts.util.L;


public class HTTPResponse {

	public final int code;
	protected final Map<String, List<String>> headers = new HashMap<String, List<String>>();

	public final String body;
	// or
	public final HTTPInputStream inputStream;

	public HTTPResponse(int code, Map<String, List<String>> headers, String body, HTTPInputStream inputStream) {
		this.code = code;
		this.headers.putAll(headers);
		this.body = body;
		this.inputStream = inputStream;
	}

	public boolean isSuccessCode() {
		return (code >= StatusCode.OK) && (code < StatusCode.MULTIPLE_CHOICES);
	}

	public long getHeaderDate(String name) {
		long val = 0;
		String valStr = getHeaderString(name);
		if (valStr != null) {
			try {
				val = Date.parse(valStr);
			} catch (Exception e) {
				L.d(e);
			}
		}
		return val;
	}

	public int getHeaderInt(String name) {
		int val = 0;
		String valStr = getHeaderString(name);
		if (valStr != null) {
			try {
				val = Integer.valueOf(valStr);
			} catch (Exception e) {
				L.d(e);
			}
		}
		return val;
	}

	public String getHeaderString(String name) {
		String val = null;
		List<String> vals = headers.get(name);
		if (vals != null && vals.size() == 1) {
			val = vals.get(0);
		}

		return val;
	}

	@Override
	public int hashCode() {
		return (code + body).hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof HTTPResponse) {
			return hashCode() == o.hashCode();
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "HTTP RESPONSE " + code + ", body: '" + body + "'.";
	}

}