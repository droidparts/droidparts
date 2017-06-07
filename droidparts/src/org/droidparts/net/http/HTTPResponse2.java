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

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.droidparts.net.http.worker.HTTPInputStream;

public class HTTPResponse2 extends HTTPResponse {

	public HTTPResponse2(HTTPResponse src) {
		this(src.code, src.headers, src.body, src.inputStream);
	}

	public HTTPResponse2(int code, Map<String, List<String>> headers, String body, HTTPInputStream inputStream) {
		super(code, headers, body, inputStream);
	}

	public JSONObject bodyAsJSONObject() throws JSONException {
		return new JSONObject(body);
	}

	public JSONArray bodyAsJSONArray() throws JSONException {
		return new JSONArray(body);
	}
}
