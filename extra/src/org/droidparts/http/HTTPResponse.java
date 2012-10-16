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
package org.droidparts.http;

import java.util.List;
import java.util.Map;

public class HTTPResponse {

	public int code;
	public String body;
	public Map<String, List<String>> headers;

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
