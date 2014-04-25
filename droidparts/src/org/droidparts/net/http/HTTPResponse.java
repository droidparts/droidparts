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

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.droidparts.net.http.worker.HTTPInputStream;
import org.droidparts.util.L;

public class HTTPResponse {

	public int code;
	public Map<String, List<String>> headers;

	public String body;
	// or
	public HTTPInputStream inputStream;

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
		if (headers != null) {
			List<String> vals = headers.get(name);
			if (vals != null && vals.size() == 1) {
				val = vals.get(0);
			}
		}
		return val;
	}

	@Override
	public int hashCode() {
		return (code + body).hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (o instanceof HTTPResponse) {
			return hashCode() == o.hashCode();
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "Response code: " + code + ", body: " + body;
	}

}